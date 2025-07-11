package dev.tushar.ecommerceapi.controller;

import dev.tushar.ecommerceapi.dto.ApiResponse;
import dev.tushar.ecommerceapi.dto.request.CategoryRequestDTO;
import dev.tushar.ecommerceapi.dto.response.CategoryResponseDTO;
import dev.tushar.ecommerceapi.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_CATEGORIES')")
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> createCategory(
            @Valid @RequestBody CategoryRequestDTO request
    ) {
        CategoryResponseDTO newCategory = categoryService.createCategory(request);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Category created successfully.",
                        newCategory,
                        HttpStatus.OK.value()
                )
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponseDTO>>> getAllCategories() {
        List<CategoryResponseDTO> categories = categoryService.getAllCategoriesAsHierarchy();
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Categories fetched successfully.",
                        categories,
                        HttpStatus.OK.value()
                )
        );
    }
}