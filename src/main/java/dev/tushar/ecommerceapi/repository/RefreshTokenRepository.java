package dev.tushar.ecommerceapi.repository;

import dev.tushar.ecommerceapi.entity.RefreshToken;
import dev.tushar.ecommerceapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    List<RefreshToken> findByUser(User user);
    Optional<RefreshToken> findByToken(String token);
    int deleteAllByExpiryDateBefore(Instant date);
}