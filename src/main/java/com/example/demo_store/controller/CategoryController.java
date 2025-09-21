package com.example.demo_store.controller;

import com.example.demo_store.entity.Category;
import com.example.demo_store.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    // GET /api/categories - Lấy tất cả categories (mặc định chỉ active)
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories(@RequestParam(defaultValue = "false") boolean includeInactive) {
        try {
            List<Category> categories;
            if (includeInactive) {
                categories = categoryRepository.findAll();
            } else {
                categories = categoryRepository.findByStatus(Category.CategoryStatus.ACTIVE);
            }
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // GET /api/categories/{id} - Lấy category theo ID
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        try {
            Optional<Category> category = categoryRepository.findById(id);
            return category.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // GET /api/categories/parent/{parentId} - Lấy categories theo parent
    @GetMapping("/parent/{parentId}")
    public ResponseEntity<List<Category>> getCategoriesByParent(@PathVariable Long parentId) {
        try {
            List<Category> categories = categoryRepository.findByParentCategoryId(parentId);
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // GET /api/categories/root - Lấy categories gốc (không có parent, chỉ active)
    @GetMapping("/root")
    public ResponseEntity<List<Category>> getRootCategories(@RequestParam(defaultValue = "false") boolean includeInactive) {
        try {
            List<Category> categories;
            if (includeInactive) {
                categories = categoryRepository.findByParentIsNull();
            } else {
                categories = categoryRepository.findByStatusAndParentIsNull(Category.CategoryStatus.ACTIVE);
            }
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // POST /api/categories - Tạo category mới
    @PostMapping
    public ResponseEntity<Category> createCategory(@RequestBody CategoryCreateRequest request) {
        try {
            Category category = new Category();
            category.setCategoryName(request.getCategoryName());
            category.setDescription(request.getDescription());
            category.setImageUrl(request.getImageUrl());
            
            // Set parent category if provided
            if (request.getParentId() != null) {
                Optional<Category> parentCategory = categoryRepository.findById(request.getParentId());
                if (parentCategory.isPresent()) {
                    category.setParent(parentCategory.get());
                }
            }
            
            Category savedCategory = categoryRepository.save(category);
            return ResponseEntity.ok(savedCategory);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // PUT /api/categories/{id} - Cập nhật category
    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @RequestBody CategoryCreateRequest request) {
        try {
            System.out.println("Updating category ID: " + id);
            System.out.println("Request data: " + request.getCategoryName() + ", parentId: " + request.getParentId());
            
            Optional<Category> categoryOptional = categoryRepository.findById(id);
            if (categoryOptional.isPresent()) {
                Category category = categoryOptional.get();
                category.setCategoryName(request.getCategoryName());
                category.setDescription(request.getDescription());
                category.setImageUrl(request.getImageUrl());
                
                // Update parent category if provided
                if (request.getParentId() != null && request.getParentId() > 0) {
                    Optional<Category> parentCategory = categoryRepository.findById(request.getParentId());
                    if (parentCategory.isPresent()) {
                        category.setParent(parentCategory.get());
                        System.out.println("Set parent to: " + parentCategory.get().getCategoryName());
                    } else {
                        System.out.println("Parent category not found with ID: " + request.getParentId());
                    }
                } else {
                    category.setParent(null);
                    System.out.println("Set parent to null");
                }
                
                // Update status if provided
                if (request.getStatus() != null) {
                    category.setStatus(Category.CategoryStatus.valueOf(request.getStatus()));
                }
                
                Category updatedCategory = categoryRepository.save(category);
                System.out.println("Updated category: " + updatedCategory.getCategoryName() + 
                                 ", parent: " + (updatedCategory.getParent() != null ? updatedCategory.getParent().getCategoryName() : "null"));
                return ResponseEntity.ok(updatedCategory);
            } else {
                System.out.println("Category not found with ID: " + id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.out.println("Error updating category: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    
    // DELETE /api/categories/{id} - Soft delete category (set status to INACTIVE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        try {
            Optional<Category> categoryOptional = categoryRepository.findById(id);
            if (categoryOptional.isPresent()) {
                Category category = categoryOptional.get();
                category.setStatus(Category.CategoryStatus.INACTIVE);
                categoryRepository.save(category);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // GET /api/categories/name/{name} - Lấy category theo tên (phục vụ unique check FE)
    @GetMapping("/name/{name}")
    public ResponseEntity<Category> getCategoryByName(@PathVariable String name) {
        try {
            Optional<Category> category = categoryRepository.findByCategoryName(name);
            return category.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // GET /api/categories/count - Đếm số lượng categories
    @GetMapping("/count")
    public ResponseEntity<Long> getCategoryCount() {
        try {
            long count = categoryRepository.count();
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // DTO class for Category creation/update
    public static class CategoryCreateRequest {
        private String categoryName;
        private String description;
        private String imageUrl;
        private Long parentId;
        private String status;
        
        // Constructors
        public CategoryCreateRequest() {}
        
        public CategoryCreateRequest(String categoryName, String description, String imageUrl, Long parentId) {
            this.categoryName = categoryName;
            this.description = description;
            this.imageUrl = imageUrl;
            this.parentId = parentId;
        }
        
        // Getters and setters
        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        
        public Long getParentId() { return parentId; }
        public void setParentId(Long parentId) { this.parentId = parentId; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
