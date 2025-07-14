package dev.tushar.ecommerceapi.controller;

import dev.tushar.ecommerceapi.dto.ApiResponse;
import dev.tushar.ecommerceapi.dto.request.AttributeRequestDTO;
import dev.tushar.ecommerceapi.dto.request.OptionSetRequestDTO; // New
import dev.tushar.ecommerceapi.dto.response.AttributeResponseDTO;
import dev.tushar.ecommerceapi.dto.response.OptionSetResponseDTO; // New
import dev.tushar.ecommerceapi.service.AttributeService;
import dev.tushar.ecommerceapi.service.OptionSetService; // New
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/attributes")
@RequiredArgsConstructor
public class AttributeController {

    private final AttributeService attributeService;
    private final OptionSetService optionSetService;

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_CATEGORIES')")
    public ResponseEntity<ApiResponse<AttributeResponseDTO>> createAttribute(
            @Valid @RequestBody AttributeRequestDTO request
    ) {
        AttributeResponseDTO newAttribute = attributeService.createAttribute(request);
        return ResponseEntity.ok(
                ApiResponse.success("Attribute created successfully.", newAttribute, HttpStatus.CREATED.value())
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AttributeResponseDTO>>> getAllAttributes() {
        List<AttributeResponseDTO> attributes = attributeService.getAllAttributes();
        return ResponseEntity.ok(
                ApiResponse.success("Attributes fetched successfully.", attributes, HttpStatus.OK.value())
        );
    }

    @PostMapping("/option-sets")
    @PreAuthorize("hasAuthority('MANAGE_CATEGORIES')")
    public ResponseEntity<ApiResponse<OptionSetResponseDTO>> createOptionSet(
            @Valid @RequestBody OptionSetRequestDTO request
    ) {
        OptionSetResponseDTO sets = optionSetService.createOptionSet(request);
        return ResponseEntity.ok(
                ApiResponse.success("Option set created successfully.", sets, HttpStatus.CREATED.value())
        );
    }

    @GetMapping("/option-sets")
    public ResponseEntity<ApiResponse<List<OptionSetResponseDTO>>> getAllOptionSets() {
        List<OptionSetResponseDTO> sets = optionSetService.getAllOptionSets();
        return ResponseEntity.ok(
                ApiResponse.success("Option sets fetched successfully.", sets, HttpStatus.OK.value())
        );
    }
}