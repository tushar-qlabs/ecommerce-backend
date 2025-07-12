package dev.tushar.ecommerceapi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.tushar.ecommerceapi.model.AttributeType;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

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
    @Column(name = "attribute_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttributeType type;

    @ManyToMany(mappedBy = "attributes")
    @JsonIgnore
    private Set<Category> categories = new HashSet<>();

    @OneToMany(mappedBy = "attribute", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<ProductAttributeValue> values = new HashSet<>();
}