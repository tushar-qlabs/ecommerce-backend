package dev.tushar.ecommerceapi.service;

import dev.tushar.ecommerceapi.dto.response.SessionResponseDTO;
import dev.tushar.ecommerceapi.entity.User;

import java.util.List;
import java.util.UUID;

public interface SessionService {

    public List<SessionResponseDTO> getActiveSessions(User user);
    public void terminateSession(User user, UUID sessionId);
}
