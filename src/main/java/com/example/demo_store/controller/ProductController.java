package com.example.demo_store.controller;

import com.example.demo_store.entity.Product;
import com.example.demo_store.entity.Category;
import com.example.demo_store.entity.Brand;
import com.example.demo_store.entity.ProductImage;
import com.example.demo_store.repository.ProductRepository;
import com.example.demo_store.repository.CategoryRepository;
import com.example.demo_store.repository.BrandRepository;
import com.example.demo_store.service.FileStorageService;
import com.example.demo_store.service.ProductImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private BrandRepository brandRepository;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private ProductImageService productImageService;
    
    // GET /api/products - Lấy tất cả products
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        try {
            List<Product> products = productRepository.findAll();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // GET /api/products/{id} - Lấy product theo ID
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        try {
            Optional<Product> product = productRepository.findById(id);
            return product.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // GET /api/products/sku/{sku} - Lấy product theo SKU
    @GetMapping("/sku/{sku}")
    public ResponseEntity<Product> getProductBySku(@PathVariable String sku) {
        try {
            Optional<Product> product = productRepository.findBySku(sku);
            return product.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // GET /api/products/status/{status} - Lấy products theo status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Product>> getProductsByStatus(@PathVariable String status) {
        try {
            Product.ProductStatus productStatus = Product.ProductStatus.valueOf(status.toUpperCase());
            List<Product> products = productRepository.findByStatus(productStatus);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // GET /api/products/category/{categoryId} - Lấy products theo category
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable Long categoryId) {
        try {
            List<Product> products = productRepository.findByCategoryCategoryId(categoryId);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // GET /api/products/brand/{brandId} - Lấy products theo brand
    @GetMapping("/brand/{brandId}")
    public ResponseEntity<List<Product>> getProductsByBrand(@PathVariable Long brandId) {
        try {
            List<Product> products = productRepository.findByBrandBrandId(brandId);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // GET /api/products/search?keyword={keyword} - Tìm kiếm products
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String keyword) {
        try {
            List<Product> products = productRepository.findByKeyword(keyword);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // GET /api/products/price-range?min={min}&max={max} - Lấy products theo khoảng giá
    @GetMapping("/price-range")
    public ResponseEntity<List<Product>> getProductsByPriceRange(
            @RequestParam Double minPrice, 
            @RequestParam Double maxPrice) {
        try {
            List<Product> products = productRepository.findByPriceRange(minPrice, maxPrice);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // POST /api/products - Tạo product mới
    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody ProductCreateRequest request) {
        try {
            Product product = new Product();
            product.setProductName(request.getProductName());
            product.setDescription(request.getDescription());
            product.setSku(request.getSku());
            product.setPrice(new BigDecimal(request.getPrice()));
            product.setStockQuantity(request.getStockQuantity());
            product.setStatus(Product.ProductStatus.valueOf(request.getStatus()));
            product.setImageUrl(request.getImageUrl());
            
            // Set category
            if (request.getCategoryId() != null) {
                Optional<Category> category = categoryRepository.findById(request.getCategoryId());
                if (category.isPresent()) {
                    product.setCategory(category.get());
                }
            }
            
            // Set brand
            if (request.getBrandId() != null) {
                Optional<Brand> brand = brandRepository.findById(request.getBrandId());
                if (brand.isPresent()) {
                    product.setBrand(brand.get());
                }
            }
            
            Product savedProduct = productRepository.save(product);
            return ResponseEntity.ok(savedProduct);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // POST /api/products/upload-image - Upload ảnh sản phẩm
    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadProductImage(@RequestParam("file") MultipartFile file) {
        try {
            String fileName = fileStorageService.storeFile(file);
            String imageUrl = "/api/files/view/" + fileName;
            return ResponseEntity.ok().body(new ImageUploadResponse(fileName, imageUrl));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Could not upload image: " + e.getMessage()));
        }
    }
    
    // PUT /api/products/{id} - Cập nhật product
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product productDetails) {
        try {
            Optional<Product> productOptional = productRepository.findById(id);
            if (productOptional.isPresent()) {
                Product product = productOptional.get();
                product.setProductName(productDetails.getProductName());
                product.setDescription(productDetails.getDescription());
                product.setSku(productDetails.getSku());
                product.setPrice(productDetails.getPrice());
                product.setStatus(productDetails.getStatus());
                
                Product updatedProduct = productRepository.save(product);
                return ResponseEntity.ok(updatedProduct);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // DELETE /api/products/{id} - Xóa product
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        try {
            if (productRepository.existsById(id)) {
                productRepository.deleteById(id);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // GET /api/products/count - Đếm số lượng products
    @GetMapping("/count")
    public ResponseEntity<Long> getProductCount() {
        try {
            long count = productRepository.count();
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // Request/Response classes
    public static class ProductCreateRequest {
        @NotBlank(message = "Product name is required")
        @Size(max = 200, message = "Product name must not exceed 200 characters")
        private String productName;
        
        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        private String description;
        
        @NotBlank(message = "SKU is required")
        @Size(max = 50, message = "SKU must not exceed 50 characters")
        private String sku;
        
        @NotBlank(message = "Price is required")
        private String price;
        
        @NotNull(message = "Stock quantity is required")
        @Min(value = 0, message = "Stock quantity must be non-negative")
        private Integer stockQuantity;
        
        private String status;
        private String imageUrl;
        
        @NotNull(message = "Category ID is required")
        private Long categoryId;
        
        @NotNull(message = "Brand ID is required")
        private Long brandId;
        
        // Getters and setters
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        
        public String getPrice() { return price; }
        public void setPrice(String price) { this.price = price; }
        
        public Integer getStockQuantity() { return stockQuantity; }
        public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        
        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
        
        public Long getBrandId() { return brandId; }
        public void setBrandId(Long brandId) { this.brandId = brandId; }
    }
    
    public static class ImageUploadResponse {
        private String fileName;
        private String imageUrl;
        
        public ImageUploadResponse(String fileName, String imageUrl) {
            this.fileName = fileName;
            this.imageUrl = imageUrl;
        }
        
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    }
    
    public static class ErrorResponse {
        private String error;
        
        public ErrorResponse(String error) {
            this.error = error;
        }
        
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }
    
    // ==================== PRODUCT IMAGES ENDPOINTS ====================
    
    // GET /api/products/{id}/images - Lấy tất cả ảnh của sản phẩm
    @GetMapping("/{id}/images")
    public ResponseEntity<List<ProductImage>> getProductImages(@PathVariable Long id) {
        try {
            List<ProductImage> images = productImageService.getProductImages(id);
            return ResponseEntity.ok(images);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // POST /api/products/{id}/images/upload - Upload nhiều ảnh cho sản phẩm
    @PostMapping("/{id}/images/upload")
    public ResponseEntity<?> uploadProductImages(@PathVariable Long id, @RequestParam("files") MultipartFile[] files) {
        try {
            List<ProductImage> uploadedImages = productImageService.uploadProductImages(id, files);
            return ResponseEntity.ok().body(new ProductImageUploadResponse(
                uploadedImages.size() + " images uploaded successfully",
                uploadedImages
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to upload images: " + e.getMessage()));
        }
    }
    
    // POST /api/products/{id}/images/upload-single - Upload ảnh đơn lẻ
    @PostMapping("/{id}/images/upload-single")
    public ResponseEntity<?> uploadSingleImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            ProductImage uploadedImage = productImageService.uploadSingleImage(id, file);
            return ResponseEntity.ok().body(new ProductImageUploadResponse(
                "Image uploaded successfully",
                List.of(uploadedImage)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to upload image: " + e.getMessage()));
        }
    }
    
    // PUT /api/products/{id}/images/{imageId}/set-primary - Đặt ảnh chính
    @PutMapping("/{id}/images/{imageId}/set-primary")
    public ResponseEntity<?> setPrimaryImage(@PathVariable Long id, @PathVariable Long imageId) {
        try {
            ProductImage primaryImage = productImageService.setPrimaryImage(id, imageId);
            return ResponseEntity.ok().body(new ProductImageUploadResponse(
                "Primary image set successfully",
                List.of(primaryImage)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to set primary image: " + e.getMessage()));
        }
    }
    
    // PUT /api/products/{id}/images/reorder - Sắp xếp lại thứ tự ảnh
    @PutMapping("/{id}/images/reorder")
    public ResponseEntity<?> reorderImages(@PathVariable Long id, @RequestBody ReorderImagesRequest request) {
        try {
            List<ProductImage> reorderedImages = productImageService.reorderImages(id, request.getImageIds());
            return ResponseEntity.ok().body(new ProductImageUploadResponse(
                "Images reordered successfully",
                reorderedImages
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to reorder images: " + e.getMessage()));
        }
    }
    
    // DELETE /api/products/{id}/images/{imageId} - Xóa ảnh
    @DeleteMapping("/{id}/images/{imageId}")
    public ResponseEntity<?> deleteImage(@PathVariable Long id, @PathVariable Long imageId) {
        try {
            productImageService.deleteImage(id, imageId);
            return ResponseEntity.ok().body(new SuccessResponse("Image deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to delete image: " + e.getMessage()));
        }
    }
    
    // DELETE /api/products/{id}/images - Xóa tất cả ảnh của sản phẩm
    @DeleteMapping("/{id}/images")
    public ResponseEntity<?> deleteAllImages(@PathVariable Long id) {
        try {
            productImageService.deleteAllProductImages(id);
            return ResponseEntity.ok().body(new SuccessResponse("All images deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to delete images: " + e.getMessage()));
        }
    }
    
    // GET /api/products/{id}/images/stats - Lấy thống kê ảnh
    @GetMapping("/{id}/images/stats")
    public ResponseEntity<?> getImageStats(@PathVariable Long id) {
        try {
            Map<String, Object> stats = productImageService.getImageStats(id);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to get image stats: " + e.getMessage()));
        }
    }
    
    // Response classes for image operations
    public static class ProductImageUploadResponse {
        private String message;
        private List<ProductImage> images;
        
        public ProductImageUploadResponse(String message, List<ProductImage> images) {
            this.message = message;
            this.images = images;
        }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public List<ProductImage> getImages() { return images; }
        public void setImages(List<ProductImage> images) { this.images = images; }
    }
    
    public static class SuccessResponse {
        private String message;
        
        public SuccessResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
    
    public static class ReorderImagesRequest {
        private List<Long> imageIds;
        
        public List<Long> getImageIds() { return imageIds; }
        public void setImageIds(List<Long> imageIds) { this.imageIds = imageIds; }
    }
}
