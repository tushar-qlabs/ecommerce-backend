package dev.tushar.ecommerceapi.service.impl;

import dev.tushar.ecommerceapi.dto.response.SessionResponseDTO;
import dev.tushar.ecommerceapi.entity.RefreshToken;
import dev.tushar.ecommerceapi.entity.User;
import dev.tushar.ecommerceapi.exception.ApiException;
import dev.tushar.ecommerceapi.repository.RefreshTokenRepository;
import dev.tushar.ecommerceapi.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SessionServiceImpl implements SessionService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SessionResponseDTO> getActiveSessions(User user) {
        return refreshTokenRepository.findByUser(user).stream()
                .map(this::mapToSessionResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * This method contains the core logic for terminating a session.
     */
    @Override
    public void terminateSession(User user, UUID sessionId) {
        // Find the session by its public ID.
        RefreshToken refreshToken = refreshTokenRepository.findById(sessionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Session not found."));

        // Security Check: Verify the session belongs to the user trying to delete it.
        if (!refreshToken.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to terminate this session.");
        }

        // Delete the refresh token from the database, effectively invalidating the session.
        refreshTokenRepository.delete(refreshToken);
    }

    private SessionResponseDTO mapToSessionResponseDTO(RefreshToken refreshToken) {
        return new SessionResponseDTO(
                refreshToken.getId(),
                refreshToken.getDeviceInfo(),
                refreshToken.getIpAddress(),
                refreshToken.getCreatedAt()
        );
    }
}