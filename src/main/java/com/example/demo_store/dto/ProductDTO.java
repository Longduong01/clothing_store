package com.example.demo_store.dto;

import com.example.demo_store.entity.Product;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    
    private Long productId;
    private String productName;
    private String description;
    private String sku;
    private BigDecimal price;
    private String status;
    private GenderRef gender;
    private String categoryName;
    private String brandName;
    private Integer totalStock; // Tổng tồn kho từ variants
    
    // Multiple categories support
    private List<CategoryRef> categories;
    private String categoriesString; // Comma-separated category names for display
    
    // Nested objects for frontend compatibility
    private CategoryRef category;
    private BrandRef brand;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static ProductDTO fromEntity(Product product) {
        if (product == null) {
            return null;
        }
        
        ProductDTO dto = new ProductDTO();
        dto.setProductId(product.getProductId());
        dto.setProductName(product.getProductName());
        dto.setDescription(product.getDescription());
        dto.setSku(product.getSku());
        dto.setPrice(null); // Price is now managed at variant level
        dto.setStatus(product.getStatus().toString());
        // Set gender
        if (product.getGender() != null) {
            GenderRef genderRef = new GenderRef();
            genderRef.setGenderId(product.getGender().getGenderId());
            genderRef.setGenderName(product.getGender().getGenderName());
            genderRef.setGenderCode(product.getGender().getGenderCode());
            genderRef.setDescription(product.getGender().getDescription());
            dto.setGender(genderRef);
        }
        dto.setCategoryName(product.getCategory() != null ? product.getCategory().getCategoryName() : null);
        dto.setBrandName(product.getBrand() != null ? product.getBrand().getBrandName() : null);
        
        // Set multiple categories
        if (product.getCategories() != null && !product.getCategories().isEmpty()) {
            List<CategoryRef> categoryRefs = product.getCategories().stream()
                .map(cat -> {
                    CategoryRef categoryRef = new CategoryRef();
                    categoryRef.setCategoryId(cat.getCategoryId());
                    categoryRef.setCategoryName(cat.getCategoryName());
                    categoryRef.setImageUrl(cat.getImageUrl());
                    categoryRef.setDescription(cat.getDescription());
                    return categoryRef;
                })
                .collect(Collectors.toList());
            dto.setCategories(categoryRefs);
            
            // Set comma-separated category names for display
            String categoriesString = product.getCategories().stream()
                .map(cat -> cat.getCategoryName())
                .collect(Collectors.joining(", "));
            dto.setCategoriesString(categoriesString);
        }
        
        // Set nested objects for frontend compatibility
        if (product.getCategory() != null) {
            CategoryRef categoryRef = new CategoryRef();
            categoryRef.setCategoryId(product.getCategory().getCategoryId());
            categoryRef.setCategoryName(product.getCategory().getCategoryName());
            categoryRef.setImageUrl(product.getCategory().getImageUrl());
            categoryRef.setDescription(product.getCategory().getDescription());
            dto.setCategory(categoryRef);
        }
        
        if (product.getBrand() != null) {
            BrandRef brandRef = new BrandRef();
            brandRef.setBrandId(product.getBrand().getBrandId());
            brandRef.setBrandName(product.getBrand().getBrandName());
            brandRef.setLogoUrl(product.getBrand().getLogoUrl());
            brandRef.setDescription(product.getBrand().getDescription());
            dto.setBrand(brandRef);
        }
        
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        
        return dto;
    }
    
    // Inner classes for nested objects
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryRef {
        private Long categoryId;
        private String categoryName;
        private String imageUrl;
        private String description;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BrandRef {
        private Long brandId;
        private String brandName;
        private String logoUrl;
        private String description;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenderRef {
        private Long genderId;
        private String genderName;
        private String genderCode;
        private String description;
    }
}
