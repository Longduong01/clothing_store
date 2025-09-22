package com.example.demo_store.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

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
    
    @Column(name = "color_code", length = 7)
    private String colorCode; // Hex color code (#FF0000)
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ColorStatus status = ColorStatus.ACTIVE;
    
    @Column(name = "product_count", nullable = false)
    private Integer productCount = 0;
    
    @OneToMany(mappedBy = "color", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"color"})
    private List<ColorImage> images;
    
    // @OneToMany(mappedBy = "color", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private List<ProductVariant> variants;
    
    public enum ColorStatus {
        ACTIVE, INACTIVE, DISCONTINUED
    }
}
