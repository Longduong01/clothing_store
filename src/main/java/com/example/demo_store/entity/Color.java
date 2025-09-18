package com.example.demo_store.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "Colors")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Color {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "color_id")
    private Long colorId;
    
    @Column(name = "color_name", unique = true, nullable = false, length = 50)
    private String colorName;
    
    // @OneToMany(mappedBy = "color", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private List<ProductVariant> variants;
}
