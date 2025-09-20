package com.example.demo_store.controller;

import com.example.demo_store.entity.ProductVariant;
import com.example.demo_store.entity.Product;
import com.example.demo_store.entity.Color;
import com.example.demo_store.repository.ProductVariantRepository;
import com.example.demo_store.repository.ProductRepository;
import com.example.demo_store.repository.SizeRepository;
import com.example.demo_store.repository.ColorRepository;
import com.example.demo_store.dto.ProductVariantDTO;
import com.example.demo_store.service.ProductStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    
    @Autowired
    private ProductStatusService productStatusService;
    
    // Upload directory
    private static final String UPLOAD_DIR = "uploads/variants/";
    
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
    
    // POST /api/variants - Tạo biến thể mới (multipart/form-data)
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ProductVariantDTO> createVariant(
            @RequestParam("productId") Long productId,
            @RequestParam("sizeId") Long sizeId,
            @RequestParam("colorId") Long colorId,
            @RequestParam(value = "sku", required = false) String sku,
            @RequestParam("price") BigDecimal price,
            @RequestParam("stock") Integer stock,
            @RequestParam(value = "status", defaultValue = "ACTIVE") String status,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        try {
            // Kiểm tra product tồn tại
            Optional<Product> product = productRepository.findById(productId);
            if (!product.isPresent()) {
                return ResponseEntity.badRequest().build();
            }
            
            // Kiểm tra size tồn tại
            Optional<com.example.demo_store.entity.Size> size = sizeRepository.findById(sizeId);
            if (!size.isPresent()) {
                return ResponseEntity.badRequest().build();
            }
            
            // Kiểm tra color tồn tại
            Optional<Color> color = colorRepository.findById(colorId);
            if (!color.isPresent()) {
                return ResponseEntity.badRequest().build();
            }
            
            // Kiểm tra biến thể đã tồn tại chưa
            List<ProductVariant> existingVariants = variantRepository.findByProductAndSizeAndColor(
                productId, sizeId, colorId);
            if (!existingVariants.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            // Auto-generate SKU nếu không có
            if (sku == null || sku.trim().isEmpty()) {
                sku = generateSku(product.get().getSku(), color.get().getColorName(), size.get().getSizeName());
            }
            
            // Kiểm tra SKU unique
            if (variantRepository.findBySku(sku).isPresent()) {
                return ResponseEntity.badRequest().build();
            }
            
            // Xử lý upload ảnh
            String imagePath = null;
            if (image != null && !image.isEmpty()) {
                imagePath = saveImage(image);
            }
            
            // Tạo biến thể mới
            ProductVariant variant = new ProductVariant();
            variant.setProduct(product.get());
            variant.setSize(size.get());
            variant.setColor(color.get());
            variant.setSku(sku);
            variant.setPrice(price);
            variant.setStock(stock);
            variant.setStatus(ProductVariant.VariantStatus.valueOf(status));
            variant.setImagePath(imagePath);
            
            ProductVariant savedVariant = variantRepository.save(variant);
            
            // Tự động cập nhật trạng thái sản phẩm
            productStatusService.updateProductStatusBasedOnVariants(productId);
            
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
            
            // Kiểm tra biến thể đã tồn tại chưa (nếu thay đổi size/color)
            if (!variant.getSize().getSizeId().equals(request.getSizeId()) || 
                !variant.getColor().getColorId().equals(request.getColorId())) {
                List<ProductVariant> existingVariants = variantRepository.findByProductAndSizeAndColor(
                    variant.getProduct().getProductId(), request.getSizeId(), request.getColorId());
                if (!existingVariants.isEmpty() && !existingVariants.get(0).getVariantId().equals(id)) {
                    return ResponseEntity.badRequest().build();
                }
            }
            
            // Cập nhật thông tin
            variant.setSku(request.getSku());
            variant.setSize(size.get());
            variant.setColor(color.get());
            variant.setPrice(request.getPrice());
            variant.setStock(request.getStock());
            variant.setStatus(ProductVariant.VariantStatus.valueOf(request.getStatus()));
            
            ProductVariant updatedVariant = variantRepository.save(variant);
            
            // Tự động cập nhật trạng thái sản phẩm
            productStatusService.updateProductStatusBasedOnVariants(variant.getProduct().getProductId());
            
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
            Optional<ProductVariant> variantOptional = variantRepository.findById(id);
            if (variantOptional.isPresent()) {
                ProductVariant variant = variantOptional.get();
                Long productId = variant.getProduct().getProductId();
                
                variantRepository.deleteById(id);
                
                // Tự động cập nhật trạng thái sản phẩm
                productStatusService.updateProductStatusBasedOnVariants(productId);
                
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
    
    // POST /api/variants/update-product-statuses - Cập nhật trạng thái tất cả sản phẩm
    @PostMapping("/update-product-statuses")
    public ResponseEntity<String> updateAllProductStatuses() {
        try {
            productStatusService.updateAllProductStatuses();
            return ResponseEntity.ok("Đã cập nhật trạng thái tất cả sản phẩm thành công");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Lỗi khi cập nhật trạng thái sản phẩm");
        }
    }
    
    // Helper methods
    private String generateSku(String productCode, String colorName, String sizeName) {
        // Normalize color name (remove accents, convert to uppercase)
        String normalizedColor = normalizeString(colorName).toUpperCase();
        String normalizedSize = sizeName.toUpperCase();
        return productCode + "-" + normalizedColor + "-" + normalizedSize;
    }
    
    private String normalizeString(String input) {
        if (input == null) return "";
        // Remove accents and special characters
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized.replaceAll("[^\\p{ASCII}]", "")
                        .replaceAll("[^a-zA-Z0-9]", "")
                        .toUpperCase();
    }
    
    private String saveImage(MultipartFile image) throws IOException {
        // Create upload directory if not exists
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate unique filename
        String originalFilename = image.getOriginalFilename();
        String extension = originalFilename != null ? 
            originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        String filename = UUID.randomUUID().toString() + extension;
        
        // Save file
        Path filePath = uploadPath.resolve(filename);
        Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        return UPLOAD_DIR + filename;
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
        
        @NotNull(message = "Size ID is required")
        private Long sizeId;
        
        @NotNull(message = "Color ID is required")
        private Long colorId;
        
        @NotNull(message = "Price is required")
        private BigDecimal price;
        
        @NotNull(message = "Stock is required")
        @Min(value = 0, message = "Stock must be non-negative")
        private Integer stock;
        
        private String status;
        
        // Getters and setters
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        
        public Long getSizeId() { return sizeId; }
        public void setSizeId(Long sizeId) { this.sizeId = sizeId; }
        
        public Long getColorId() { return colorId; }
        public void setColorId(Long colorId) { this.colorId = colorId; }
        
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        
        public Integer getStock() { return stock; }
        public void setStock(Integer stock) { this.stock = stock; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
