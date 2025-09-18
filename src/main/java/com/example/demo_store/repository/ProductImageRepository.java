package com.example.demo_store.repository;

import com.example.demo_store.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    
    // Lấy tất cả ảnh của một sản phẩm
    List<ProductImage> findByProductProductIdOrderBySortOrderAscImageIdAsc(Long productId);
    
    // Lấy ảnh chính của một sản phẩm
    Optional<ProductImage> findByProductProductIdAndIsPrimaryTrue(Long productId);
    
    // Lấy ảnh theo product_id và sort_order
    List<ProductImage> findByProductProductIdAndSortOrder(Long productId, Integer sortOrder);
    
    // Đếm số lượng ảnh của một sản phẩm
    Long countByProductProductId(Long productId);
    
    // Xóa tất cả ảnh của một sản phẩm
    void deleteByProductProductId(Long productId);
    
    // Lấy ảnh theo image_url
    Optional<ProductImage> findByImageUrl(String imageUrl);
    
    // Lấy ảnh theo product_id và image_name
    Optional<ProductImage> findByProductProductIdAndImageName(Long productId, String imageName);
    
    // Lấy ảnh chính hoặc ảnh đầu tiên của sản phẩm
    @Query("SELECT pi FROM ProductImage pi WHERE pi.product.productId = :productId ORDER BY pi.isPrimary DESC, pi.sortOrder ASC, pi.imageId ASC")
    List<ProductImage> findPrimaryOrFirstByProductId(@Param("productId") Long productId);
    
    // Lấy ảnh theo product_id với limit
    @Query("SELECT pi FROM ProductImage pi WHERE pi.product.productId = :productId ORDER BY pi.sortOrder ASC, pi.imageId ASC")
    List<ProductImage> findByProductIdWithLimit(@Param("productId") Long productId, org.springframework.data.domain.Pageable pageable);
}
