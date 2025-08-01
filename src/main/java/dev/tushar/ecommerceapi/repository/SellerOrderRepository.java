package dev.tushar.ecommerceapi.repository;

import dev.tushar.ecommerceapi.entity.SellerOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SellerOrderRepository extends JpaRepository<SellerOrder, Long> {
    List<SellerOrder> findByBusinessId(Long businessId);
}