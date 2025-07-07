// Address.java
package dev.tushar.ecommerceapi.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "addresses",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "label"})
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private String streetLine1;

    private String streetLine2;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String state;

    @Column(nullable = false)
    private String postalCode;

    @Column(nullable = false)
    private String countryCode;
}