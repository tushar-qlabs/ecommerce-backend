package dev.tushar.ecommerceapi.service;

import dev.tushar.ecommerceapi.dto.request.AttributeRequestDTO;
import dev.tushar.ecommerceapi.dto.response.AttributeResponseDTO;
import java.util.List;

public interface AttributeService {
    AttributeResponseDTO createAttribute(AttributeRequestDTO request);
    List<AttributeResponseDTO> getAllAttributes();
}