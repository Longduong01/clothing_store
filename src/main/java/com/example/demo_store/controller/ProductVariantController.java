package com.example.demo_store.controller;

import com.example.demo_store.entity.ProductVariant;
import com.example.demo_store.entity.Product;
import com.example.demo_store.entity.Size;
import com.example.demo_store.entity.Color;
import com.example.demo_store.repository.ProductVariantRepository;
import com.example.demo_store.repository.ProductRepository;
import com.example.demo_store.repository.SizeRepository;
import com.example.demo_store.repository.ColorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/product-variants")
@CrossOrigin(origins = "*")
public class ProductVariantController {

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SizeRepository sizeRepository;

    @Autowired
    private ColorRepository colorRepository;

    // GET /api/product-variants - Lấy tất cả biến thể sản phẩm với pagination
    @GetMapping
    public ResponseEntity<?> getAllProductVariants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String status
    ) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<ProductVariant> variants;
            if (productId != null && status != null) {
                variants = productVariantRepository.findByProductProductIdAndStatus(productId, 
                    ProductVariant.VariantStatus.valueOf(status), pageable);
            } else if (productId != null) {
                variants = productVariantRepository.findByProductProductId(productId, pageable);
            } else if (status != null) {
                variants = productVariantRepository.findByStatus(
                    ProductVariant.VariantStatus.valueOf(status), pageable);
            } else {
                variants = productVariantRepository.findAll(pageable);
            }
            
            return ResponseEntity.ok(variants);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to fetch product variants: " + e.getMessage()));
        }
    }

    // GET /api/product-variants/{id} - Lấy biến thể sản phẩm theo ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductVariantById(@PathVariable Long id) {
        try {
            Optional<ProductVariant> variant = productVariantRepository.findById(id);
            if (variant.isPresent()) {
                return ResponseEntity.ok(variant.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to fetch product variant: " + e.getMessage()));
        }
    }

    // GET /api/product-variants/product/{productId} - Lấy biến thể theo sản phẩm
    @GetMapping("/product/{productId}")
    public ResponseEntity<?> getProductVariantsByProduct(@PathVariable Long productId) {
        try {
            List<ProductVariant> variants = productVariantRepository.findByProductProductIdOrderByCreatedAtAsc(productId);
            return ResponseEntity.ok(variants);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to fetch product variants: " + e.getMessage()));
        }
    }

    // POST /api/product-variants - Tạo biến thể sản phẩm mới
    @PostMapping
    public ResponseEntity<?> createProductVariant(@RequestBody ProductVariantCreateRequest request) {
        try {
            // Validate product exists
            if (!productRepository.existsById(request.getProductId())) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Product not found"));
            }

            // Validate size exists
            if (!sizeRepository.existsById(request.getSizeId())) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Size not found"));
            }

            // Validate color exists
            if (!colorRepository.existsById(request.getColorId())) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Color not found"));
            }

            // Check if variant already exists
            if (productVariantRepository.existsByProductProductIdAndSizeSizeIdAndColorColorId(
                request.getProductId(), request.getSizeId(), request.getColorId())) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Product variant already exists"));
            }

            Product product = productRepository.findById(request.getProductId()).get();
            Size size = sizeRepository.findById(request.getSizeId()).get();
            Color color = colorRepository.findById(request.getColorId()).get();

            // Create product variant
            ProductVariant variant = new ProductVariant();
            variant.setProduct(product);
            variant.setSize(size);
            variant.setColor(color);
            variant.setPrice(request.getPrice());
            variant.setStock(request.getStock());
            variant.setStatus(request.getStatus());

            ProductVariant savedVariant = productVariantRepository.save(variant);
            return ResponseEntity.ok(savedVariant);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to create product variant: " + e.getMessage()));
        }
    }

    // PUT /api/product-variants/{id} - Cập nhật biến thể sản phẩm
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProductVariant(@PathVariable Long id, @RequestBody ProductVariantUpdateRequest request) {
        try {
            Optional<ProductVariant> variantOptional = productVariantRepository.findById(id);
            if (variantOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            ProductVariant variant = variantOptional.get();
            if (request.getPrice() != null) {
                variant.setPrice(request.getPrice());
            }
            if (request.getStock() != null) {
                variant.setStock(request.getStock());
            }
            if (request.getStatus() != null) {
                variant.setStatus(request.getStatus());
            }

            ProductVariant updatedVariant = productVariantRepository.save(variant);
            return ResponseEntity.ok(updatedVariant);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to update product variant: " + e.getMessage()));
        }
    }

    // DELETE /api/product-variants/{id} - Xóa biến thể sản phẩm
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProductVariant(@PathVariable Long id) {
        try {
            if (!productVariantRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }

            productVariantRepository.deleteById(id);
            return ResponseEntity.ok(new SuccessResponse("Product variant deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to delete product variant: " + e.getMessage()));
        }
    }

    // GET /api/product-variants/stats - Thống kê biến thể sản phẩm
    @GetMapping("/stats")
    public ResponseEntity<?> getProductVariantStats() {
        try {
            long totalVariants = productVariantRepository.count();
            long activeVariants = productVariantRepository.countByStatus(ProductVariant.VariantStatus.ACTIVE);
            long outOfStockVariants = productVariantRepository.countByStatus(ProductVariant.VariantStatus.OUT_OF_STOCK);
            long inactiveVariants = productVariantRepository.countByStatus(ProductVariant.VariantStatus.INACTIVE);

            ProductVariantStats stats = new ProductVariantStats();
            stats.setTotalVariants(totalVariants);
            stats.setActiveVariants(activeVariants);
            stats.setOutOfStockVariants(outOfStockVariants);
            stats.setInactiveVariants(inactiveVariants);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to fetch product variant stats: " + e.getMessage()));
        }
    }

    // Response classes
    public static class ErrorResponse {
        private String error;
        
        public ErrorResponse(String error) {
            this.error = error;
        }
        
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }

    public static class SuccessResponse {
        private String message;
        
        public SuccessResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class ProductVariantCreateRequest {
        private Long productId;
        private Long sizeId;
        private Long colorId;
        private BigDecimal price;
        private Integer stock;
        private ProductVariant.VariantStatus status;

        // Getters and setters
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        
        public Long getSizeId() { return sizeId; }
        public void setSizeId(Long sizeId) { this.sizeId = sizeId; }
        
        public Long getColorId() { return colorId; }
        public void setColorId(Long colorId) { this.colorId = colorId; }
        
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        
        public Integer getStock() { return stock; }
        public void setStock(Integer stock) { this.stock = stock; }
        
        public ProductVariant.VariantStatus getStatus() { return status; }
        public void setStatus(ProductVariant.VariantStatus status) { this.status = status; }
    }

    public static class ProductVariantUpdateRequest {
        private BigDecimal price;
        private Integer stock;
        private ProductVariant.VariantStatus status;

        // Getters and setters
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        
        public Integer getStock() { return stock; }
        public void setStock(Integer stock) { this.stock = stock; }
        
        public ProductVariant.VariantStatus getStatus() { return status; }
        public void setStatus(ProductVariant.VariantStatus status) { this.status = status; }
    }

    public static class ProductVariantStats {
        private long totalVariants;
        private long activeVariants;
        private long outOfStockVariants;
        private long inactiveVariants;

        // Getters and setters
        public long getTotalVariants() { return totalVariants; }
        public void setTotalVariants(long totalVariants) { this.totalVariants = totalVariants; }
        
        public long getActiveVariants() { return activeVariants; }
        public void setActiveVariants(long activeVariants) { this.activeVariants = activeVariants; }
        
        public long getOutOfStockVariants() { return outOfStockVariants; }
        public void setOutOfStockVariants(long outOfStockVariants) { this.outOfStockVariants = outOfStockVariants; }
        
        public long getInactiveVariants() { return inactiveVariants; }
        public void setInactiveVariants(long inactiveVariants) { this.inactiveVariants = inactiveVariants; }
    }
}
