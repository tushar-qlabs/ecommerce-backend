package dev.tushar.ecommerceapi.repository;

import dev.tushar.ecommerceapi.entity.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BusinessRepository extends JpaRepository<Business, Long> {
    boolean existsByUserId(Long userId);
    Optional<Business> findByUserId(Long userId);
}
