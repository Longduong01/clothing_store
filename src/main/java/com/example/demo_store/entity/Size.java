package com.example.demo_store.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "Sizes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Size {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "size_id")
    private Long sizeId;
    
    @Column(name = "size_name", unique = true, nullable = false, length = 10)
    private String sizeName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SizeStatus status = SizeStatus.ACTIVE;
    
    @Column(name = "product_count", nullable = false)
    private Integer productCount = 0;
    
    // @OneToMany(mappedBy = "size", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private List<ProductVariant> variants;
    
    public enum SizeStatus {
        ACTIVE, INACTIVE, DISCONTINUED
    }
}
