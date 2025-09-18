package com.example.demo_store.service;

import com.example.demo_store.entity.Product;
import com.example.demo_store.entity.ProductImage;
import com.example.demo_store.repository.ProductImageRepository;
import com.example.demo_store.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductImageService {
    
    @Autowired
    private ProductImageRepository productImageRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    private static final int MAX_IMAGES_PER_PRODUCT = 10;
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".webp"};
    
    // Lấy tất cả ảnh của sản phẩm
    public List<ProductImage> getProductImages(Long productId) {
        return productImageRepository.findByProductProductIdOrderBySortOrderAscImageIdAsc(productId);
    }
    
    // Lấy ảnh chính của sản phẩm
    public Optional<ProductImage> getPrimaryImage(Long productId) {
        return productImageRepository.findByProductProductIdAndIsPrimaryTrue(productId);
    }
    
    // Upload nhiều ảnh cho sản phẩm
    @Transactional
    public List<ProductImage> uploadProductImages(Long productId, MultipartFile[] files) {
        // Kiểm tra sản phẩm có tồn tại không
        Optional<Product> productOpt = productRepository.findById(productId);
        if (!productOpt.isPresent()) {
            throw new RuntimeException("Product not found with id: " + productId);
        }
        
        Product product = productOpt.get();
        
        // Kiểm tra số lượng ảnh hiện tại
        long currentImageCount = productImageRepository.countByProductProductId(productId);
        if (currentImageCount + files.length > MAX_IMAGES_PER_PRODUCT) {
            throw new RuntimeException("Maximum " + MAX_IMAGES_PER_PRODUCT + " images allowed per product");
        }
        
        List<ProductImage> uploadedImages = new ArrayList<>();
        
        for (MultipartFile file : files) {
            try {
                // Validate file
                validateFile(file);
                
                // Upload file
                String fileName = fileStorageService.storeFile(file);
                String imageUrl = "/api/files/view/" + fileName;
                
                // Tạo ProductImage entity
                ProductImage productImage = new ProductImage();
                productImage.setProduct(product);
                productImage.setImageUrl(imageUrl);
                productImage.setImageName(file.getOriginalFilename());
                productImage.setImageType(file.getContentType());
                productImage.setFileSize(file.getSize());
                productImage.setIsPrimary(false); // Mặc định không phải ảnh chính
                productImage.setSortOrder((int) currentImageCount + uploadedImages.size() + 1);
                
                // Lưu vào database
                ProductImage savedImage = productImageRepository.save(productImage);
                uploadedImages.add(savedImage);
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to upload image: " + file.getOriginalFilename() + " - " + e.getMessage());
            }
        }
        
        // Cập nhật thumbnail_url nếu chưa có
        if (product.getThumbnailUrl() == null && !uploadedImages.isEmpty()) {
            product.setThumbnailUrl(uploadedImages.get(0).getImageUrl());
            productRepository.save(product);
        }
        
        return uploadedImages;
    }
    
    // Upload ảnh đơn lẻ
    @Transactional
    public ProductImage uploadSingleImage(Long productId, MultipartFile file) {
        return uploadProductImages(productId, new MultipartFile[]{file}).get(0);
    }
    
    // Đặt ảnh chính
    @Transactional
    public ProductImage setPrimaryImage(Long productId, Long imageId) {
        // Bỏ primary của tất cả ảnh khác
        List<ProductImage> allImages = productImageRepository.findByProductProductIdOrderBySortOrderAscImageIdAsc(productId);
        for (ProductImage image : allImages) {
            image.setIsPrimary(false);
        }
        productImageRepository.saveAll(allImages);
        
        // Đặt ảnh được chọn làm primary
        Optional<ProductImage> imageOpt = productImageRepository.findById(imageId);
        if (!imageOpt.isPresent()) {
            throw new RuntimeException("Image not found with id: " + imageId);
        }
        
        ProductImage primaryImage = imageOpt.get();
        primaryImage.setIsPrimary(true);
        ProductImage savedImage = productImageRepository.save(primaryImage);
        
        // Cập nhật thumbnail_url của sản phẩm
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setThumbnailUrl(primaryImage.getImageUrl());
            productRepository.save(product);
        }
        
        return savedImage;
    }
    
    // Sắp xếp lại thứ tự ảnh
    @Transactional
    public List<ProductImage> reorderImages(Long productId, List<Long> imageIds) {
        List<ProductImage> images = productImageRepository.findByProductProductIdOrderBySortOrderAscImageIdAsc(productId);
        
        for (int i = 0; i < imageIds.size(); i++) {
            Long imageId = imageIds.get(i);
            Optional<ProductImage> imageOpt = images.stream()
                    .filter(img -> img.getImageId().equals(imageId))
                    .findFirst();
            
            if (imageOpt.isPresent()) {
                ProductImage image = imageOpt.get();
                image.setSortOrder(i + 1);
                productImageRepository.save(image);
            }
        }
        
        return productImageRepository.findByProductProductIdOrderBySortOrderAscImageIdAsc(productId);
    }
    
    // Xóa ảnh
    @Transactional
    public void deleteImage(Long productId, Long imageId) {
        Optional<ProductImage> imageOpt = productImageRepository.findById(imageId);
        if (!imageOpt.isPresent()) {
            throw new RuntimeException("Image not found with id: " + imageId);
        }
        
        ProductImage image = imageOpt.get();
        
        // Kiểm tra xem có phải ảnh chính không
        if (image.getIsPrimary()) {
            // Tìm ảnh khác để làm primary
            List<ProductImage> otherImages = productImageRepository.findByProductProductIdOrderBySortOrderAscImageIdAsc(productId)
                    .stream()
                    .filter(img -> !img.getImageId().equals(imageId))
                    .collect(Collectors.toList());
            
            if (!otherImages.isEmpty()) {
                ProductImage newPrimary = otherImages.get(0);
                newPrimary.setIsPrimary(true);
                productImageRepository.save(newPrimary);
                
                // Cập nhật thumbnail_url
                Optional<Product> productOpt = productRepository.findById(productId);
                if (productOpt.isPresent()) {
                    Product product = productOpt.get();
                    product.setThumbnailUrl(newPrimary.getImageUrl());
                    productRepository.save(product);
                }
            }
        }
        
        // Xóa file từ storage
        try {
            String fileName = image.getImageUrl().substring(image.getImageUrl().lastIndexOf("/") + 1);
            fileStorageService.deleteFile(fileName);
        } catch (Exception e) {
            // Log error but don't fail the operation
            System.err.println("Failed to delete file: " + e.getMessage());
        }
        
        // Xóa từ database
        productImageRepository.deleteById(imageId);
    }
    
    // Xóa tất cả ảnh của sản phẩm
    @Transactional
    public void deleteAllProductImages(Long productId) {
        List<ProductImage> images = productImageRepository.findByProductProductIdOrderBySortOrderAscImageIdAsc(productId);
        
        // Xóa files từ storage
        for (ProductImage image : images) {
            try {
                String fileName = image.getImageUrl().substring(image.getImageUrl().lastIndexOf("/") + 1);
                fileStorageService.deleteFile(fileName);
            } catch (Exception e) {
                System.err.println("Failed to delete file: " + e.getMessage());
            }
        }
        
        // Xóa từ database
        productImageRepository.deleteByProductProductId(productId);
        
        // Cập nhật thumbnail_url
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setThumbnailUrl(null);
            productRepository.save(product);
        }
    }
    
    // Validate file
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("File size exceeds " + (MAX_FILE_SIZE / 1024 / 1024) + "MB limit");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new RuntimeException("File name is null");
        }
        
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!Arrays.asList(ALLOWED_EXTENSIONS).contains(extension)) {
            throw new RuntimeException("Invalid file type. Allowed types: " + Arrays.toString(ALLOWED_EXTENSIONS));
        }
    }
    
    // Lấy thống kê ảnh
    public Map<String, Object> getImageStats(Long productId) {
        long totalImages = productImageRepository.countByProductProductId(productId);
        Optional<ProductImage> primaryImage = productImageRepository.findByProductProductIdAndIsPrimaryTrue(productId);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalImages", totalImages);
        stats.put("hasPrimaryImage", primaryImage.isPresent());
        stats.put("maxImagesAllowed", MAX_IMAGES_PER_PRODUCT);
        stats.put("canUploadMore", totalImages < MAX_IMAGES_PER_PRODUCT);
        
        return stats;
    }
}
