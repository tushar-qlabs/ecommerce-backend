package dev.tushar.ecommerceapi.service;

import dev.tushar.ecommerceapi.dto.request.ProductRequestDTO;
import dev.tushar.ecommerceapi.dto.response.ProductResponseDTO;
import dev.tushar.ecommerceapi.security.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

public interface ProductService {

    ProductResponseDTO createProduct(CustomUserDetails currentUser, ProductRequestDTO productRequest);

    ProductResponseDTO getProductById(Long productId);

    Page<ProductResponseDTO> searchProducts(String q, Set<Long> categoryIds, BigDecimal minPrice, BigDecimal maxPrice, Map<String, String> attributes, Pageable pageable);

    Page<ProductResponseDTO> getProductsByCategory(Long categoryId, Pageable pageable);
}