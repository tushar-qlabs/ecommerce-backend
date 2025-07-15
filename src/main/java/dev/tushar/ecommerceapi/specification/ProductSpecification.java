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

            // Filter out all those variants of soft-deleted products
            predicates.add(criteriaBuilder.isFalse(productJoin.get("isDeleted")));

            // now we will search 'q' aka query parameter in on product name and description
            // for this, we will first tokenize the query string and then search for each token
            // before this, we will also remove hyphens from the query string and from
            // the product's name and description using criteriaBuilder.function
            // which will excute the 'REPLACE' function in the db. This allow better search
            // flexibility.
            //
            // (we are not actually searching right here, only building predicates)

            if (q != null && !q.isBlank()) {
                String[] searchTokens = q.toLowerCase().split("\\s+");

                Expression<String> nameWithoutHyphens = criteriaBuilder.function("REPLACE", String.class, productJoin.get("name"), criteriaBuilder.literal("-"), criteriaBuilder.literal(""));
                Expression<String> descriptionWithoutHyphens = criteriaBuilder.function("REPLACE", String.class, productJoin.get("description"), criteriaBuilder.literal("-"), criteriaBuilder.literal(""));

                // Right here we will collect every token's name and description match predicates
                // For this, we will use the LIKE operator with wildcards on both prefix and suffix of the token.
                List<Predicate> tokenPredicates = new ArrayList<>();
                for (String token : searchTokens) {
                    String cleanToken = token.replace("-", "");
                    Predicate nameMatch = criteriaBuilder.like(criteriaBuilder.lower(nameWithoutHyphens), "%" + cleanToken + "%");
                    Predicate descriptionMatch = criteriaBuilder.like(criteriaBuilder.lower(descriptionWithoutHyphens), "%" + cleanToken + "%");
                    tokenPredicates.add(criteriaBuilder.or(nameMatch, descriptionMatch));
                }
                // here we will convert the Predicate list to an array and then pass it to the
                // criteriaBuilder.and method. (unfortunately, it doesnt take list directly)
                predicates.add(criteriaBuilder.and(tokenPredicates.toArray(new Predicate[0])));
            }

            //  Category Filter (on the parent product)
            if (categoryIds != null && !categoryIds.isEmpty()) {
                predicates.add(productJoin.get("category").get("id").in(categoryIds));
            }

            // Price Filters (directly on the variant)
            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            // Here, now we will make a equality predicate for each attribute
            // This attributes are category specific.
            if (attributes != null && !attributes.isEmpty()) {
                for (Map.Entry<String, String> entry : attributes.entrySet()) {
                    predicates.add(criteriaBuilder.equal(
                            criteriaBuilder.function("jsonb_extract_path_text", String.class, root.get("attributes"), criteriaBuilder.literal(entry.getKey())),
                            entry.getValue()
                    ));
                }
            }

            // here we will convert the main predicate list to an array and then return it.
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}