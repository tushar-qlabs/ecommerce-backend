package dev.tushar.ecommerceapi.service;

import dev.tushar.ecommerceapi.dto.request.OptionSetRequestDTO;
import dev.tushar.ecommerceapi.dto.response.OptionSetResponseDTO;
import java.util.List;

public interface OptionSetService {
    OptionSetResponseDTO createOptionSet(OptionSetRequestDTO request);
    List<OptionSetResponseDTO> getAllOptionSets();
}