package dev.tushar.ecommerceapi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "categories",
        indexes = {
                @Index(name = "idx_category_path", columnList = "path")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String path;

    @Column(nullable = false)
    private int level;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Product> products = new HashSet<>();

    @Builder.Default
    private boolean deleted = false;
}