package dev.tushar.ecommerceapi.repository;

import dev.tushar.ecommerceapi.entity.Attribute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttributeRepository extends JpaRepository<Attribute, Long> {
    Optional<Attribute> findByName(String name);
}