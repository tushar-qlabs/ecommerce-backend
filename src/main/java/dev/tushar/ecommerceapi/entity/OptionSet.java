package dev.tushar.ecommerceapi.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.util.List;

@Entity
@Table(name = "option_sets")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptionSet {

    //    This table is supposed to be used only when the
    //    attribute type is ENUM otherwise no use! HAHA

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // e.g., "Apparel Sizes", "Shoe Sizes"

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<String> options; // e.g., ["S", "M", "L", "XL"]
}