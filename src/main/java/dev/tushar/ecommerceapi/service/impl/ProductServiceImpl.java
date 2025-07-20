package dev.tushar.ecommerceapi.service.impl;

import dev.tushar.ecommerceapi.dto.request.ProductRequestDTO;
import dev.tushar.ecommerceapi.dto.response.ProductDetailResponseDTO;
import dev.tushar.ecommerceapi.dto.response.ProductResponseDTO;
import dev.tushar.ecommerceapi.entity.*;
import dev.tushar.ecommerceapi.exception.ApiException;
import dev.tushar.ecommerceapi.model.AttributeType;
import dev.tushar.ecommerceapi.repository.*;
import dev.tushar.ecommerceapi.security.CustomUserDetails;
import dev.tushar.ecommerceapi.service.ProductService;
import dev.tushar.ecommerceapi.specification.ProductVariantSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static dev.tushar.ecommerceapi.util.MyUtils.getParentIdFromPath;
import static dev.tushar.ecommerceapi.util.MyUtils.validateColorHex;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final BusinessRepository businessRepository;
    private final CategoryRepository categoryRepository;
    private final ProductVariantSpecification productVariantSpecification;


    @Override
    public ProductDetailResponseDTO createProduct(CustomUserDetails currentUser, ProductRequestDTO request) {
        Business business = businessRepository.findByUserId(currentUser.user().getId())
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "You must have a registered business to create products."));

        if (!business.getVerificationStatus().equals("VERIFIED")) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Your business is not verified yet.");
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Invalid category ID: " + request.categoryId()));

        Set<CategoryAttribute> effectiveRules = getResolvedCategoryRules(category);
        validateProductVariants(request.variants(), effectiveRules);

        Product product = Product.builder()
                .business(business)
                .category(category)
                .name(request.name())
                .description(request.description())
                .build();

        List<ProductVariant> variants = request.variants().stream()
                .map(variantDto -> ProductVariant.builder()
                        .product(product)
                        .price(variantDto.price())
                        .stockQuantity(variantDto.stockQuantity())
                        .attributes(variantDto.attributes())
                        .build())
                .collect(Collectors.toList());

        product.setVariants(variants);
        product.setPrimaryVariant(variants.get(0));

        Product savedProduct = productRepository.save(product);
        return mapToProductDetailResponseDTO(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponseDTO getProductById(Long productId) {
        Product product = productRepository.findByIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product with ID " + productId + " not found."));

        return mapToProductDetailResponseDTO(product);
    }

    /**
     * Searches for products with filtering, including hierarchical category matching.
     * If the search query 'q' matches a category name, this method automatically includes all
     * products from that category and its descendants.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> searchProducts(
            String q,
            Set<Long> categoryIds,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Map<String, String> attributes,
            Pageable pageable
    ) {
        // Suppose a user is searching for "Leather Bag" of brown color and want the results sorted by price in ascending order
        // .../products/search?q=Leather+Bag&attr_Color=%23A52A2A&sort=price,asc

        // For this, we will first check if we have any given set of category ids? e.g., /products/search?categoryIds=1,2,3
        // If not, we will create a new set.
        Set<Long> effectiveCategoryIds = (categoryIds != null) ? new HashSet<>(categoryIds) : new HashSet<>();

        // Now, we will also check if the search query 'q' have any token that matches any of
        // the category we have.
        if (q != null && !q.isBlank()) {

            // To do this, we will split the search query into tokens and check if any token
            // matches any of the categories we have.
            List<String> searchTokens = List.of(q.toLowerCase().split("\\s+"));
            List<Category> matchedCategories = categoryRepository.findByNameIn(searchTokens);

            // Next, we will get all the descendants of these found categories token
            // We're already saving path in enumerated form, so it's pretty easy to do using LIKE :path%
            for (Category matchedCategory : matchedCategories) {
                List<Category> descendantCategories = categoryRepository.findAllByPath(matchedCategory.getPath()); // 1/2/3 -> 1/% -> [1, 2, 3]
                descendantCategories.stream()
                        .map(Category::getId)
                        // after getting all the categories, we will get their
                        // ids and add it to our main effectiveCategoryIds that will be used for query
                        .forEach(effectiveCategoryIds::add); // Consumer<Long>
            }
        }

        // Now, it's time to do actual searching using the effectiveCategoryIds, q, minPrice, maxPrice, and attributes
        // For this, we will use or ProductVariantSpecification that uses criteriaBuilder to build the query dynamically
        Specification<ProductVariant> spec = productVariantSpecification.withFilters(q, effectiveCategoryIds, minPrice, maxPrice, attributes);

        List<ProductVariant> allMatchingVariants = productVariantRepository.findAll(spec, pageable.getSort());

        Map<Long, ProductVariant> uniqueProductVariantsMap = new LinkedHashMap<>();
        for (ProductVariant variant : allMatchingVariants) {
            uniqueProductVariantsMap.putIfAbsent(variant.getProduct().getId(), variant);
        }
        List<ProductVariant> uniqueVariantsList = new ArrayList<>(uniqueProductVariantsMap.values());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), uniqueVariantsList.size());

        List<ProductVariant> pageContent = (start > uniqueVariantsList.size())
                ? Collections.emptyList()
                : uniqueVariantsList.subList(start, end);

        List<ProductResponseDTO> dtoList = pageContent.stream()
                .map(this::mapVariantToProductResponseDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, uniqueVariantsList.size());
    }


    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getProductsByCategory(Long categoryId, Pageable pageable) {
        Category parentCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Category with ID " + categoryId + " not found."));

        List<Category> allCategoriesInBranch = categoryRepository.findAllByPath(parentCategory.getPath());

        Set<Long> allCategoryIds = allCategoriesInBranch.stream()
                .map(Category::getId)
                .collect(Collectors.toSet());
        // We can reuse our powerful search method here.
        return searchProducts(null, allCategoryIds, null, null, null, pageable);
    }


    // Helper Methods - Mappers

    private ProductDetailResponseDTO mapToProductDetailResponseDTO(Product product) {
        List<ProductDetailResponseDTO.VariantDetailDTO> variantDTOs = product.getVariants().stream()
                .map(variant -> {
                    Map<String, String> stringAttributes = variant.getAttributes().entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));

                    return new ProductDetailResponseDTO.VariantDetailDTO(
                            variant.getId(),
                            variant.getPrice(),
                            variant.getStockQuantity(),
                            stringAttributes
                    );
                })
                .collect(Collectors.toList());

        return new ProductDetailResponseDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                new ProductResponseDTO.BusinessInfo(product.getBusiness().getId(), product.getBusiness().getBusinessName()),
                new ProductResponseDTO.CategoryInfo(product.getCategory().getId(), product.getCategory().getName()),
                variantDTOs
        );
    }

    private ProductResponseDTO mapVariantToProductResponseDTO(ProductVariant variant) {
        Product product = variant.getProduct();
        Map<String, String> stringAttributes = variant.getAttributes().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));

        return new ProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                new ProductResponseDTO.BusinessInfo(product.getBusiness().getId(), product.getBusiness().getBusinessName()),
                new ProductResponseDTO.CategoryInfo(product.getCategory().getId(), product.getCategory().getName()),
                variant.getId(),
                variant.getPrice(),
                variant.getStockQuantity(),
                stringAttributes
        );
    }


    // Helper Methods - Validation & Rules

    @Transactional(readOnly = true)
    private Set<CategoryAttribute> getResolvedCategoryRules(Category category) {
        Map<String, CategoryAttribute> effectiveRulesMap = new HashMap<>();
        Category current = category;

        while (current != null) {
            for (CategoryAttribute rule : current.getCategoryAttributes()) {
                effectiveRulesMap.putIfAbsent(rule.getAttribute().getName(), rule);
            }
            Long parentId = getParentIdFromPath(current.getPath());
            current = (parentId != null) ? categoryRepository.findById(parentId).orElse(null) : null;
        }

        return new HashSet<>(effectiveRulesMap.values());
    }

    private void validateProductVariants(List<ProductRequestDTO.ProductVariantRequestDTO> variants, Set<CategoryAttribute> effectiveRules) {
        Set<String> requiredAttributeNames = effectiveRules.stream()
                .map(rule -> rule.getAttribute().getName())
                .collect(Collectors.toSet());

        for (ProductRequestDTO.ProductVariantRequestDTO variant : variants) {
            Map<String, Object> submittedAttrs = variant.attributes();

            if (!submittedAttrs.keySet().equals(requiredAttributeNames)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Product attributes do not match the attributes required by the category. Required: " + requiredAttributeNames + ", but you provided " + submittedAttrs.keySet());
            }

            for (CategoryAttribute rule : effectiveRules) {
                String attributeName = rule.getAttribute().getName();
                Object attributeValue = submittedAttrs.get(attributeName);

                if (attributeName.equals("Color") && rule.getAttributeType() == AttributeType.TEXT) {
                    if (!(attributeValue instanceof String) || !validateColorHex((String) attributeValue)) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid hex color code for attribute 'Color'. It must be in #RRGGBB format.");
                    }
                }

                if (rule.getAttributeType() == AttributeType.ENUM) {
                    if (!(attributeValue instanceof String) || !rule.getOptionSet().getOptions().contains(attributeValue.toString())) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid value for attribute '" + attributeName + "'. Allowed values are: " + rule.getOptionSet().getOptions());
                    }
                } else if (rule.getAttributeType() == AttributeType.BOOLEAN) {
                    if (!(attributeValue instanceof Boolean)) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Value for '" + attributeName + "' must be a boolean (true/false).");
                    }
                }
            }
        }
    }
}