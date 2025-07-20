package dev.tushar.ecommerceapi.specification;

import dev.tushar.ecommerceapi.entity.Category;
import dev.tushar.ecommerceapi.entity.Product;
import dev.tushar.ecommerceapi.entity.ProductVariant;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ProductVariantSpecification {

    // We're here providing the implementation of the Specification<ProductVariant> interface
    // It ia a functional interface that defines an abstract method named
    // toPredicate(Root<ProductVariant> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder)

    // root is our main table; it's a reference to the ProductVariant entity.
    // query is a CriteriaQuery object that represents the query being built. (we're not using it here)
    // criteriaBuilder is a CriteriaBuilder object that helps us build the query.

    public Specification<ProductVariant> withFilters(
            String q,
            Set<Long> categoryIds,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Map<String, String> attributes
    ) {
        return (root, query, criteriaBuilder) -> {

            // First, we need to join ProductVariant to Product
            // this will allow us to access product name and description
            Join<ProductVariant, Product> productJoin = root.join("product");

            // Second, we need to join previously joined table to Category
            // this will allow us to access category name of the product
            Join<Product, Category> categoryJoin = productJoin.join("category");

            // Now we have to make a List<Predicate> (not the predicate from Function, it's from JPA), to collect all the predicates.
            // Predicates are sql conditions that are programmatically made
            // and will be used to make the query later.
            List<Predicate> predicates = new ArrayList<>();

            // This, predicate will make sure that the product which is deleted
            // shouldn't be included in the results
            predicates.add(criteriaBuilder.isFalse(productJoin.get("isDeleted")));

            if (q != null && !q.isBlank()) {

                // Next, we will create a expression that will be used remove the hyphens from name, description and category name for every predicate
                // After that we will lowercase the string.
                Expression<String> nameExpression = criteriaBuilder.lower(
                        criteriaBuilder.function("REPLACE", String.class, productJoin.get("name"), criteriaBuilder.literal("-"), criteriaBuilder.literal(""))
                );
                Expression<String> descriptionExpression = criteriaBuilder.lower(
                        criteriaBuilder.function("REPLACE", String.class, productJoin.get("description"), criteriaBuilder.literal("-"), criteriaBuilder.literal(""))
                );
                Expression<String> categoryNameExpression = criteriaBuilder.lower(
                        criteriaBuilder.function("REPLACE", String.class, categoryJoin.get("name"), criteriaBuilder.literal("-"), criteriaBuilder.literal(""))
                );

                // Now we will split the search query into tokens and create a predicate for each token
                // that should be match against product name, description and category name
                // This predicate list is for tokens matching, while the outer predicate list is for all sort of predicates

                List<Predicate> tokenPredicates = new ArrayList<>();
                String[] searchTokens = q.toLowerCase().split("\\s+");

                for (String token : searchTokens) {

                    // We will also remove the hyphen from the query itself before searching
                    // and make it ready for pattern matching with LIKE clause.

                    String cleanToken = token.replace("-", "");
                    String pattern = "%" + cleanToken + "%";

                    // Now apply the Like pattern with the expression for every filed.
                    // This will return the predicate of it, this will loop for all the tokens.

                    Predicate nameMatch = criteriaBuilder.like(nameExpression, pattern);
                    Predicate descriptionMatch = criteriaBuilder.like(descriptionExpression, pattern);
                    Predicate categoryMatch = criteriaBuilder.like(categoryNameExpression, pattern);

                    // Add the predicate to the list with or operator (if token match in any field)
                    tokenPredicates.add(criteriaBuilder.or(nameMatch, descriptionMatch, categoryMatch));
                }

                // Finally, add all the predicates with and operator...
                // but before we add we need to convert it to array, because and operator expects array
                predicates.add(criteriaBuilder.and(tokenPredicates.toArray(Predicate[]::new)));
            }

            // If user requested to filter by category ids then set these predicate
            if (categoryIds != null && !categoryIds.isEmpty()) {
                predicates.add(productJoin.get("category").get("id").in(categoryIds));
            }

            // If user requested to filter by price then set these predicate
            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            // Next, we also have to filter by attributes
            // For this, we will use jsonb_extract_path_text function which is a native function of Postgres
            // to query the jsonb column.
            if (attributes != null && !attributes.isEmpty()) {
                for (Map.Entry<String, String> entry : attributes.entrySet()) {

                    // We will use jsonb_extract_path_text function to query the jsonb column
                    // String.class is the expected return type
                    // root.get("attributes") is the jsonb column
                    // criteriaBuilder.literal(entry.getKey()) is the key of the attribute
                    // entry.getValue() is the value of the attribute based on which we want to filter

                    Expression<String> jsonAttribute = criteriaBuilder.function(
                            "jsonb_extract_path_text", String.class, root.get("attributes"), criteriaBuilder.literal(entry.getKey())
                    );
                    predicates.add(criteriaBuilder.equal(jsonAttribute, entry.getValue()));
                }
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new)); // Now add all the predicates and return it.
        };
    }
}
