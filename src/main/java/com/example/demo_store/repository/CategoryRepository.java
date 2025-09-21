package com.example.demo_store.repository;

import com.example.demo_store.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    List<Category> findByParentCategoryId(Long parentId);
    
    List<Category> findByParentIsNull();
    
    Optional<Category> findByCategoryName(String categoryName);
    
    // Soft delete support
    List<Category> findByStatus(Category.CategoryStatus status);
    
    List<Category> findByStatusAndParentIsNull(Category.CategoryStatus status);
    
    List<Category> findByStatusAndParentCategoryId(Category.CategoryStatus status, Long parentId);
}
