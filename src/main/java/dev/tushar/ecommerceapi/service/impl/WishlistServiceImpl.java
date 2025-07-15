package dev.tushar.ecommerceapi.service.impl;

import dev.tushar.ecommerceapi.dto.request.AddItemToWishlistRequestDTO;
import dev.tushar.ecommerceapi.dto.response.ProductResponseDTO;
import dev.tushar.ecommerceapi.entity.*;
import dev.tushar.ecommerceapi.exception.ApiException;
import dev.tushar.ecommerceapi.repository.ProductVariantRepository;
import dev.tushar.ecommerceapi.repository.UserRepository;
import dev.tushar.ecommerceapi.security.CustomUserDetails;
import dev.tushar.ecommerceapi.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class WishlistServiceImpl implements WishlistService {

    private final UserRepository userRepository;
    private final ProductVariantRepository productVariantRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getMyWishlist(CustomUserDetails currentUser) {
        User user = userRepository.findById(currentUser.user().getId()).orElseThrow();
        return user.getWishlistItems().stream()
                .map(this::mapVariantToProductResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ProductResponseDTO addItemToWishlist(CustomUserDetails currentUser, AddItemToWishlistRequestDTO request) {
        User user = userRepository.findById(currentUser.user().getId()).orElseThrow();
        ProductVariant productVariant = productVariantRepository.findById(request.productVariantId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product variant not found."));

        user.getWishlistItems().add(productVariant);
        userRepository.save(user);

        return mapVariantToProductResponseDTO(productVariant);
    }

    @Override
    public void removeItemFromWishlist(CustomUserDetails currentUser, Long productVariantId) {
        User user = userRepository.findById(currentUser.user().getId()).orElseThrow();
        user.getWishlistItems().removeIf(variant -> variant.getId().equals(productVariantId));
        userRepository.save(user);
    }

    private ProductResponseDTO mapVariantToProductResponseDTO(ProductVariant variant) {
        Product product = variant.getProduct();
        Category category = product.getCategory();
        Map<String, String> stringAttributes = variant.getAttributes().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));

        return new ProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                new ProductResponseDTO.BusinessInfo(product.getBusiness().getId(), product.getBusiness().getBusinessName()),
                new ProductResponseDTO.CategoryInfo(
                        category.getId(),
                        category.getName()
                ),
                variant.getId(),
                variant.getPrice(),
                variant.getStockQuantity(),
                stringAttributes
        );
    }
}