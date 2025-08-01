package dev.tushar.ecommerceapi.repository;

import dev.tushar.ecommerceapi.entity.Permission;
import dev.tushar.ecommerceapi.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.Set;


public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}