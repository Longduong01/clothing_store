package com.example.demo_store.controller;

import com.example.demo_store.entity.Product;
import com.example.demo_store.entity.Category;
import com.example.demo_store.entity.Brand;
import com.example.demo_store.entity.ProductImage;
import com.example.demo_store.dto.ProductDTO;
import com.example.demo_store.dto.ProductUpdateRequest;
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
    
    @Autowired
    private com.example.demo_store.repository.ProductVariantRepository productVariantRepository;
    
    // Tính tổng tồn kho từ các variants của sản phẩm
    private Integer calculateTotalStock(Long productId) {
        try {
            List<com.example.demo_store.entity.ProductVariant> variants = 
                productVariantRepository.findByProductProductId(productId);
            return variants.stream()
                .mapToInt(com.example.demo_store.entity.ProductVariant::getStock)
                .sum();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    // GET /api/products - Lấy tất cả products (trả về DTO gọn tránh lỗi serialize)
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts(
            @RequestParam(value = "includeInactive", defaultValue = "false") boolean includeInactive) {
        try {
            List<Product> products;
            if (includeInactive) {
                // Lấy tất cả sản phẩm (bao gồm cả inactive)
                products = productRepository.findAll();
            } else {
                // Chỉ lấy sản phẩm active
                products = productRepository.findByStatus(Product.ProductStatus.ACTIVE);
            }
            
            List<ProductDTO> dtos = products.stream()
                .map(product -> {
                    ProductDTO dto = ProductDTO.fromEntity(product);
                    // Tính tổng tồn kho từ variants
                    dto.setTotalStock(calculateTotalStock(product.getProductId()));
                    return dto;
                })
                .toList();
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    
    // GET /api/products/{id} - Lấy product theo ID
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        try {
            Optional<Product> product = productRepository.findById(id);
            if (product.isPresent()) {
                ProductDTO dto = ProductDTO.fromEntity(product.get());
                // Tính tổng tồn kho từ variants
                dto.setTotalStock(calculateTotalStock(id));
                return ResponseEntity.ok(dto);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    
    // GET /api/products/sku/{sku} - Lấy product theo SKU
    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductDto> getProductBySku(@PathVariable String sku) {
        try {
            Optional<Product> product = productRepository.findBySku(sku);
            return product.map(p -> ResponseEntity.ok(ProductDto.fromEntity(p)))
                        .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // GET /api/products/name/{name} - Lấy product theo tên (phục vụ unique check FE)
    @GetMapping("/name/{name}")
    public ResponseEntity<ProductDto> getProductByName(@PathVariable String name) {
        try {
            List<Product> products = productRepository.findByKeyword(name);
            return products.stream().findFirst()
                    .map(p -> ResponseEntity.ok(ProductDto.fromEntity(p)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            e.printStackTrace();
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
    
    // Price range search is now handled at variant level
    // @GetMapping("/price-range")
    // public ResponseEntity<List<Product>> getProductsByPriceRange(...)
    
    // POST /api/products - Tạo product mới (parent product)
    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody ProductCreateRequest request) {
        try {
            Product product = new Product();
            product.setProductName(request.getProductName());
            product.setDescription(request.getDescription());
            product.setSku(request.getSku());
            // Price and stock are now managed at variant level
            product.setStatus(Product.ProductStatus.valueOf(request.getStatus()));
            product.setImageUrl(request.getImageUrl());
            product.setThumbnailUrl(request.getThumbnailUrl());
            
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
            e.printStackTrace();
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
    public ResponseEntity<ProductDto> updateProduct(@PathVariable Long id, @RequestBody ProductUpdateRequest request) {
        try {
            Optional<Product> productOptional = productRepository.findById(id);
            if (productOptional.isPresent()) {
                Product product = productOptional.get();
                product.setProductName(request.getProductName());
                product.setDescription(request.getDescription());
                product.setSku(request.getSku());
                product.setStatus(Product.ProductStatus.valueOf(request.getStatus()));
                product.setImageUrl(request.getImageUrl());
                product.setThumbnailUrl(request.getThumbnailUrl());
                
                // Update category if provided
                if (request.getCategoryId() != null) {
                    Optional<Category> category = categoryRepository.findById(request.getCategoryId());
                    if (category.isPresent()) {
                        product.setCategory(category.get());
                    }
                }
                
                // Update brand if provided
                if (request.getBrandId() != null) {
                    Optional<Brand> brand = brandRepository.findById(request.getBrandId());
                    if (brand.isPresent()) {
                        product.setBrand(brand.get());
                    }
                }
                
                Product updatedProduct = productRepository.save(product);
                return ResponseEntity.ok(ProductDto.fromEntity(updatedProduct));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    
    // DELETE /api/products/{id} - Soft delete product (chuyển status thành INACTIVE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        try {
            Optional<Product> productOptional = productRepository.findById(id);
            if (productOptional.isPresent()) {
                Product product = productOptional.get();
                // Soft delete: chuyển status thành INACTIVE thay vì xóa hẳn
                product.setStatus(Product.ProductStatus.INACTIVE);
                productRepository.save(product);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        
        private String status;
        private String imageUrl;
        private String thumbnailUrl;
        
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
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        
        public String getThumbnailUrl() { return thumbnailUrl; }
        public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
        
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
    
    // POST /api/products/{id}/images - Upload ảnh cho sản phẩm
    @PostMapping(value = "/{id}/images", consumes = "multipart/form-data")
    public ResponseEntity<List<ProductImage>> uploadProductImages(
            @PathVariable Long id,
            @RequestParam("images") List<MultipartFile> images) {
        try {
            // Kiểm tra product tồn tại
            Optional<Product> product = productRepository.findById(id);
            if (!product.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            List<ProductImage> uploadedImages = productImageService.uploadProductImages(id, images.toArray(new MultipartFile[0]));
            
            return ResponseEntity.ok(uploadedImages);
        } catch (Exception e) {
            e.printStackTrace();
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

// DTO classes
class ProductDto {
    public Long productId;
    public String productName;
    public String description;
    public String sku;
    public Double price;
    public Integer stockQuantity;
    public String status;
    public String imageUrl;
    public String thumbnailUrl;
    public SimpleRef brand;
    public SimpleRef category;

    public static ProductDto fromEntity(Product p) {
        ProductDto dto = new ProductDto();
        dto.productId = p.getProductId();
        dto.productName = p.getProductName();
        dto.description = p.getDescription();
        dto.sku = p.getSku();
        // Price and stock are now managed at variant level
        dto.price = null; // Will be managed at variant level
        dto.stockQuantity = null; // Will be managed at variant level
        dto.status = p.getStatus() != null ? p.getStatus().name() : null;
        dto.imageUrl = p.getImageUrl();
        dto.thumbnailUrl = p.getThumbnailUrl();
        if (p.getBrand() != null) {
            dto.brand = new SimpleRef(p.getBrand().getBrandId(), p.getBrand().getBrandName());
        }
        if (p.getCategory() != null) {
            dto.category = new SimpleRef(p.getCategory().getCategoryId(), p.getCategory().getCategoryName());
        }
        return dto;
    }
}

class SimpleRef {
    public Long id;
    public String name;
    public SimpleRef(Long id, String name) { this.id = id; this.name = name; }
}

