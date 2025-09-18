package com.example.demo_store.repository;

import com.example.demo_store.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    List<Category> findByParentCategoryId(Long parentId);
    
    List<Category> findByParentIsNull();
}
