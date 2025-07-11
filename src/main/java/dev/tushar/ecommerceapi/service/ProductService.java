package dev.tushar.ecommerceapi.service;

import dev.tushar.ecommerceapi.dto.ApiResponse;
import dev.tushar.ecommerceapi.dto.request.ProductRequestDTO;
import dev.tushar.ecommerceapi.dto.response.ProductResponseDTO;

import java.util.List;

public interface ProductService {

    public ApiResponse<ProductResponseDTO> createProduct(ProductRequestDTO productRequest);
    public ApiResponse<ProductResponseDTO> getProductById(Long productId);
    public ApiResponse<List<ProductResponseDTO>> getAllProducts();
    public ApiResponse<List<ProductResponseDTO>> getAllProductsByBusinessId(Long businessId);
    public ApiResponse<ProductResponseDTO> updateProduct(Long productId, ProductRequestDTO productRequest);
    public ApiResponse<Void> deleteProduct(Long productId);
}
