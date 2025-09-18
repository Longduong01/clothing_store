package com.example.demo_store.repository;

import com.example.demo_store.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    // Find order items by order ID
    List<OrderItem> findByOrderOrderIdOrderByCreatedAtAsc(Long orderId);
    
    // Find order items by order ID (descending)
    List<OrderItem> findByOrderOrderIdOrderByCreatedAtDesc(Long orderId);
    
    // Find order items by product ID
    List<OrderItem> findByProductProductId(Long productId);
    
    // Delete order items by order ID
    void deleteByOrderOrderId(Long orderId);
    
    // Count order items by order ID
    long countByOrderOrderId(Long orderId);
    
    // Get total quantity sold for a product
    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.product.productId = :productId")
    Long getTotalQuantitySoldByProduct(@Param("productId") Long productId);
    
    // Get total revenue for a product
    @Query("SELECT SUM(oi.price * oi.quantity) FROM OrderItem oi WHERE oi.product.productId = :productId")
    java.math.BigDecimal getTotalRevenueByProduct(@Param("productId") Long productId);
    
    // Find top selling products
    @Query("SELECT oi.product.productId, oi.product.productName, SUM(oi.quantity) as totalQuantity " +
           "FROM OrderItem oi GROUP BY oi.product.productId, oi.product.productName " +
           "ORDER BY totalQuantity DESC")
    List<Object[]> findTopSellingProducts();
    
    // Find order items by date range
    @Query("SELECT oi FROM OrderItem oi WHERE oi.createdAt BETWEEN :startDate AND :endDate")
    List<OrderItem> findByDateRange(@Param("startDate") java.time.LocalDateTime startDate, 
                                   @Param("endDate") java.time.LocalDateTime endDate);
}
