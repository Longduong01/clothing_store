package com.example.demo_store.repository;

import com.example.demo_store.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    
    List<ProductVariant> findByProductProductId(Long productId);
    
    List<ProductVariant> findByStatus(ProductVariant.VariantStatus status);
    
    List<ProductVariant> findByProductProductIdAndStatus(Long productId, ProductVariant.VariantStatus status);
    
    @Query("SELECT v FROM ProductVariant v WHERE v.product.productId = :productId AND v.size.sizeId = :sizeId AND v.color.colorId = :colorId")
    List<ProductVariant> findByProductAndSizeAndColor(@Param("productId") Long productId, @Param("sizeId") Long sizeId, @Param("colorId") Long colorId);
    
    Optional<ProductVariant> findBySku(String sku);
    
    @Query("SELECT v FROM ProductVariant v WHERE v.sku LIKE %:keyword%")
    List<ProductVariant> findBySkuContaining(@Param("keyword") String keyword);
    
    @Query("SELECT v FROM ProductVariant v WHERE v.price BETWEEN :minPrice AND :maxPrice")
    List<ProductVariant> findByPriceRange(@Param("minPrice") java.math.BigDecimal minPrice, @Param("maxPrice") java.math.BigDecimal maxPrice);
    
    @Query("SELECT v FROM ProductVariant v WHERE v.stock = 0")
    List<ProductVariant> findOutOfStock();
    
    @Query("SELECT v FROM ProductVariant v WHERE v.stock > 0")
    List<ProductVariant> findInStock();
    
    // Methods for product count calculation
    @Query("SELECT COUNT(v) FROM ProductVariant v WHERE v.size.sizeId = :sizeId AND v.status = :status")
    long countBySizeIdAndStatus(@Param("sizeId") Long sizeId, @Param("status") ProductVariant.VariantStatus status);
    
    @Query("SELECT COUNT(v) FROM ProductVariant v WHERE v.color.colorId = :colorId AND v.status = :status")
    long countByColorIdAndStatus(@Param("colorId") Long colorId, @Param("status") ProductVariant.VariantStatus status);
}