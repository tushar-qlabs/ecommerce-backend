package dev.tushar.ecommerceapi.repository;

import dev.tushar.ecommerceapi.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);

    List<Category> findByDeletedFalse();

    List<Category> findByNameIn(List<String> names);

    /**
     * Finds direct, non deleted child categories based on the parent's path and the children's level.
     * This is an efficient way to check for siblings without loading all categories into memory.
     * Example: parentPath = '1/', childLevel = 1 will find all categories at level 1 whose path starts with '1/'.
     * E.g.,
     * SELECT c FROM Category c WHERE c.path LIKE '1/%' AND c.level = 1 AND c.deleted = false
     */
    @Query("SELECT c FROM Category c WHERE c.path LIKE :parentPath% AND c.level = :childLevel AND c.deleted = false")
    List<Category> findDirectChildren(@Param("parentPath") String parentPath, @Param("childLevel") int childLevel);

    /**
     * Finds all descendant categories (children, grandchildren, etc.) of a given category,
     * including the category itself, based on its path.
     * E.g.,
     * SELECT c FROM Category c WHERE c.path LIKE '1/2/3/%' AND c.level = 1 AND c.deleted = false
     */
    @Query("SELECT c FROM Category c WHERE c.path LIKE :path%")
    List<Category> findAllByPath(@Param("path") String path);

    // I did be honest! This looks trash but it works :D
}