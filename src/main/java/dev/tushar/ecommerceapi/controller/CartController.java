package dev.tushar.ecommerceapi.controller;

import dev.tushar.ecommerceapi.dto.ApiResponse;
import dev.tushar.ecommerceapi.dto.request.CartItemRequestDTO;
import dev.tushar.ecommerceapi.dto.response.CartResponseDTO;
import dev.tushar.ecommerceapi.security.CustomUserDetails;
import dev.tushar.ecommerceapi.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CartResponseDTO>> getMyCart(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        CartResponseDTO cart = cartService.getMyCart(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Cart fetched successfully.", cart, HttpStatus.OK.value()));
    }


    @PutMapping("/me/items")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CartResponseDTO>> setItemInCart(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody CartItemRequestDTO request) {
        CartResponseDTO updatedCart = cartService.setItemInCart(currentUser, request);
        return ResponseEntity.ok(ApiResponse.success("Cart updated successfully.", updatedCart, HttpStatus.OK.value()));
    }

    @DeleteMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> clearMyCart(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        cartService.clearMyCart(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared successfully.", null, HttpStatus.OK.value()));
    }
}