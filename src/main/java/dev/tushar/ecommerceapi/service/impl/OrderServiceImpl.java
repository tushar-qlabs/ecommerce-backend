package dev.tushar.ecommerceapi.service.impl;

import dev.tushar.ecommerceapi.dto.request.CreateOrderRequestDTO;
import dev.tushar.ecommerceapi.dto.response.AddressResponseDTO;
import dev.tushar.ecommerceapi.dto.response.OrderResponseDTO;
import dev.tushar.ecommerceapi.dto.response.ProductResponseDTO;
import dev.tushar.ecommerceapi.dto.response.SellerOrderResponseDTO;
import dev.tushar.ecommerceapi.entity.*;
import dev.tushar.ecommerceapi.exception.ApiException;
import dev.tushar.ecommerceapi.repository.*;
import dev.tushar.ecommerceapi.security.CustomUserDetails;
import dev.tushar.ecommerceapi.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final ProductVariantRepository productVariantRepository;
    private final BusinessRepository businessRepository;
    private final SellerOrderRepository sellerOrderRepository;

    @Override
    public OrderResponseDTO createOrderFromCart(CustomUserDetails currentUser, CreateOrderRequestDTO request, boolean mockPaymentSuccessful) {
        Cart cart = cartRepository.findByUserId(currentUser.user().getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Cart not found."));

        if (cart.getItems().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot create an order from an empty cart.");
        }

        // We use BigDecimal to avoid floating point precision issues when adding decimal values
        // like 0.1 + 0.2 = 0.3 instead of 0.30000000000000004 (this happens because float and double use binary representation)
        // Additionally, we can't use '*' operator in BigDecimal; so we have to use .multiply() method
        // Also, reduct() first argument is initial value, and second argument we're passing
        // is something similar to BiFunction but both arguments need to be of the same type and return the same type.
        BigDecimal grandTotal = cart.getItems().stream()
                .map(item -> item.getProductVariant().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // This is where we may need to handle payment it...
        if (!mockPaymentSuccessful) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Payment failed.");
        }

        return saveOrderAndClearCart(currentUser.user(), cart, request.shippingAddressId(), grandTotal);
    }

    @Transactional
    public OrderResponseDTO saveOrderAndClearCart(User user, Cart cart, Long shippingAddressId, BigDecimal grandTotal) {
        Address shippingAddress = addressRepository.findById(shippingAddressId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Shipping address not found."));

        for (CartItem item : cart.getItems()) {
            ProductVariant variant = productVariantRepository.findById(item.getProductVariant().getId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product variant not found."));

            if (variant.getStockQuantity() < item.getQuantity()) {
                throw new ApiException(HttpStatus.CONFLICT, "Insufficient stock for product: " + variant.getProduct().getName());
            }
        }

        Order order = Order.builder()
                .user(user)
                .shippingAddress(shippingAddress)
                .grandTotal(grandTotal)
                .status("Processing")
                .build();
        Order savedOrder = orderRepository.save(order);

        Map<Business, List<CartItem>> itemsByBusiness = cart.getItems().stream()
                .collect(Collectors.groupingBy(item -> item.getProductVariant().getProduct().getBusiness()));

        for (Map.Entry<Business, List<CartItem>> entry : itemsByBusiness.entrySet()) {
            SellerOrder sellerOrder = SellerOrder.builder()
                    .order(savedOrder)
                    .business(entry.getKey())
                    .status("Processing")
                    .build();

            List<OrderItem> orderItems = entry.getValue().stream()
                    .map(cartItem -> {
                        ProductVariant variant = cartItem.getProductVariant();
                        variant.setStockQuantity(variant.getStockQuantity() - cartItem.getQuantity());

                        return OrderItem.builder()
                                .sellerOrder(sellerOrder)
                                .productVariant(variant)
                                .quantity(cartItem.getQuantity())
                                .priceAtPurchase(variant.getPrice())
                                .build();
                    })
                    .collect(Collectors.toList());

            sellerOrder.setOrderItems(orderItems);
            savedOrder.getSellerOrders().add(sellerOrder);
        }

        cart.getItems().clear();
        cartRepository.save(cart);

        return mapToOrderResponseDTO(orderRepository.save(savedOrder));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(CustomUserDetails currentUser, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found."));
        if (!order.getUser().getId().equals(currentUser.user().getId())) {
            throw new AccessDeniedException("You do not have permission to view this order.");
        }
        return mapToOrderResponseDTO(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getMyOrders(CustomUserDetails currentUser) {
        List<Order> orders = orderRepository.findByUserId(currentUser.user().getId());
        return orders.stream().map(this::mapToOrderResponseDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SellerOrderResponseDTO> getMySellerOrders(CustomUserDetails currentUser) {
        Business business = businessRepository.findByUserId(currentUser.user().getId())
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "You must have a registered business to view seller orders."));

        List<SellerOrder> sellerOrders = sellerOrderRepository.findByBusinessId(business.getId());

        return sellerOrders.stream()
                .map(this::mapToSellerOrderResponseDTO)
                .collect(Collectors.toList());
    }

    // --- Helper DTO Mappers ---

    private SellerOrderResponseDTO mapToSellerOrderResponseDTO(SellerOrder sellerOrder) {
        List<SellerOrderResponseDTO.OrderItemDTO> itemDTOs = sellerOrder.getOrderItems().stream()
                .map(item -> new SellerOrderResponseDTO.OrderItemDTO(
                        item.getId(),
                        item.getQuantity(),
                        item.getPriceAtPurchase(),
                        mapVariantToProductResponseDTO(item.getProductVariant())
                ))
                .collect(Collectors.toList());

        return new SellerOrderResponseDTO(
                sellerOrder.getId(),
                sellerOrder.getOrder().getId(),
                sellerOrder.getOrder().getCreatedAt(),
                sellerOrder.getStatus(),
                mapToAddressResponseDTO(sellerOrder.getOrder().getShippingAddress()),
                itemDTOs
        );
    }

    private OrderResponseDTO mapToOrderResponseDTO(Order order) {
        List<OrderResponseDTO.OrderItemDTO> allItems = order.getSellerOrders().stream()
                .flatMap(sellerOrder -> sellerOrder.getOrderItems().stream()
                        .map(orderItem -> mapToOrderItemDTO(orderItem, sellerOrder.getBusiness()))
                )
                .collect(Collectors.toList());

        return new OrderResponseDTO(
                order.getId(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getGrandTotal(),
                mapToAddressResponseDTO(order.getShippingAddress()),
                allItems
        );
    }

    private OrderResponseDTO.OrderItemDTO mapToOrderItemDTO(OrderItem orderItem, Business business) {
        return new OrderResponseDTO.OrderItemDTO(
                orderItem.getQuantity(),
                orderItem.getPriceAtPurchase(),
                mapVariantToProductResponseDTO(orderItem.getProductVariant()),
                new OrderResponseDTO.BusinessInfoDTO(business.getBusinessName())
        );
    }

    private AddressResponseDTO mapToAddressResponseDTO(Address address) {
        return new AddressResponseDTO(
                address.getId(), address.getLabel(), address.getStreetLine1(),
                address.getStreetLine2(), address.getCity(), address.getState(),
                address.getPostalCode(), address.getCountryCode(), address.isDefault()
        );
    }

    private ProductResponseDTO mapVariantToProductResponseDTO(ProductVariant variant) {
        Product product = variant.getProduct();
        Map<String, String> stringAttributes = variant.getAttributes().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));

        return new ProductResponseDTO(
                product.getId(), product.getName(), product.getDescription(),
                new ProductResponseDTO.BusinessInfo(product.getBusiness().getId(), product.getBusiness().getBusinessName()),
                new ProductResponseDTO.CategoryInfo(product.getCategory().getId(), product.getCategory().getName()),
                variant.getId(), variant.getPrice(), variant.getStockQuantity(), stringAttributes
        );
    }
}