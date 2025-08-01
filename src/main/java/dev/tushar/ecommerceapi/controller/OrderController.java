package dev.tushar.ecommerceapi.controller;

import dev.tushar.ecommerceapi.dto.ApiResponse;
import dev.tushar.ecommerceapi.dto.request.CreateOrderRequestDTO;
import dev.tushar.ecommerceapi.dto.response.OrderResponseDTO;
import dev.tushar.ecommerceapi.dto.response.SellerOrderResponseDTO;
import dev.tushar.ecommerceapi.security.CustomUserDetails;
import dev.tushar.ecommerceapi.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> createOrder(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody CreateOrderRequestDTO request,
            @RequestParam(defaultValue = "true") boolean mockPaymentSuccess) {

        OrderResponseDTO createdOrder = orderService.createOrderFromCart(currentUser, request, mockPaymentSuccess);

        return new ResponseEntity<>(
                ApiResponse.success("Order created successfully.", createdOrder, HttpStatus.CREATED.value()),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<OrderResponseDTO>>> getMyOrders(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        List<OrderResponseDTO> orders = orderService.getMyOrders(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Orders fetched successfully.", orders, HttpStatus.OK.value()));
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> getOrderById(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long orderId) {
        OrderResponseDTO order = orderService.getOrderById(currentUser, orderId);
        return ResponseEntity.ok(ApiResponse.success("Order details fetched successfully.", order, HttpStatus.OK.value()));
    }

    @GetMapping("/seller/me")
    @PreAuthorize("hasAuthority('READ_SELLER_ORDERS')")
    public ResponseEntity<ApiResponse<List<SellerOrderResponseDTO>>> getMySellerOrders(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        List<SellerOrderResponseDTO> sellerOrders = orderService.getMySellerOrders(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Seller orders fetched successfully.", sellerOrders, HttpStatus.OK.value()));
    }
}