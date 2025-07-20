package dev.tushar.ecommerceapi.service;

import dev.tushar.ecommerceapi.dto.request.ProductRequestDTO;
import dev.tushar.ecommerceapi.dto.response.ProductDetailResponseDTO;
import dev.tushar.ecommerceapi.dto.response.ProductResponseDTO;
import dev.tushar.ecommerceapi.security.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

public interface ProductService {

    ProductDetailResponseDTO createProduct(CustomUserDetails currentUser, ProductRequestDTO productRequest);

    ProductDetailResponseDTO getProductById(Long productId);

    Page<ProductResponseDTO> searchProducts(
            String q, // search query
            Set<Long> categoryIds, // category ids
            BigDecimal minPrice, // min price
            BigDecimal maxPrice, // max price
            Map<String, String> attributes, // category specific attributes
            Pageable pageable
    );

    Page<ProductResponseDTO> getProductsByCategory(Long categoryId, Pageable pageable);
}