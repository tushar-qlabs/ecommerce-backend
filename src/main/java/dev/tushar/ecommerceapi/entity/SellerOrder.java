package dev.tushar.ecommerceapi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

/*
    This entity will split the order into multiple seller orders.
    It will hold:
        - Seller's Order ID
        - Order ID (Main Order)
        - Business ID (Seller)
        - Status (e.g., 'Processing', 'Shipped', 'Delivered')
 */

@Entity
@Table(name = "seller_orders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seller_order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(nullable = false)
    private String status; // e.g., 'Processing', 'Shipped', 'Delivered'

    private String trackingId;

    @OneToMany(mappedBy = "sellerOrder", cascade = CascadeType.ALL)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();
}