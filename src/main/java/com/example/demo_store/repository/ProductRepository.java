package com.example.demo_store.repository;

import com.example.demo_store.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    List<Product> findByStatus(Product.ProductStatus status);
    
    List<Product> findByCategoryCategoryId(Long categoryId);
    
    List<Product> findByBrandBrandId(Long brandId);
    
    List<Product> findByCategoryCategoryIdAndStatus(Long categoryId, Product.ProductStatus status);
    
    @Query("SELECT p FROM Product p WHERE p.productName LIKE %:keyword% OR p.description LIKE %:keyword%")
    List<Product> findByKeyword(@Param("keyword") String keyword);
    
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice")
    List<Product> findByPriceRange(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);
    
    Optional<Product> findBySku(String sku);
}
