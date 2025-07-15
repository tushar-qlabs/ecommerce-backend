package dev.tushar.ecommerceapi.service.impl;

import dev.tushar.ecommerceapi.dto.request.ProductRequestDTO;
import dev.tushar.ecommerceapi.dto.response.ProductResponseDTO;
import dev.tushar.ecommerceapi.entity.*;
import dev.tushar.ecommerceapi.exception.ApiException;
import dev.tushar.ecommerceapi.model.AttributeType;
import dev.tushar.ecommerceapi.repository.*;
import dev.tushar.ecommerceapi.security.CustomUserDetails;
import dev.tushar.ecommerceapi.service.ProductService;
import dev.tushar.ecommerceapi.specification.ProductSpecification;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
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

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository; // We now need this
    private final BusinessRepository businessRepository;
    private final CategoryRepository categoryRepository;
    private final ProductSpecification productSpecification;

    @Override
    public ProductResponseDTO createProduct(CustomUserDetails currentUser, ProductRequestDTO request) {
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
                        .imageUrls(variantDto.imageUrls())
                        .build())
                .collect(Collectors.toList());

        product.setVariants(variants);
        product.setPrimaryVariant(variants.get(0));

        Product savedProduct = productRepository.save(product);
        return mapToFlatProductResponseDTO(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDTO getProductById(Long productId) {
        Product product = productRepository.findByIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product with ID " + productId + " not found."));
        return mapToFlatProductResponseDTO(product);
    }

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
        // First, create the specification for ProductVariant
        Specification<ProductVariant> spec = productSpecification.withFilters(q, categoryIds, minPrice, maxPrice, attributes);

        // Second we will fetch all matching variants without pagination first.
        // This is necessary to ensure we only return one variant per product.
        List<ProductVariant> allMatchingVariants = productVariantRepository.findAll(spec, pageable.getSort());

        // Third, ensure we only have one variant per product.
        // We use a Map with the product ID as the key to guarantee uniqueness.
        Map<Long, ProductVariant> uniqueProductVariantsMap = new LinkedHashMap<>();
        for (ProductVariant variant : allMatchingVariants) {
            uniqueProductVariantsMap.putIfAbsent(variant.getProduct().getId(), variant);
        }
        List<ProductVariant> uniqueVariantsList = new ArrayList<>(uniqueProductVariantsMap.values());

        // Fourth, we need to manually apply pagination to the unique list.
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), uniqueVariantsList.size());
        List<ProductVariant> pageContent = (start > uniqueVariantsList.size())
                ? Collections.emptyList()
                : uniqueVariantsList.subList(start, end);

        // Finally we create a Page object from our manually paginated list and map to DTOs.
        return new PageImpl<>(
                pageContent.stream().map(this::mapVariantToProductResponseDTO).collect(Collectors.toList()),
                pageable,
                uniqueVariantsList.size()
        );
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

        return searchProducts(null, allCategoryIds, null, null, null, pageable);
    }

    // --- Helper Methods ---

    /**
     * New mapper that creates the response from a ProductVariant.
     * This ensures the response shows the specific variant that matched the search.
     */
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
                stringAttributes,
                variant.getImageUrls()
        );
    }

    private ProductResponseDTO mapToFlatProductResponseDTO(Product product) {
        ProductVariant primaryVariant = product.getPrimaryVariant();
        if (primaryVariant == null) {
            throw new IllegalStateException("Data consistency error: Product with ID " + product.getId() + " has no primary variant set.");
        }
        return mapVariantToProductResponseDTO(primaryVariant);
    }

    @Transactional(readOnly = true)
    private Set<CategoryAttribute> getResolvedCategoryRules(Category category) {
        /*
         * This method is responsible for resolving the category rules for a given category.
         * It starts from the leaf category and works its way up to the root category.
         *
         * So, suppose we  don't have any rules at Men's but have rules at Clothing
         * then this method will return all rules from Clothing and Men's.
         *
         */

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