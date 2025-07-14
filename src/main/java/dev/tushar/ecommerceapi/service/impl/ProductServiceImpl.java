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
import dev.tushar.ecommerceapi.specification.ProductVariantSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final BusinessRepository businessRepository;
    private final CategoryRepository categoryRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductSpecification productSpecification;
    private final ProductVariantSpecification productVariantSpecification;

    @Override
    public ProductResponseDTO createProduct(CustomUserDetails currentUser, ProductRequestDTO request) {

        Business business = businessRepository.findByUserId(currentUser.user().getId())
                .orElseThrow(() -> new ApiException(
                        HttpStatus.FORBIDDEN, "You must have a registered business to create products.")
                );

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
        Product savedProduct = productRepository.save(product);

        return mapToProductResponseDTO(savedProduct);
    }

    @Override
    public ProductResponseDTO getProductById(Long productId) {
        Product product = productRepository.findByIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product with ID " + productId + " not found."));
        return mapToProductResponseDTO(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> searchProducts(String q, Set<Long> categoryIds, BigDecimal minPrice, BigDecimal maxPrice, Map<String, String> attributes, Pageable pageable) {

        Set<Long> matchingProductIds = null;

        if (minPrice != null || maxPrice != null || (attributes != null && !attributes.isEmpty())) {
            Specification<ProductVariant> variantSpec = productVariantSpecification.withFilters(minPrice, maxPrice, attributes);
            List<ProductVariant> matchingVariants = productVariantRepository.findAll(variantSpec);

            if (matchingVariants.isEmpty()) {
                return Page.empty(pageable);
            }
            matchingProductIds = matchingVariants.stream()
                    .map(variant -> variant.getProduct().getId())
                    .collect(Collectors.toSet());
        }

        Specification<Product> finalProductSpec = productSpecification.withFilters(q, categoryIds, matchingProductIds);

        return productRepository.findAll(finalProductSpec, pageable).map(this::mapToProductResponseDTO);
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

    private ProductResponseDTO mapToProductResponseDTO(Product product) {
        ProductResponseDTO.BusinessInfo businessInfo = new ProductResponseDTO.BusinessInfo(
                product.getBusiness().getId(),
                product.getBusiness().getBusinessName()
        );

        ProductResponseDTO.CategoryInfo categoryInfo = new ProductResponseDTO.CategoryInfo(
                product.getCategory().getId(),
                product.getCategory().getName()
        );

        List<ProductResponseDTO.ProductVariantDTO> variantDTOs = product.getVariants().stream()
                .map(variant -> {
                    Map<String, String> stringAttributes = variant.getAttributes().entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));

                    return new ProductResponseDTO.ProductVariantDTO(
                            variant.getId(),
                            variant.getPrice(),
                            variant.getStockQuantity(),
                            variant.getSku(),
                            stringAttributes,
                            variant.getImageUrls()
                    );
                }).collect(Collectors.toList());

        return new ProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                businessInfo,
                categoryInfo,
                variantDTOs
        );
    }
}