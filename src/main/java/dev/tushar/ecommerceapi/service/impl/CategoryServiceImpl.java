package dev.tushar.ecommerceapi.service.impl;

import dev.tushar.ecommerceapi.dto.request.CategoryRequestDTO;
import dev.tushar.ecommerceapi.dto.response.CategoryResponseDTO;
import dev.tushar.ecommerceapi.entity.Category;
import dev.tushar.ecommerceapi.exception.ApiException;
import dev.tushar.ecommerceapi.repository.CategoryRepository;
import dev.tushar.ecommerceapi.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public CategoryResponseDTO createCategory(CategoryRequestDTO request) {
        Category parent = null;
        int level = 0;

        // Check if parent category exists
        if (request.parentCategoryId() != null) {
            parent = categoryRepository.findById(request.parentCategoryId())
                    .filter(p -> !p.isDeleted())
                    .orElseThrow(() -> new ApiException(
                            HttpStatus.NOT_FOUND,
                            "Parent category with ID " + request.parentCategoryId() + " not found."
                            )
                    );
            // If exists, get level and add 1 to make it ready for new category
            level = parent.getLevel() + 1;

            // Check all existing siblings, does any o have the same name what we want to create in this parent
            List<Category> siblings = categoryRepository.findDirectChildren(parent.getPath(), level);
            boolean isNameTaken = siblings.stream()
                    .anyMatch(sibling -> sibling.getName().equalsIgnoreCase(request.name()));

            // If yes, throw exception
            if (isNameTaken) {
                throw new ApiException(
                        HttpStatus.CONFLICT,
                        "A category with the name '" + request.name() + "' already exists under this parent."
                );
            }
        } else {
            categoryRepository.findByName(request.name()).ifPresent(c -> {
                if (c.getLevel() == 0 && !c.isDeleted()) {
                    throw new ApiException(
                            HttpStatus.CONFLICT, "A root category with the name '" + request.name() + "' already exists."
                    );
                }
            });
        }

        // Proceed with creation
        Category newCategory = Category.builder()
                .name(request.name())
                .level(level)
                .build();

        newCategory = categoryRepository.save(newCategory);

        String path = (parent != null)
                ? parent.getPath() + newCategory.getId() + "/"
                : newCategory.getId() + "/";
        newCategory.setPath(path);

        return mapToCategoryResponseDTO(newCategory);
    }

    // --- Other methods remain the same ---

    @Override
    public CategoryResponseDTO updateCategory(Long categoryId, CategoryRequestDTO request) {
        Category categoryToUpdate = categoryRepository.findById(categoryId)
                .filter(cat -> !cat.isDeleted())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Category with ID " + categoryId + " not found."));

        if (request.name() != null && !request.name().isBlank()) {
            categoryToUpdate.setName(request.name());
        }
        Category updatedCategory = categoryRepository.save(categoryToUpdate);
        return mapToCategoryResponseDTO(updatedCategory);
    }

    @Override
    public List<CategoryResponseDTO> getAllCategoriesAsHierarchy() {
        List<Category> allCategories = categoryRepository.findByDeletedFalse();
        return buildHierarchy(allCategories);
    }

    @Override
    public CategoryResponseDTO getCategoryById(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .filter(cat -> !cat.isDeleted())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Category with ID " + categoryId + " not found."));
        return mapToCategoryResponseDTO(category);
    }

    @Override
    public void deleteCategory(Long categoryId) {
        Category categoryToDelete = categoryRepository.findById(categoryId)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Category with ID " + categoryId + " not found."));

        // 2. Find the category and its entire branch of descendants.
        List<Category> branch = categoryRepository.findAllByPath(categoryToDelete.getPath());

        // 3. Check if ANY category in the entire branch has products.
        // This is the main safety check.
        for (Category category : branch) {
            // We need to initialize the collection before accessing it
            Hibernate.initialize(category.getProducts());
            if (!category.getProducts().isEmpty()) {
                throw new ApiException(HttpStatus.CONFLICT,
                        "Cannot delete. The category '" + category.getName() + "' (or one of its sub-categories) has products assigned to it.");
            }
        }

        // 4. If the entire branch is product-free, perform a cascading soft-delete.
        for (Category category : branch) {
            category.setDeleted(true);
        }
    }

    private List<CategoryResponseDTO> buildHierarchy(List<Category> categories) {
        Map<Long, CategoryResponseDTO> dtoMap = new HashMap<>();
        List<CategoryResponseDTO> rootCategories = new ArrayList<>();

        for (Category category : categories) {
            Long parentId = getParentIdFromPath(category.getPath());
            dtoMap.put(category.getId(), new CategoryResponseDTO(category.getId(), category.getName(), parentId, new HashSet<>()));
        }

        for (CategoryResponseDTO dto : dtoMap.values()) {
            if (dto.parentId() == null) {
                rootCategories.add(dto);
            } else {
                CategoryResponseDTO parentDto = dtoMap.get(dto.parentId());
                if (parentDto != null) {
                    parentDto.subCategories().add(dto);
                }
            }
        }
        return rootCategories;
    }

    private Long getParentIdFromPath(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        String[] pathParts = path.split("/");
        return (pathParts.length > 1) ? Long.parseLong(pathParts[pathParts.length - 2]) : null;
    }

    private CategoryResponseDTO mapToCategoryResponseDTO(Category category) {
        return new CategoryResponseDTO(category.getId(), category.getName(), getParentIdFromPath(category.getPath()), Collections.emptySet());
    }
}