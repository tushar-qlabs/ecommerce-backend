package dev.tushar.ecommerceapi.service;

import dev.tushar.ecommerceapi.dto.request.AddItemToWishlistRequestDTO;
import dev.tushar.ecommerceapi.dto.response.ProductResponseDTO;
import dev.tushar.ecommerceapi.security.CustomUserDetails;
import java.util.List;

public interface WishlistService {
    List<ProductResponseDTO> getMyWishlist(CustomUserDetails currentUser);
    ProductResponseDTO addItemToWishlist(CustomUserDetails currentUser, AddItemToWishlistRequestDTO request);
    void removeItemFromWishlist(CustomUserDetails currentUser, Long productVariantId);
}