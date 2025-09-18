package com.example.demo_store.repository;

import com.example.demo_store.entity.ProductVariant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    
    // Find variants by product ID
    List<ProductVariant> findByProductProductIdOrderByCreatedAtAsc(Long productId);
    
    // Find variants by product ID with pagination
    Page<ProductVariant> findByProductProductId(Long productId, Pageable pageable);
    
    // Find variants by status
    Page<ProductVariant> findByStatus(ProductVariant.VariantStatus status, Pageable pageable);
    
    // Find variants by product ID and status
    Page<ProductVariant> findByProductProductIdAndStatus(Long productId, ProductVariant.VariantStatus status, Pageable pageable);
    
    // Find variant by product, size, and color
    Optional<ProductVariant> findByProductProductIdAndSizeSizeIdAndColorColorId(Long productId, Long sizeId, Long colorId);
    
    // Check if variant exists by product, size, and color
    boolean existsByProductProductIdAndSizeSizeIdAndColorColorId(Long productId, Long sizeId, Long colorId);
    
    // Find variants by size ID
    List<ProductVariant> findBySizeSizeId(Long sizeId);
    
    // Find variants by color ID
    List<ProductVariant> findByColorColorId(Long colorId);
    
    // Count variants by product ID
    long countByProductProductId(Long productId);
    
    // Count variants by status
    long countByStatus(ProductVariant.VariantStatus status);
    
    // Find variants with stock greater than
    @Query("SELECT pv FROM ProductVariant pv WHERE pv.stock > :minStock")
    List<ProductVariant> findByStockGreaterThan(@Param("minStock") Integer minStock);
    
    // Find variants with stock less than or equal to
    @Query("SELECT pv FROM ProductVariant pv WHERE pv.stock <= :maxStock")
    List<ProductVariant> findByStockLessThanEqual(@Param("maxStock") Integer maxStock);
    
    // Find variants by price range
    @Query("SELECT pv FROM ProductVariant pv WHERE pv.price BETWEEN :minPrice AND :maxPrice")
    List<ProductVariant> findByPriceBetween(@Param("minPrice") java.math.BigDecimal minPrice, 
                                           @Param("maxPrice") java.math.BigDecimal maxPrice);
    
    // Find out of stock variants
    @Query("SELECT pv FROM ProductVariant pv WHERE pv.stock = 0")
    List<ProductVariant> findOutOfStockVariants();
    
    // Find low stock variants
    @Query("SELECT pv FROM ProductVariant pv WHERE pv.stock > 0 AND pv.stock <= :threshold")
    List<ProductVariant> findLowStockVariants(@Param("threshold") Integer threshold);
    
    // Get variant statistics by product
    @Query("SELECT COUNT(pv), SUM(pv.stock), AVG(pv.price) FROM ProductVariant pv WHERE pv.product.productId = :productId")
    Object[] getVariantStatsByProduct(@Param("productId") Long productId);
    
    // Find variants by product and size
    List<ProductVariant> findByProductProductIdAndSizeSizeId(Long productId, Long sizeId);
    
    // Find variants by product and color
    List<ProductVariant> findByProductProductIdAndColorColorId(Long productId, Long colorId);
}
