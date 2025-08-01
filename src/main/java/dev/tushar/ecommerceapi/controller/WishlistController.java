package dev.tushar.ecommerceapi.controller;

import dev.tushar.ecommerceapi.dto.ApiResponse;
import dev.tushar.ecommerceapi.dto.request.AddItemToWishlistRequestDTO;
import dev.tushar.ecommerceapi.dto.response.ProductResponseDTO;
import dev.tushar.ecommerceapi.security.CustomUserDetails;
import dev.tushar.ecommerceapi.service.WishlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ProductResponseDTO>>> getMyWishlist(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        List<ProductResponseDTO> wishlist = wishlistService.getMyWishlist(currentUser);
        return ResponseEntity.ok(
                ApiResponse.success("Wishlist fetched successfully.", wishlist, HttpStatus.OK.value())
        );
    }

    @PostMapping("/me/items")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> addItemToWishlist(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody AddItemToWishlistRequestDTO request
    ) {
        ProductResponseDTO newItem = wishlistService.addItemToWishlist(currentUser, request);
        return ResponseEntity.ok(
                ApiResponse.success("Item added to wishlist successfully.", newItem, HttpStatus.CREATED.value())
        );
    }

    @DeleteMapping("/me/items/{variantId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> removeItemFromWishlist(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long variantId
    ) {
        wishlistService.removeItemFromWishlist(currentUser, variantId);
        return ResponseEntity.ok(
                ApiResponse.success("Item removed from wishlist successfully.", null, HttpStatus.OK.value())
        );
    }
}