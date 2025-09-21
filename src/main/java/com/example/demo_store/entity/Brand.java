package com.example.demo_store.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "Brands")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Brand {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "brand_id")
    private Long brandId;
    
    @Column(name = "brand_name", nullable = false, length = 100)
    private String brandName;
    
    @Column(name = "logo_url", length = 500)
    private String logoUrl;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BrandStatus status = BrandStatus.ACTIVE;
    
    @Column(name = "product_count", nullable = false)
    private Integer productCount = 0;
    
    // @OneToMany(mappedBy = "brand", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private List<Product> products;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum BrandStatus {
        ACTIVE, INACTIVE, DISCONTINUED
    }
}
