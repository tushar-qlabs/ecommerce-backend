package dev.tushar.ecommerceapi.repository;

import dev.tushar.ecommerceapi.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
// No longer needs JpaSpecificationExecutor

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByIdAndIsDeletedFalse(Long id);
}