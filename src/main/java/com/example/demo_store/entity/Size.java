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
    
    // @OneToMany(mappedBy = "size", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private List<ProductVariant> variants;
}
