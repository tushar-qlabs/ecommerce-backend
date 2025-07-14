package dev.tushar.ecommerceapi.specification;

import dev.tushar.ecommerceapi.entity.Product;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ProductSpecification {

    public Specification<Product> withFilters(
            String q,
            Set<Long> categoryIds,
            Set<Long> productIds
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.isFalse(root.get("isDeleted")));

            if (q != null && !q.isBlank()) {
                String[] searchTokens = q.toLowerCase().split("\\s+");

                // This expression creates a representation of the 'name' column with hyphens removed
                Expression<String> nameWithoutHyphens = criteriaBuilder.function(
                        "REPLACE",
                        String.class,
                        root.get("name"),
                        criteriaBuilder.literal("-"),
                        criteriaBuilder.literal("")
                );

                // We will do the same for the 'description' too
                Expression<String> descriptionWithoutHyphens = criteriaBuilder.function(
                        "REPLACE",
                        String.class,
                        root.get("description"),
                        criteriaBuilder.literal("-"),
                        criteriaBuilder.literal("")
                );

                List<Predicate> tokenPredicates = new ArrayList<>();
                for (String token : searchTokens) {
                    // Also we will remove hyphens from the user's search term
                    // to make the search more flexible
                    String cleanToken = token.replace("-", "");

                    Predicate nameMatch = criteriaBuilder.like(criteriaBuilder.lower(nameWithoutHyphens), "%" + cleanToken + "%");
                    Predicate descriptionMatch = criteriaBuilder.like(criteriaBuilder.lower(descriptionWithoutHyphens), "%" + cleanToken + "%");
                    tokenPredicates.add(criteriaBuilder.or(nameMatch, descriptionMatch));
                }
                predicates.add(criteriaBuilder.and(tokenPredicates.toArray(new Predicate[0])));
            }

            // If a set of category IDs is provided, use a "WHERE IN" clause
            if (categoryIds != null && !categoryIds.isEmpty()) {
                predicates.add(root.get("category").get("id").in(categoryIds));
            }

            if (productIds != null && !productIds.isEmpty()) {
                predicates.add(root.get("id").in(productIds));
            }

            Objects.requireNonNull(query).distinct(true);
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}