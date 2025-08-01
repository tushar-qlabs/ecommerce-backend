package dev.tushar.ecommerceapi.service;

import dev.tushar.ecommerceapi.dto.request.CartItemRequestDTO;
import dev.tushar.ecommerceapi.dto.response.CartResponseDTO;
import dev.tushar.ecommerceapi.security.CustomUserDetails;

public interface CartService {
    CartResponseDTO getMyCart(CustomUserDetails currentUser);
    CartResponseDTO setItemInCart(CustomUserDetails currentUser, CartItemRequestDTO request);
    void clearMyCart(CustomUserDetails currentUser);
}