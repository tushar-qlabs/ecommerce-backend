package dev.tushar.ecommerceapi.specification;

import dev.tushar.ecommerceapi.entity.Product;
import dev.tushar.ecommerceapi.entity.ProductVariant;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Component
public class ProductSpecification {

    public Specification<ProductVariant> withFilters(
            String q,
            Set<Long> categoryIds,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Map<String, String> attributes
    ) {
        return (root, query, criteriaBuilder) -> {
            // We will first do inner join between ProductVariant and Product
            Join<ProductVariant, Product> productJoin = root.join("product");

            // Now we need a list of predicates to store all the filters we
            // will build using the criteria builder
            List<Predicate> predicates = new ArrayList<>();

            // Filter out variants of soft-deleted products
            predicates.add(criteriaBuilder.isFalse(productJoin.get("isDeleted")));

            // --- 'q' parameter: search on product name/description ---
            if (q != null && !q.isBlank()) {
                String[] searchTokens = q.toLowerCase().split("\\s+");
                Expression<String> nameWithoutHyphens = criteriaBuilder.function("REPLACE", String.class, productJoin.get("name"), criteriaBuilder.literal("-"), criteriaBuilder.literal(""));
                Expression<String> descriptionWithoutHyphens = criteriaBuilder.function("REPLACE", String.class, productJoin.get("description"), criteriaBuilder.literal("-"), criteriaBuilder.literal(""));

                List<Predicate> tokenPredicates = new ArrayList<>();
                for (String token : searchTokens) {
                    String cleanToken = token.replace("-", "");
                    Predicate nameMatch = criteriaBuilder.like(criteriaBuilder.lower(nameWithoutHyphens), "%" + cleanToken + "%");
                    Predicate descriptionMatch = criteriaBuilder.like(criteriaBuilder.lower(descriptionWithoutHyphens), "%" + cleanToken + "%");
                    tokenPredicates.add(criteriaBuilder.or(nameMatch, descriptionMatch));
                }
                predicates.add(criteriaBuilder.and(tokenPredicates.toArray(new Predicate[0])));
            }

            // --- Category Filter (on the parent product) ---
            if (categoryIds != null && !categoryIds.isEmpty()) {
                predicates.add(productJoin.get("category").get("id").in(categoryIds));
            }

            // --- Price Filters (directly on the variant) ---
            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            // --- Attribute Filters (directly on the variant) ---
            if (attributes != null && !attributes.isEmpty()) {
                for (Map.Entry<String, String> entry : attributes.entrySet()) {
                    predicates.add(criteriaBuilder.equal(
                            criteriaBuilder.function("jsonb_extract_path_text", String.class, root.get("attributes"), criteriaBuilder.literal(entry.getKey())),
                            entry.getValue()
                    ));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}