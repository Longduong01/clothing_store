package com.example.demo_store.controller;

import com.example.demo_store.entity.ProductVariant;
import com.example.demo_store.entity.Product;
import com.example.demo_store.entity.Color;
import com.example.demo_store.repository.ProductVariantRepository;
import com.example.demo_store.repository.ProductRepository;
import com.example.demo_store.repository.SizeRepository;
import com.example.demo_store.repository.ColorRepository;
import com.example.demo_store.dto.ProductVariantDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/variants")
public class ProductVariantController {
    
    @Autowired
    private ProductVariantRepository variantRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private SizeRepository sizeRepository;
    
    @Autowired
    private ColorRepository colorRepository;
    
    // GET /api/variants/product/{productId} - Lấy danh sách biến thể của sản phẩm
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductVariantDTO>> getVariantsByProduct(@PathVariable Long productId) {
        try {
            List<ProductVariant> variants = variantRepository.findByProductProductId(productId);
            List<ProductVariantDTO> dtos = variants.stream().map(ProductVariantDTO::fromEntity).toList();
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    
    // GET /api/variants/{id} - Lấy biến thể theo ID
    @GetMapping("/{id}")
    public ResponseEntity<ProductVariantDTO> getVariantById(@PathVariable Long id) {
        try {
            Optional<ProductVariant> variant = variantRepository.findById(id);
            return variant.map(v -> ResponseEntity.ok(ProductVariantDTO.fromEntity(v)))
                        .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    
    // POST /api/variants - Tạo biến thể mới
    @PostMapping
    public ResponseEntity<ProductVariantDTO> createVariant(@Valid @RequestBody ProductVariantCreateRequest request) {
        try {
            // Kiểm tra product tồn tại
            Optional<Product> product = productRepository.findById(request.getProductId());
            if (!product.isPresent()) {
                return ResponseEntity.badRequest().build();
            }
            
            // Kiểm tra size tồn tại
            Optional<com.example.demo_store.entity.Size> size = sizeRepository.findById(request.getSizeId());
            if (!size.isPresent()) {
                return ResponseEntity.badRequest().build();
            }
            
            // Kiểm tra color tồn tại
            Optional<Color> color = colorRepository.findById(request.getColorId());
            if (!color.isPresent()) {
                return ResponseEntity.badRequest().build();
            }
            
            // Kiểm tra biến thể đã tồn tại chưa
            List<ProductVariant> existingVariants = variantRepository.findByProductAndSizeAndColor(
                request.getProductId(), request.getSizeId(), request.getColorId());
            if (!existingVariants.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            // Tạo biến thể mới
            ProductVariant variant = new ProductVariant();
            variant.setProduct(product.get());
            variant.setSize(size.get());
            variant.setColor(color.get());
            variant.setSku(request.getSku());
            variant.setPrice(request.getPrice());
            variant.setStock(request.getStock());
            variant.setStatus(ProductVariant.VariantStatus.valueOf(request.getStatus()));
            
            ProductVariant savedVariant = variantRepository.save(variant);
            return ResponseEntity.ok(ProductVariantDTO.fromEntity(savedVariant));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    
    // PUT /api/variants/{id} - Cập nhật biến thể
    @PutMapping("/{id}")
    public ResponseEntity<ProductVariantDTO> updateVariant(@PathVariable Long id, @Valid @RequestBody ProductVariantUpdateRequest request) {
        try {
            Optional<ProductVariant> variantOptional = variantRepository.findById(id);
            if (!variantOptional.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            ProductVariant variant = variantOptional.get();
            
            // Cập nhật thông tin
            variant.setSku(request.getSku());
            variant.setPrice(request.getPrice());
            variant.setStock(request.getStock());
            variant.setStatus(ProductVariant.VariantStatus.valueOf(request.getStatus()));
            
            ProductVariant updatedVariant = variantRepository.save(variant);
            return ResponseEntity.ok(ProductVariantDTO.fromEntity(updatedVariant));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    
    // DELETE /api/variants/{id} - Xóa biến thể
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVariant(@PathVariable Long id) {
        try {
            if (variantRepository.existsById(id)) {
                variantRepository.deleteById(id);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    
    // GET /api/variants/sku/{sku} - Lấy biến thể theo SKU (cho validation)
    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductVariantDTO> getVariantBySku(@PathVariable String sku) {
        try {
            Optional<ProductVariant> variant = variantRepository.findBySku(sku);
            return variant.map(v -> ResponseEntity.ok(ProductVariantDTO.fromEntity(v)))
                        .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    
    // Request/Response classes
    public static class ProductVariantCreateRequest {
        @NotNull(message = "Product ID is required")
        private Long productId;
        
        @NotNull(message = "Size ID is required")
        private Long sizeId;
        
        @NotNull(message = "Color ID is required")
        private Long colorId;
        
        @NotBlank(message = "SKU is required")
        @jakarta.validation.constraints.Size(max = 100, message = "SKU must not exceed 100 characters")
        private String sku;
        
        @NotNull(message = "Price is required")
        private BigDecimal price;
        
        @NotNull(message = "Stock is required")
        @Min(value = 0, message = "Stock must be non-negative")
        private Integer stock;
        
        private String status = "ACTIVE";
        
        // Getters and setters
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        
        public Long getSizeId() { return sizeId; }
        public void setSizeId(Long sizeId) { this.sizeId = sizeId; }
        
        public Long getColorId() { return colorId; }
        public void setColorId(Long colorId) { this.colorId = colorId; }
        
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        
        public Integer getStock() { return stock; }
        public void setStock(Integer stock) { this.stock = stock; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
    
    public static class ProductVariantUpdateRequest {
        @NotBlank(message = "SKU is required")
        @jakarta.validation.constraints.Size(max = 100, message = "SKU must not exceed 100 characters")
        private String sku;
        
        @NotNull(message = "Price is required")
        private BigDecimal price;
        
        @NotNull(message = "Stock is required")
        @Min(value = 0, message = "Stock must be non-negative")
        private Integer stock;
        
        private String status;
        
        // Getters and setters
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        
        public Integer getStock() { return stock; }
        public void setStock(Integer stock) { this.stock = stock; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
