package dev.tushar.ecommerceapi.service.impl;

import dev.tushar.ecommerceapi.dto.request.CategoryRequestDTO;
import dev.tushar.ecommerceapi.dto.response.CategoryResponseDTO;
import dev.tushar.ecommerceapi.entity.Category;
import dev.tushar.ecommerceapi.exception.ApiException;
import dev.tushar.ecommerceapi.repository.CategoryRepository;
import dev.tushar.ecommerceapi.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public CategoryResponseDTO createCategory(CategoryRequestDTO request) {
        Category parent = null;
        int level = 0;

        if (request.parentCategoryId() != null) {
            parent = categoryRepository.findById(request.parentCategoryId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Parent category with ID " + request.parentCategoryId() + " not found."));
            level = parent.getLevel() + 1;
        }

        Category newCategory = Category.builder()
                .name(request.name())
                .level(level)
                .build();

        // Save first to get the ID
        newCategory = categoryRepository.save(newCategory);

        String path;
        if (parent != null) {
            path = parent.getPath() + newCategory.getId() + "/";
        } else {
            path = newCategory.getId() + "/";
        }
        newCategory.setPath(path);

        // Save again to update path
        categoryRepository.save(newCategory);

        return mapToCategoryResponseDTO(newCategory);
    }

    @Override
    public List<CategoryResponseDTO> getAllCategoriesAsHierarchy() {
        List<Category> allCategories = categoryRepository.findAll();
        return buildHierarchy(allCategories);
    }

    private List<CategoryResponseDTO> buildHierarchy(List<Category> categories) {
        Map<Long, CategoryResponseDTO> dtoMap = new HashMap<>();
        List<CategoryResponseDTO> rootCategories = new ArrayList<>();

        // First pass: Create all DTOs and put them in a map for easy access.
        for (Category category : categories) {
            Long parentId = getParentIdFromPath(category.getPath());
            dtoMap.put(
                    category.getId(),
                    new CategoryResponseDTO(category.getId(), category.getName(), parentId, new HashSet<>())
            );
        }

        // Second pass: Link children to their parents.
        for (CategoryResponseDTO dto : dtoMap.values()) {
            if (dto.parentId() == null) {
                rootCategories.add(dto); // This is our top-level category.
            } else {
                CategoryResponseDTO parentDto = dtoMap.get(dto.parentId()); // This is our sub-category, find its parent in the map and add it.
                if (parentDto != null) {
                    parentDto.subCategories().add(dto);
                }
            }
        }
        return rootCategories;
    }

    //  Extract parent ID from a path string.
    private Long getParentIdFromPath(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        String[] pathParts = path.split("/");
        if (pathParts.length > 1) { // This way we're excluding those categories that have no parent
            return Long.parseLong(pathParts[pathParts.length - 2]); // This is the parent id of the category with the given path
        }
        return null;

        /*
            Path: 1/
                Parent ID: null
            Path: 1/4/
                Parent ID: 1
            Path: 1/4/11/
                Parent ID: 4
            Path: 12/
                Parent ID: null
            Path: 12/13/
                Parent ID: 12
            Path: 12/14/
                Parent ID: 12
            Path: 1/17/
                Parent ID: 1
        */
    }

    private CategoryResponseDTO mapToCategoryResponseDTO(Category category) {
        return new CategoryResponseDTO(category.getId(), category.getName(), getParentIdFromPath(category.getPath()), Collections.emptySet());
    }
}

