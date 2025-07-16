package dev.tushar.ecommerceapi.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
    This entity will be used to store the order details
    of the user.
    It will hold:
        - Order ID
        - User ID
        - Shipping Address ID
        - Grand Total
        - Status
 */

@Entity
@Table(name = "orders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_address_id", nullable = false)
    private Address shippingAddress;

    @Column(nullable = false)
    private BigDecimal grandTotal;

    @Column(nullable = false)
    private String status; // Different status: 'Processing', 'Completed', 'Cancelled'

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @Builder.Default
    private List<SellerOrder> sellerOrders = new ArrayList<>();

    // TODO: We will add payment related details later
}