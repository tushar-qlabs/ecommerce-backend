package dev.tushar.ecommerceapi.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "attributes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Attribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;
}