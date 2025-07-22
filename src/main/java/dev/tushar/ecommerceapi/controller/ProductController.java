package dev.tushar.ecommerceapi.controller;

import dev.tushar.ecommerceapi.dto.ApiResponse;
import dev.tushar.ecommerceapi.dto.request.ProductRequestDTO;
import dev.tushar.ecommerceapi.dto.response.ProductDetailResponseDTO;
import dev.tushar.ecommerceapi.dto.response.ProductResponseDTO;
import dev.tushar.ecommerceapi.security.CustomUserDetails;
import dev.tushar.ecommerceapi.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_PRODUCTS')")
    public ResponseEntity<ApiResponse<ProductDetailResponseDTO>> createProduct(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody ProductRequestDTO request)
    {
        ProductDetailResponseDTO newProduct = productService.createProduct(currentUser, request); // Change the variable type
        return new ResponseEntity<>(
                ApiResponse.success("Product created successfully with all variants.", newProduct, HttpStatus.CREATED.value()),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductDetailResponseDTO>> getProductById(@PathVariable Long productId) {
        ProductDetailResponseDTO product = productService.getProductById(productId);
        return ResponseEntity.ok(
                ApiResponse.success("Product fetched successfully.", product, HttpStatus.OK.value())
        );
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ProductResponseDTO>>> searchProducts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            HttpServletRequest request,
            Pageable pageable
    ) {
        final String ATTR_PREFIX = "attr_";
        Map<String, String> attributes = new HashMap<>();

        for (String paramName : Collections.list(request.getParameterNames())) {
            if (paramName.startsWith(ATTR_PREFIX)) {
                String attributeName = paramName.substring(ATTR_PREFIX.length());
                attributeName = attributeName.substring(0, 1).toUpperCase() + attributeName.substring(1);
                String attributeValue = request.getParameter(paramName);
                attributes.put(attributeName, attributeValue);
            }
        }

        System.out.println("Attributes: " + attributes);
        System.out.println(pageable);

        Set<Long> categoryIdSet = (categoryId != null) ? Set.of(categoryId) : null;
        Page<ProductResponseDTO> products = productService.searchProducts(q, categoryIdSet, minPrice, maxPrice, attributes, pageable);
        return ResponseEntity.ok(
                ApiResponse.success("Products searched successfully.", products, HttpStatus.OK.value())
        );
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<Page<ProductResponseDTO>>> getProductsByCategory(
            @PathVariable Long categoryId,
            Pageable pageable
    ) {
        Page<ProductResponseDTO> products = productService.getProductsByCategory(categoryId, pageable);
        return ResponseEntity.ok(
                ApiResponse.success("Products for category " + categoryId + " fetched successfully.", products, HttpStatus.OK.value())
        );
    }
}