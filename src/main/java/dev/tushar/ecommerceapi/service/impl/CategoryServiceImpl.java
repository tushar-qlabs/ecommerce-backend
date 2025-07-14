package dev.tushar.ecommerceapi.service.impl;

import dev.tushar.ecommerceapi.dto.request.CategoryRequestDTO;
import dev.tushar.ecommerceapi.dto.response.CategoryAttributeResponseDTO;
import dev.tushar.ecommerceapi.dto.response.CategoryResponseDTO;
import dev.tushar.ecommerceapi.entity.Attribute;
import dev.tushar.ecommerceapi.entity.Category;
import dev.tushar.ecommerceapi.entity.CategoryAttribute;
import dev.tushar.ecommerceapi.entity.OptionSet;
import dev.tushar.ecommerceapi.exception.ApiException;
import dev.tushar.ecommerceapi.model.AttributeType;
import dev.tushar.ecommerceapi.repository.AttributeRepository;
import dev.tushar.ecommerceapi.repository.CategoryRepository;
import dev.tushar.ecommerceapi.repository.OptionSetRepository;
import dev.tushar.ecommerceapi.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static dev.tushar.ecommerceapi.util.MyUtils.getParentIdFromPath;

import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final AttributeRepository attributeRepository;
    private final OptionSetRepository optionSetRepository;

    @Override
    public CategoryResponseDTO createCategory(CategoryRequestDTO request) {
        Category parent = null;
        int level = 0;

        // We check if the provided parentCategoryId is valid
        if (request.parentCategoryId() != null) {
            parent = categoryRepository.findById(request.parentCategoryId())
                    .filter(p -> !p.isDeleted())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Parent category with ID " + request.parentCategoryId() + " not found."));

            // If parentCategoryId is valid,
            // then get the level of the parent category and increment it by 1.
            // This will be the level of the new category.
            level = parent.getLevel() + 1;
        }

        // CREATING THE NEW CATEGORY [IT'S ATTRIBUTES WILL BE ADDED LATER]
        Category newCategory = Category.builder()
                .name(request.name())
                .level(level)  // If parentCategoryId is null, then the default level 0 will be used
                .build();
        Category savedCategory = categoryRepository.save(newCategory);

        // We will get the path from the parent category and
        // append it with the new category ID
        String path = (parent != null)
                ? parent.getPath() + savedCategory.getId() + "/"        // parentPath + newCategoryId + "/" (child category)
                : savedCategory.getId() + "/";                          // newCategoryId + "/"  (root category)
        savedCategory.setPath(path);

        if (request.categoryAttributes() != null && !request.categoryAttributes().isEmpty()) {
            Set<CategoryAttribute> categoryAttributes = request.categoryAttributes().stream().map(attrDto -> {
                // >>>
                //  attributeId:Long,
                //  attributeType:AttributeType[TEXT, ENUM, BOOLEAN],
                //  optionSetId:Long
                // <<<

                // First, we find the attribute by its ID
                Attribute attribute = attributeRepository.findById(attrDto.attributeId())
                        .orElseThrow(() -> new ApiException(
                                HttpStatus.BAD_REQUEST,
                                "Attribute with ID " + attrDto.attributeId() + " not found.")
                        );

                // Second, we check if the attribute type is ENUM
                OptionSet optionSet = null;
                if (attrDto.attributeType() == AttributeType.ENUM) {

                    // If yes, then it SHOULD have an optionSetId
                    if (attrDto.optionSetId() == null) {
                        throw new ApiException(
                                HttpStatus.BAD_REQUEST, "optionSetId is required for ENUM attribute type."
                        );
                    }

                    // If it has an optionSetId, then we find the optionSet by its ID
                    optionSet = optionSetRepository.findById(attrDto.optionSetId())
                            .orElseThrow(() -> new ApiException(
                                    HttpStatus.BAD_REQUEST, "OptionSet with ID " + attrDto.optionSetId() + " not found.")
                            );
                }

                // Third, we create a new CategoryAttribute and return it
                // So, this will hold all resolved data for the Category attributes
                return CategoryAttribute.builder()
                        .category(savedCategory)
                        .attribute(attribute)
                        .attributeType(attrDto.attributeType())
                        .optionSet(optionSet)
                        .build();
            }).collect(Collectors.toSet()); // This wll get collected into a Set

            // Finally, we add the categoryAttributes to the category
            savedCategory.getCategoryAttributes().addAll(categoryAttributes);
            
            // We don't really need to save the category again with updated attributes
            // Because we already saved it before within the same transaction.
            // So, that EntityManager will handle it automatically.
        }

        // Next, we convert it to CategoryResponseDTO with empty subCategories
        // Because new category doesn't have any subCategories
        return mapToCategoryResponseDTO(savedCategory, new HashSet<>());
    }




    @Override
    public CategoryResponseDTO updateCategory(Long categoryId, CategoryRequestDTO request) {
        Category categoryToUpdate = categoryRepository.findById(categoryId)
                .filter(cat -> !cat.isDeleted())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Category with ID " + categoryId + " not found."));

        if (request.name() != null && !request.name().isBlank()) {
            categoryToUpdate.setName(request.name());
        }
        // TODO: Add logic here to update the categoryAttributes set for the category
        Category updatedCategory = categoryRepository.save(categoryToUpdate);

        // Corrected call to mapToCategoryResponseDTO
        return mapToCategoryResponseDTO(updatedCategory, buildSubCategoryHierarchy(updatedCategory, categoryRepository.findByDeletedFalse()));
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
        return mapToCategoryResponseDTO(category, buildSubCategoryHierarchy(category, categoryRepository.findByDeletedFalse()));
    }

    @Override
    public void deleteCategory(Long categoryId) {
        Category categoryToDelete = categoryRepository.findById(categoryId)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Category with ID " + categoryId + " not found."));

        List<Category> branch = categoryRepository.findAllByPath(categoryToDelete.getPath());

        for (Category category : branch) {
            Hibernate.initialize(category.getProducts());
            if (!category.getProducts().isEmpty()) {
                throw new ApiException(HttpStatus.CONFLICT,
                        "Cannot delete. The category '" + category.getName() + "' (or one of its sub-categories) has products assigned to it.");
            }
        }
        for (Category category : branch) {
            category.setDeleted(true);
        }
    }

    private List<CategoryResponseDTO> buildHierarchy(List<Category> allCategories) {
        Map<Long, CategoryResponseDTO> dtoMap = new HashMap<>();
        for (Category category : allCategories) {
            dtoMap.put(category.getId(), mapToCategoryResponseDTO(category, new HashSet<>()));
        }
        List<CategoryResponseDTO> rootCategories = new ArrayList<>();
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

    private Set<CategoryResponseDTO> buildSubCategoryHierarchy(Category parentCategory, List<Category> allCategories) {
        return allCategories.stream()
                .filter(cat -> {
                    Long parentId = getParentIdFromPath(cat.getPath());
                    return parentId != null && parentId.equals(parentCategory.getId());
                })
                .map(subCat -> mapToCategoryResponseDTO(subCat, buildSubCategoryHierarchy(subCat, allCategories)))
                .collect(Collectors.toSet());
    }

    private CategoryResponseDTO mapToCategoryResponseDTO(Category category, Set<CategoryResponseDTO> subCategories) {
        Set<CategoryAttributeResponseDTO> attributes = category.getCategoryAttributes().stream()
                .map(attr -> {
                    List<String> options = (attr.getOptionSet() != null) ? attr.getOptionSet().getOptions() : null;
                    return new CategoryAttributeResponseDTO(
                            attr.getAttribute().getId(),
                            attr.getAttribute().getName(),
                            attr.getAttributeType(),
                            options
                    );
                })
                .collect(Collectors.toSet());

        // --- New Breadcrumb Logic ---
        List<CategoryResponseDTO.AncestorDTO> breadcrumb = new ArrayList<>();
        if (category.getPath() != null && !category.getPath().isEmpty()) {
            // Parse the path string (e.g., "1/2/3/") into a list of IDs
            List<Long> ancestorIds = Arrays.stream(category.getPath().split("/"))
                    .map(Long::parseLong)
                    .toList();

            // We will now fetch all ancestor categories
            List<Category> ancestors = categoryRepository.findAllById(ancestorIds);
            breadcrumb = ancestors.stream()
                    .map(anc -> new CategoryResponseDTO.AncestorDTO(anc.getId(), anc.getName()))
                    .toList();
        }

        return new CategoryResponseDTO(
                category.getId(),
                category.getName(),
                getParentIdFromPath(category.getPath()),
                breadcrumb,
                attributes,
                subCategories
        );
    }
}