package dev.tushar.ecommerceapi.repository;

import dev.tushar.ecommerceapi.entity.OptionSet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OptionSetRepository extends JpaRepository<OptionSet, Long> {
    Optional<OptionSet> findByName(String name);
}