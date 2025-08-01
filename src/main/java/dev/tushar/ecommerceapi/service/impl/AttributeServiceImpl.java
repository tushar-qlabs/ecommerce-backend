package dev.tushar.ecommerceapi.service.impl;

import dev.tushar.ecommerceapi.dto.request.AttributeRequestDTO;
import dev.tushar.ecommerceapi.dto.response.AttributeResponseDTO;
import dev.tushar.ecommerceapi.entity.Attribute;
import dev.tushar.ecommerceapi.repository.AttributeRepository;
import dev.tushar.ecommerceapi.service.AttributeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AttributeServiceImpl implements AttributeService {

    private final AttributeRepository attributeRepository;

    @Override
    public AttributeResponseDTO createAttribute(AttributeRequestDTO request) {
        Attribute attribute = Attribute.builder().name(request.name()).build();
        attribute = attributeRepository.save(attribute);
        return new AttributeResponseDTO(attribute.getId(), attribute.getName());
    }

    @Override
    public List<AttributeResponseDTO> getAllAttributes() {
        return attributeRepository.findAll().stream()
                .map(attr -> new AttributeResponseDTO(attr.getId(), attr.getName()))
                .collect(Collectors.toList());
    }
}