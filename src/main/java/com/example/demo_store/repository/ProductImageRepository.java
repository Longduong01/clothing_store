package com.example.demo_store.repository;

import com.example.demo_store.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Integer> {
    
    List<ProductImage> findByProductIdOrderByImageOrderAsc(Integer productId);
    
    List<ProductImage> findByProductIdAndIsPrimaryTrue(Integer productId);
    
    @Query("SELECT pi FROM ProductImage pi WHERE pi.productId = :productId ORDER BY pi.isPrimary DESC, pi.imageOrder ASC")
    List<ProductImage> findByProductIdOrderByPrimaryAndOrder(@Param("productId") Integer productId);
    
    void deleteByProductId(Integer productId);
}