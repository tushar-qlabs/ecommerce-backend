package dev.tushar.ecommerceapi.dto.response;

import java.time.Instant;
import java.util.UUID;

public record SessionResponseDTO(
        UUID id,
        String deviceInfo,
        String ipAddress,
        Instant createdAt
) {}