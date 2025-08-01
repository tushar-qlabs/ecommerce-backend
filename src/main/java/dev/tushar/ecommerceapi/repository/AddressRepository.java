package dev.tushar.ecommerceapi.repository;

import dev.tushar.ecommerceapi.entity.Address;
import dev.tushar.ecommerceapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findAllByUser(User user);
}
