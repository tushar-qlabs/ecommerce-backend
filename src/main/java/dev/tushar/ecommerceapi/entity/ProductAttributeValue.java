package dev.tushar.ecommerceapi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "product_attribute_values",
        // Ensures a product can't have the same attribute twice
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"product_id", "attribute_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductAttributeValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "value_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    @JsonIgnore
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attribute_id")
    private Attribute attribute;

    @Column(nullable = false)
    private String value;
}