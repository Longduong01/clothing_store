package com.example.demo_store.service;

import com.example.demo_store.entity.ProductImage;
import com.example.demo_store.repository.ProductImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

@Service
public class ProductImageService {
    
    @Autowired
    private ProductImageRepository productImageRepository;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    public List<ProductImage> saveProductImages(Integer productId, MultipartFile[] images) {
        List<ProductImage> savedImages = new ArrayList<>();
        
        if (images != null && images.length > 0) {
            for (int i = 0; i < images.length; i++) {
                MultipartFile image = images[i];
                if (image != null && !image.isEmpty()) {
                    try {
                        String fileName = fileStorageService.storeFile(image);
                        String imageUrl = "/api/files/view/" + fileName;
                        
                        ProductImage productImage = new ProductImage();
                        productImage.setProductId(productId);
                        productImage.setImageUrl(imageUrl);
                        productImage.setImageOrder(i);
                        productImage.setIsPrimary(i == 0); // First image is primary
                        
                        ProductImage saved = productImageRepository.save(productImage);
                        savedImages.add(saved);
                    } catch (Exception e) {
                        System.err.println("Error saving product image: " + e.getMessage());
                    }
                }
            }
        }
        
        return savedImages;
    }
    
    public List<ProductImage> getProductImages(Integer productId) {
        return productImageRepository.findByProductIdOrderByPrimaryAndOrder(productId);
    }
    
    public void deleteProductImages(Integer productId) {
        productImageRepository.deleteByProductId(productId);
    }
    
    public ProductImage updatePrimaryImage(Integer productId, Integer imageId) {
        // Set all images to non-primary
        List<ProductImage> allImages = productImageRepository.findByProductIdOrderByImageOrderAsc(productId);
        for (ProductImage image : allImages) {
            image.setIsPrimary(false);
            productImageRepository.save(image);
        }
        
        // Set selected image as primary
        ProductImage primaryImage = productImageRepository.findById(imageId).orElse(null);
        if (primaryImage != null) {
            primaryImage.setIsPrimary(true);
            return productImageRepository.save(primaryImage);
        }
        
        return null;
    }
    
    public List<ProductImage> uploadProductImages(Long productId, MultipartFile[] images) {
        return saveProductImages(productId.intValue(), images);
    }
    
    public ProductImage uploadSingleImage(Long productId, MultipartFile image) {
        List<ProductImage> images = saveProductImages(productId.intValue(), new MultipartFile[]{image});
        return images.isEmpty() ? null : images.get(0);
    }
    
    public ProductImage setPrimaryImage(Long productId, Long imageId) {
        return updatePrimaryImage(productId.intValue(), imageId.intValue());
    }
    
    public List<ProductImage> reorderImages(Long productId, List<Long> imageIds) {
        List<ProductImage> images = productImageRepository.findByProductIdOrderByImageOrderAsc(productId.intValue());
        for (int i = 0; i < imageIds.size(); i++) {
            Long imageId = imageIds.get(i);
            ProductImage image = images.stream()
                .filter(img -> img.getImageId().equals(imageId.intValue()))
                .findFirst()
                .orElse(null);
            if (image != null) {
                image.setImageOrder(i);
                productImageRepository.save(image);
            }
        }
        return productImageRepository.findByProductIdOrderByPrimaryAndOrder(productId.intValue());
    }
    
    public void deleteImage(Long productId, Long imageId) {
        productImageRepository.deleteById(imageId.intValue());
    }
    
    public void deleteAllProductImages(Long productId) {
        deleteProductImages(productId.intValue());
    }
    
    public Map<String, Object> getImageStats(Long productId) {
        List<ProductImage> images = getProductImages(productId.intValue());
        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalImages", images.size());
        stats.put("primaryImage", images.stream().filter(ProductImage::getIsPrimary).findFirst().orElse(null));
        stats.put("images", images);
        return stats;
    }
}