package dev.tushar.ecommerceapi.service;

import dev.tushar.ecommerceapi.dto.request.CategoryRequestDTO;
import dev.tushar.ecommerceapi.dto.response.CategoryResponseDTO;

import java.util.List;

public interface CategoryService {
    CategoryResponseDTO createCategory(CategoryRequestDTO request);

    List<CategoryResponseDTO> getAllCategoriesAsHierarchy();

    CategoryResponseDTO getCategoryById(Long categoryId);

    CategoryResponseDTO updateCategory(Long categoryId, CategoryRequestDTO request);

    void deleteCategory(Long categoryId);
}