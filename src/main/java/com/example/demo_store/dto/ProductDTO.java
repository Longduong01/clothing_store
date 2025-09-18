package com.example.demo_store.dto;

import com.example.demo_store.entity.Product;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    private String categoryName;
    private String brandName;
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
        dto.setPrice(product.getPrice());
        dto.setStatus(product.getStatus().toString());
        dto.setCategoryName(product.getCategory() != null ? product.getCategory().getCategoryName() : null);
        dto.setBrandName(product.getBrand() != null ? product.getBrand().getBrandName() : null);
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        
        return dto;
    }
}
