package dev.tushar.ecommerceapi.specification;

import dev.tushar.ecommerceapi.entity.ProductVariant;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ProductVariantSpecification {

    public Specification<ProductVariant> withFilters(
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Map<String, String> attributes
    ) {
//        We are providing implementation of the Specification interface -
//        @FunctionalInterface
//        public interface Specification<T> {
//            Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder);
//        }
        return (root, query, criteriaBuilder) -> {

            // We will store all the predicates in this list, that will be used to filter the results
            List<Predicate> predicates = new ArrayList<>();

            // criteriaBuilder is a tool that helps us get the predicate of different fields
            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            // If minPrice is provided, adds a "price >= minPrice" condition. (predicate)
            // If maxPrice is provided, adds a "price <= maxPrice" condition. (predicate)

            if (attributes != null && !attributes.isEmpty()) {
                for (Map.Entry<String, String> entry : attributes.entrySet()) {
                    predicates.add(criteriaBuilder.equal(
                            criteriaBuilder.function(
                                    "jsonb_extract_path_text",
                                    String.class,
                                    root.get("attributes"),
                                    criteriaBuilder.literal(entry.getKey())
                            ),
                            entry.getValue()
                    ));
                }
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}