package com.example.demo_store.controller;

import com.example.demo_store.entity.Product;
import com.example.demo_store.entity.Category;
import com.example.demo_store.entity.Brand;
import com.example.demo_store.entity.Gender;
import com.example.demo_store.entity.ProductImage;
import com.example.demo_store.dto.ProductDTO;
import com.example.demo_store.dto.ProductCreateRequest;
import com.example.demo_store.dto.ProductUpdateRequest;
import com.example.demo_store.repository.ProductRepository;
import com.example.demo_store.repository.CategoryRepository;
import com.example.demo_store.repository.BrandRepository;
import com.example.demo_store.repository.GenderRepository;
import com.example.demo_store.service.FileStorageService;
import com.example.demo_store.service.ProductImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;

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
    private GenderRepository genderRepository;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private ProductImageService productImageService;
    
    @Autowired
    private com.example.demo_store.repository.ProductVariantRepository productVariantRepository;
    
    @Autowired
    private com.example.demo_store.service.ProductCountService productCountService;
    
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
                    // Lấy gallery images
                    try {
                        List<ProductImage> galleryImages = productImageService.getProductImages(product.getProductId().intValue());
                        System.out.println("Product " + product.getProductId() + " has " + galleryImages.size() + " gallery images");
                        dto.setGalleryImages(galleryImages);
                    } catch (Exception e) {
                        System.out.println("Error getting gallery images for product " + product.getProductId() + ": " + e.getMessage());
                        // Nếu có lỗi khi lấy ảnh, để trống
                        dto.setGalleryImages(new ArrayList<>());
                    }
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
    
    
    // PUT /api/products/{id}/images/{imageId}/primary - Đặt ảnh làm ảnh chính
    @PutMapping("/{id}/images/{imageId}/primary")
    public ResponseEntity<ProductImage> setPrimaryImage(@PathVariable Long id, @PathVariable Integer imageId) {
        ProductImage primaryImage = productImageService.updatePrimaryImage(id.intValue(), imageId);
        if (primaryImage != null) {
            return ResponseEntity.ok(primaryImage);
        } else {
            return ResponseEntity.notFound().build();
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
    
    // POST /api/products - Tạo product mới với upload ảnh
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ProductDTO> createProductWithImages(
            @RequestParam("productName") String productName,
            @RequestParam("description") String description,
            @RequestParam("sku") String sku,
            @RequestParam("status") String status,
            @RequestParam(value = "genderId", required = false) Long genderId,
            @RequestParam(value = "brandId", required = false) Long brandId,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "categoryIds", required = false) String categoryIds,
            @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestParam(value = "images", required = false) MultipartFile[] images) {
        
        try {
            Product product = new Product();
            product.setProductName(productName);
            product.setDescription(description);
            product.setSku(sku);
            product.setStatus(Product.ProductStatus.valueOf(status));
            
            // Set gender
            if (genderId != null) {
                Optional<Gender> gender = genderRepository.findById(genderId);
                if (gender.isPresent()) {
                    product.setGender(gender.get());
                }
            }
            
            // Set brand
            if (brandId != null) {
                Optional<Brand> brand = brandRepository.findById(brandId);
                if (brand.isPresent()) {
                    product.setBrand(brand.get());
                }
            }
            
            // Set multiple categories
            if (categoryIds != null && !categoryIds.isEmpty()) {
                Set<Category> categories = new HashSet<>();
                String[] ids = categoryIds.split(",");
                for (String id : ids) {
                    try {
                        Long categoryIdLong = Long.parseLong(id.trim());
                        Optional<Category> category = categoryRepository.findById(categoryIdLong);
                        if (category.isPresent()) {
                            categories.add(category.get());
                        }
                    } catch (NumberFormatException e) {
                        // Skip invalid IDs
                    }
                }
                product.setCategories(categories);
            }
            
            // Set single category for backward compatibility
            if (categoryId != null) {
                Optional<Category> category = categoryRepository.findById(categoryId);
                if (category.isPresent()) {
                    product.setCategory(category.get());
                }
            }
            
            // Handle thumbnail upload
            if (thumbnail != null && !thumbnail.isEmpty()) {
                String thumbnailFileName = fileStorageService.storeFile(thumbnail);
                product.setThumbnailUrl("/api/files/view/" + thumbnailFileName);
            }
            
            Product savedProduct = productRepository.save(product);
            
            // Handle gallery images upload
            if (images != null && images.length > 0) {
                productImageService.saveProductImages(savedProduct.getProductId().intValue(), images);
            }
            
            // Update product counts for categories and brand
            productCountService.updateProductCounts(savedProduct);
            
            ProductDTO productDTO = ProductDTO.fromEntity(savedProduct);
            productDTO.setTotalStock(calculateTotalStock(savedProduct.getProductId()));
            return ResponseEntity.ok(productDTO);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // POST /api/products - Tạo product mới (parent product) - JSON only
    @PostMapping(consumes = "application/json")
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductCreateRequest request) {
        try {
            Product product = new Product();
            product.setProductName(request.getProductName());
            product.setDescription(request.getDescription());
            product.setSku(request.getSku());
            product.setStatus(Product.ProductStatus.valueOf(request.getStatus()));
            // Set gender
            if (request.getGenderId() != null) {
                Optional<Gender> gender = genderRepository.findById(request.getGenderId());
                if (gender.isPresent()) {
                    product.setGender(gender.get());
                }
            }
            product.setThumbnailUrl(request.getThumbnailUrl());
            
            // Set brand
            if (request.getBrandId() != null) {
                Optional<Brand> brand = brandRepository.findById(request.getBrandId());
                if (brand.isPresent()) {
                    product.setBrand(brand.get());
                }
            }
            
            // Set multiple categories
            if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
                Set<Category> categories = new HashSet<>();
                for (Long categoryId : request.getCategoryIds()) {
                    Optional<Category> category = categoryRepository.findById(categoryId);
                    if (category.isPresent()) {
                        categories.add(category.get());
                    }
                }
                product.setCategories(categories);
            }
            
            // Set single category for backward compatibility
            if (request.getCategoryId() != null) {
                Optional<Category> category = categoryRepository.findById(request.getCategoryId());
                if (category.isPresent()) {
                    product.setCategory(category.get());
                }
            }
            
            Product savedProduct = productRepository.save(product);
            
            // Update product counts for categories and brand
            productCountService.updateProductCounts(savedProduct);
            
            // Calculate total stock and return as DTO
            ProductDTO productDTO = ProductDTO.fromEntity(savedProduct);
            productDTO.setTotalStock(calculateTotalStock(savedProduct.getProductId()));
            
            return ResponseEntity.ok(productDTO);
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
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @RequestBody ProductUpdateRequest request) {
        try {
            Optional<Product> productOptional = productRepository.findById(id);
            if (productOptional.isPresent()) {
                Product product = productOptional.get();
                
                if (request.getProductName() != null) {
                    product.setProductName(request.getProductName());
                }
                if (request.getDescription() != null) {
                    product.setDescription(request.getDescription());
                }
                if (request.getSku() != null) {
                    product.setSku(request.getSku());
                }
                if (request.getStatus() != null) {
                    product.setStatus(Product.ProductStatus.valueOf(request.getStatus()));
                }
                if (request.getGenderId() != null) {
                    Optional<Gender> gender = genderRepository.findById(request.getGenderId());
                    if (gender.isPresent()) {
                        product.setGender(gender.get());
                    }
                }
                if (request.getThumbnailUrl() != null) {
                    product.setThumbnailUrl(request.getThumbnailUrl());
                }
                
                // Update multiple categories if provided
                if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
                    Set<Category> categories = new HashSet<>();
                    for (Long categoryId : request.getCategoryIds()) {
                        Optional<Category> category = categoryRepository.findById(categoryId);
                        if (category.isPresent()) {
                            categories.add(category.get());
                        }
                    }
                    product.setCategories(categories);
                }
                
                // Update single category for backward compatibility
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
                
                // Update product counts for categories and brand
                productCountService.updateProductCounts(updatedProduct);
                
                // Calculate total stock and return as DTO
                ProductDTO productDTO = ProductDTO.fromEntity(updatedProduct);
                productDTO.setTotalStock(calculateTotalStock(updatedProduct.getProductId()));
                
                return ResponseEntity.ok(productDTO);
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
                
                // Update product counts after soft delete
                productCountService.updateProductCounts(product);
                
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
            List<ProductImage> images = productImageService.getProductImages(id.intValue());
            return ResponseEntity.ok(images);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // POST /api/products/{id}/images - Upload ảnh cho sản phẩm
    @PostMapping(value = "/{id}/images", consumes = "multipart/form-data")
    public ResponseEntity<List<ProductImage>> uploadProductImages(
            @PathVariable Long id,
            @RequestParam("images") MultipartFile[] images) {
        try {
            // Kiểm tra product tồn tại
            Optional<Product> product = productRepository.findById(id);
            if (!product.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            List<ProductImage> uploadedImages = productImageService.saveProductImages(id.intValue(), images);
            
            return ResponseEntity.ok(uploadedImages);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    
    // DELETE /api/products/{id}/images/{imageId} - Xóa 1 ảnh của sản phẩm
    @DeleteMapping("/{id}/images/{imageId}")
    public ResponseEntity<Void> deleteProductImage(
            @PathVariable Long id,
            @PathVariable Long imageId) {
        try {
            // Không cần kiểm tra product ở đây, chỉ cần xóa theo imageId
            productImageService.deleteImage(id, imageId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    
    
    // POST /api/products/{id}/thumbnail - Upload thumbnail cho sản phẩm
    @PostMapping(value = "/{id}/thumbnail", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadThumbnail(
            @PathVariable Long id,
            @RequestParam("thumbnail") MultipartFile thumbnail) {
        try {
            // Kiểm tra product tồn tại
            Optional<Product> product = productRepository.findById(id);
            if (!product.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            // Lưu thumbnail file
            String thumbnailFileName = fileStorageService.storeFile(thumbnail);
            String thumbnailUrl = "/api/files/view/" + thumbnailFileName;
            
            // Cập nhật thumbnailUrl trong product
            Product productEntity = product.get();
            productEntity.setThumbnailUrl(thumbnailUrl);
            productRepository.save(productEntity);
            
            return ResponseEntity.ok().body(Map.of(
                "message", "Thumbnail uploaded successfully",
                "thumbnailUrl", thumbnailUrl
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to upload thumbnail: " + e.getMessage()
            ));
        }
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
        dto.imageUrl = null; // Image URL is now managed in ProductImages table
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

