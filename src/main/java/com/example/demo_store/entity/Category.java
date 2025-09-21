package com.example.demo_store.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@Entity
@Table(name = "Categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;
    
    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;
    
    @Column(name = "image_url", length = 500)
    private String imageUrl;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CategoryStatus status = CategoryStatus.ACTIVE;
    
    @Column(name = "product_count", nullable = false)
    private Integer productCount = 0;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Category parent;
    
    // @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private List<Category> children;
    
    // @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private List<Product> products;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum CategoryStatus {
        ACTIVE, INACTIVE, DISCONTINUED
    }
}
