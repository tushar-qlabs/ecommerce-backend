package dev.tushar.ecommerceapi.service.impl;

import dev.tushar.ecommerceapi.dto.request.CartItemRequestDTO;
import dev.tushar.ecommerceapi.dto.response.CartResponseDTO;
import dev.tushar.ecommerceapi.dto.response.ProductResponseDTO;
import dev.tushar.ecommerceapi.entity.*;
import dev.tushar.ecommerceapi.exception.ApiException;
import dev.tushar.ecommerceapi.repository.CartItemRepository;
import dev.tushar.ecommerceapi.repository.CartRepository;
import dev.tushar.ecommerceapi.repository.ProductVariantRepository;
import dev.tushar.ecommerceapi.security.CustomUserDetails;
import dev.tushar.ecommerceapi.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository productVariantRepository;

    @Override
    @Transactional(readOnly = true)
    public CartResponseDTO getMyCart(CustomUserDetails currentUser) {
        Cart cart = getOrCreateCart(currentUser.user());
        return mapToCartResponseDTO(cart);
    }

    @Override
    public CartResponseDTO setItemInCart(CustomUserDetails currentUser, CartItemRequestDTO request) {
        Cart cart = getOrCreateCart(currentUser.user());
        Long variantId = request.productVariantId();
        Long quantity = request.quantity();

        if (quantity <= 0) {
            cartItemRepository.findByCartIdAndProductVariantId(cart.getId(), variantId)
                    .ifPresent(cartItemRepository::delete);
        } else {
            ProductVariant variant = productVariantRepository.findById(variantId)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product variant not found."));

            CartItem item = cartItemRepository.findByCartIdAndProductVariantId(cart.getId(), variantId)
                    .orElseGet(() -> CartItem.builder()
                            .cart(cart)
                            .productVariant(variant)
                            .build());

            item.setQuantity(quantity);
            cart.getItems().add(item); // Ensures item is in the cart's collection
            cartItemRepository.save(item);
        }
        return mapToCartResponseDTO(getOrCreateCart(currentUser.user()));
    }

    @Override
    public void clearMyCart(CustomUserDetails currentUser) {
        Cart cart = getOrCreateCart(currentUser.user());
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    // --- Helper Methods ---

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));
    }

    private CartResponseDTO mapToCartResponseDTO(Cart cart) {
        List<CartResponseDTO.CartItemDetailDTO> itemDTOs = cart.getItems().stream()
                .map(item -> new CartResponseDTO.CartItemDetailDTO(
                        item.getId(),
                        item.getQuantity(),
                        mapVariantToProductResponseDTO(item.getProductVariant())
                ))
                .collect(Collectors.toList());

        BigDecimal subtotal = itemDTOs.stream()
                .map(item -> item.product().price().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponseDTO(cart.getId(), cart.getUser().getId(), itemDTOs, subtotal);
    }

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
                stringAttributes
        );
    }
}