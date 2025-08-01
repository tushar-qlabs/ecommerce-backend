package dev.tushar.ecommerceapi.service.impl;

import dev.tushar.ecommerceapi.dto.request.OptionSetRequestDTO;
import dev.tushar.ecommerceapi.dto.response.OptionSetResponseDTO;
import dev.tushar.ecommerceapi.entity.OptionSet;
import dev.tushar.ecommerceapi.repository.OptionSetRepository;
import dev.tushar.ecommerceapi.service.OptionSetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OptionSetServiceImpl implements OptionSetService {

    private final OptionSetRepository optionSetRepository;

    @Override
    public OptionSetResponseDTO createOptionSet(OptionSetRequestDTO request) {
        OptionSet optionSet = OptionSet.builder()
                .name(request.name())
                .options(request.options())
                .build();
        optionSet = optionSetRepository.save(optionSet);
        return new OptionSetResponseDTO(optionSet.getId(), optionSet.getName(), optionSet.getOptions());
    }

    @Override
    public List<OptionSetResponseDTO> getAllOptionSets() {
        return optionSetRepository.findAll().stream()
                .map(os -> new OptionSetResponseDTO(os.getId(), os.getName(), os.getOptions()))
                .collect(Collectors.toList());
    }
}