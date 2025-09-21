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
import java.util.ArrayList;
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
    
    @Autowired
    private com.example.demo_store.service.ProductCountService productCountService;
    
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
            @RequestParam("price") String priceStr,
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
            
            // Convert price string to BigDecimal
            BigDecimal price;
            try {
                price = new BigDecimal(priceStr);
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().build();
            }
            
            // Kiểm tra biến thể đã tồn tại chưa
            List<ProductVariant> existingVariants = variantRepository.findByProductAndSizeAndColor(
                productId, sizeId, colorId);
            if (!existingVariants.isEmpty()) {
                return ResponseEntity.status(400).body(null);
            }
            
            // Luôn tự động tạo SKU
            String sku = generateSku(product.get().getSku(), color.get().getColorName(), size.get().getSizeName());
            
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
            
            // Update product counts for size and color
            productCountService.updateVariantCounts(savedVariant);
            
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
            
            // Lưu size/color cũ để so sánh
            Long oldSizeId = variant.getSize().getSizeId();
            Long oldColorId = variant.getColor().getColorId();
            
            // Cập nhật thông tin
            variant.setSize(size.get());
            variant.setColor(color.get());
            
            // Tự động tạo lại SKU nếu thay đổi size hoặc color
            if (!oldSizeId.equals(request.getSizeId()) || !oldColorId.equals(request.getColorId())) {
                String newSku = generateSku(variant.getProduct().getSku(), color.get().getColorName(), size.get().getSizeName());
                variant.setSku(newSku);
            } else {
                // Nếu không thay đổi size/color thì giữ nguyên SKU từ request
                variant.setSku(request.getSku());
            }
            variant.setPrice(request.getPrice());
            variant.setStock(request.getStock());
            variant.setStatus(ProductVariant.VariantStatus.valueOf(request.getStatus()));
            
            ProductVariant updatedVariant = variantRepository.save(variant);
            
            // Update product counts for size and color
            productCountService.updateVariantCounts(updatedVariant);
            
            // Tự động cập nhật trạng thái sản phẩm
            productStatusService.updateProductStatusBasedOnVariants(variant.getProduct().getProductId());
            
            return ResponseEntity.ok(ProductVariantDTO.fromEntity(updatedVariant));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    
    // DELETE /api/variants/{id} - Soft delete variant (set status to INACTIVE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVariant(@PathVariable Long id) {
        try {
            Optional<ProductVariant> variantOptional = variantRepository.findById(id);
            if (variantOptional.isPresent()) {
                ProductVariant variant = variantOptional.get();
                Long productId = variant.getProduct().getProductId();
                
                // Soft delete: chuyển status thành INACTIVE thay vì xóa hẳn
                variant.setStatus(ProductVariant.VariantStatus.INACTIVE);
                variantRepository.save(variant);
                
                // Update product counts after soft delete
                productCountService.updateVariantCounts(variant);
                
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
    
    // POST /api/variants/{id}/images - Upload ảnh cho biến thể
    @PostMapping(value = "/{id}/images", consumes = "multipart/form-data")
    public ResponseEntity<ProductVariantDTO> uploadVariantImage(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile image) {
        try {
            // Kiểm tra variant tồn tại
            Optional<ProductVariant> variantOptional = variantRepository.findById(id);
            if (!variantOptional.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            ProductVariant variant = variantOptional.get();
            
            // Xử lý upload ảnh
            if (!image.isEmpty()) {
                String imagePath = saveImage(image);
                variant.setImagePath(imagePath);
                ProductVariant updatedVariant = variantRepository.save(variant);
                return ResponseEntity.ok(ProductVariantDTO.fromEntity(updatedVariant));
            }
            
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    
    // POST /api/variants/bulk - Tạo nhiều biến thể cùng lúc
    @PostMapping("/bulk")
    public ResponseEntity<List<ProductVariantDTO>> createBulkVariants(@RequestBody BulkVariantCreateRequest request) {
        try {
            List<ProductVariantDTO> createdVariants = new ArrayList<>();
            
            // Kiểm tra product tồn tại
            Optional<Product> product = productRepository.findById(request.getProductId());
            if (!product.isPresent()) {
                return ResponseEntity.badRequest().build();
            }
            
            for (BulkVariantItem item : request.getVariants()) {
                // Kiểm tra size tồn tại
                Optional<com.example.demo_store.entity.Size> size = sizeRepository.findById(item.getSizeId());
                if (!size.isPresent()) {
                    continue; // Skip invalid size
                }
                
                // Kiểm tra color tồn tại
                Optional<Color> color = colorRepository.findById(item.getColorId());
                if (!color.isPresent()) {
                    continue; // Skip invalid color
                }
                
                // Kiểm tra biến thể đã tồn tại chưa
                List<ProductVariant> existingVariants = variantRepository.findByProductAndSizeAndColor(
                    request.getProductId(), item.getSizeId(), item.getColorId());
                if (!existingVariants.isEmpty()) {
                    continue; // Skip existing variant
                }
                
                // Tạo SKU tự động
                String sku = generateSku(product.get().getSku(), color.get().getColorName(), size.get().getSizeName());
                
                // Kiểm tra SKU unique
                if (variantRepository.findBySku(sku).isPresent()) {
                    continue; // Skip duplicate SKU
                }
                
                // Tạo biến thể mới
                ProductVariant variant = new ProductVariant();
                variant.setProduct(product.get());
                variant.setSize(size.get());
                variant.setColor(color.get());
                variant.setSku(sku);
                variant.setPrice(item.getPrice());
                variant.setStock(item.getStock());
                variant.setStatus(ProductVariant.VariantStatus.valueOf(item.getStatus()));
                
                ProductVariant savedVariant = variantRepository.save(variant);
                createdVariants.add(ProductVariantDTO.fromEntity(savedVariant));
            }
            
            // Tự động cập nhật trạng thái sản phẩm
            productStatusService.updateProductStatusBasedOnVariants(request.getProductId());
            
            return ResponseEntity.ok(createdVariants);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
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
        
        // Convert to uppercase first
        String result = input.toUpperCase().trim();
        
        // Map Vietnamese characters to ASCII equivalents
        result = result.replace("À", "A").replace("Á", "A").replace("Ạ", "A").replace("Ả", "A").replace("Ã", "A");
        result = result.replace("Â", "A").replace("Ầ", "A").replace("Ấ", "A").replace("Ậ", "A").replace("Ẩ", "A").replace("Ẫ", "A");
        result = result.replace("Ă", "A").replace("Ằ", "A").replace("Ắ", "A").replace("Ặ", "A").replace("Ẳ", "A").replace("Ẵ", "A");
        
        result = result.replace("È", "E").replace("É", "E").replace("Ẹ", "E").replace("Ẻ", "E").replace("Ẽ", "E");
        result = result.replace("Ê", "E").replace("Ề", "E").replace("Ế", "E").replace("Ệ", "E").replace("Ể", "E").replace("Ễ", "E");
        
        result = result.replace("Ì", "I").replace("Í", "I").replace("Ị", "I").replace("Ỉ", "I").replace("Ĩ", "I");
        
        result = result.replace("Ò", "O").replace("Ó", "O").replace("Ọ", "O").replace("Ỏ", "O").replace("Õ", "O");
        result = result.replace("Ô", "O").replace("Ồ", "O").replace("Ố", "O").replace("Ộ", "O").replace("Ổ", "O").replace("Ỗ", "O");
        result = result.replace("Ơ", "O").replace("Ờ", "O").replace("Ớ", "O").replace("Ợ", "O").replace("Ở", "O").replace("Ỡ", "O");
        
        result = result.replace("Ù", "U").replace("Ú", "U").replace("Ụ", "U").replace("Ủ", "U").replace("Ũ", "U");
        result = result.replace("Ư", "U").replace("Ừ", "U").replace("Ứ", "U").replace("Ự", "U").replace("Ử", "U").replace("Ữ", "U");
        
        result = result.replace("Ỳ", "Y").replace("Ý", "Y").replace("Ỵ", "Y").replace("Ỷ", "Y").replace("Ỹ", "Y");
        
        result = result.replace("Đ", "D");
        
        // Remove any remaining special characters except letters, numbers, and hyphens
        result = result.replaceAll("[^A-Z0-9\\-]", "");
        
        // Remove multiple consecutive hyphens and trim
        result = result.replaceAll("-+", "-").replaceAll("^-|-$", "");
        
        return result;
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
    public static class BulkVariantCreateRequest {
        private Long productId;
        private List<BulkVariantItem> variants;
        
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        
        public List<BulkVariantItem> getVariants() { return variants; }
        public void setVariants(List<BulkVariantItem> variants) { this.variants = variants; }
    }
    
    public static class BulkVariantItem {
        private Long sizeId;
        private Long colorId;
        private BigDecimal price;
        private Integer stock;
        private String status = "ACTIVE";
        
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
    
    public static class ProductVariantCreateRequest {
        @NotNull(message = "Product ID is required")
        private Long productId;
        
        @NotNull(message = "Size ID is required")
        private Long sizeId;
        
        @NotNull(message = "Color ID is required")
        private Long colorId;
        
        @NotBlank(message = "SKU is required")
        @Size(max = 100, message = "SKU must not exceed 100 characters")
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
        @Size(max = 100, message = "SKU must not exceed 100 characters")
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
