package dev.tushar.ecommerceapi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

/*
    This entity will be used to store the items
    that are supposed to be packed and shipped
    to the customer.
    It will hold:
        - Order Item ID (this table primary key)
        - Seller Order ID (split seller order ID)
        - Product Variant ID (product variant ID that is supposed to be shipped)
        - Quantity (Quantity of that product variant that is supposed to be shipped)
        - Price at the time of purchase (Price of the product variant at the time of purchase)
 */

@Entity
@Table(name = "order_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_order_id", nullable = false)
    private SellerOrder sellerOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal priceAtPurchase;
}