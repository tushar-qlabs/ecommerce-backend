package dev.tushar.ecommerceapi.service;

import dev.tushar.ecommerceapi.dto.request.CreateOrderRequestDTO;
import dev.tushar.ecommerceapi.dto.response.OrderResponseDTO;
import dev.tushar.ecommerceapi.dto.response.SellerOrderResponseDTO;
import dev.tushar.ecommerceapi.security.CustomUserDetails;
import java.util.List;

public interface OrderService {
    OrderResponseDTO createOrderFromCart(CustomUserDetails currentUser, CreateOrderRequestDTO request, boolean isPaymentSuccessful);
    OrderResponseDTO getOrderById(CustomUserDetails currentUser, Long orderId);
    List<OrderResponseDTO> getMyOrders(CustomUserDetails currentUser);
    List<SellerOrderResponseDTO> getMySellerOrders(CustomUserDetails currentUser);
}