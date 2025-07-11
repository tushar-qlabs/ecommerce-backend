package dev.tushar.ecommerceapi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "categories",
        indexes = {
                // Adding an index to the path column will dramatically speed up subtree queries
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

    @Column(nullable = false, unique = true)
    private String name;

    /**
     * Stores the materialized path of category IDs (e.g., "1/5/12/").
     * Top-level categories will have a path like "1/".
     */
    private String path;

    /**
     * Stores the depth of the category in the hierarchy.
     * Top-level categories are at level 0.
     * E.g., a category with a path of "1/5/12/" will have a level of 3.
     */

    @Column(nullable = false)
    private int level;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Product> products = new HashSet<>();
}