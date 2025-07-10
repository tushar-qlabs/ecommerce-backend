// Business.java
package dev.tushar.ecommerceapi.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "businesses")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString   
public class Business {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "business_id")
    private Long id;

    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", unique = true)
    private User user;

    @Column(nullable = false)
    private String businessName;

    @Column(nullable = false)
    private String businessDescription;

    @Column(nullable = false)
    private String verificationStatus; // Pending, Approved, Rejected

    @ToString.Exclude
    @OneToMany(mappedBy = "business", fetch = FetchType.LAZY)
    private Set<Product> products;

}


