package com.example.demo_store.repository;

import com.example.demo_store.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // Find orders by status
    Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);
    
    // Find orders by status (String)
    Page<Order> findByStatus(String status, Pageable pageable);
    
    // Find orders by customer
    Page<Order> findByUserUserId(Long customerId, Pageable pageable);
    
    // Find orders by customer and status
    Page<Order> findByStatusAndUserUserId(String status, Long customerId, Pageable pageable);
    
    // Find orders by customer ordered by creation date
    List<Order> findByUserUserIdOrderByCreatedAtDesc(Long customerId);
    
    // Count orders by status
    long countByStatus(Order.OrderStatus status);
    
    // Count orders by status (String)
    long countByStatus(String status);
    
    // Find orders by date range
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findByDateRange(@Param("startDate") java.time.LocalDateTime startDate, 
                               @Param("endDate") java.time.LocalDateTime endDate);
    
    // Find orders with total amount greater than
    @Query("SELECT o FROM Order o WHERE o.totalAmount >= :minAmount")
    List<Order> findByTotalAmountGreaterThanEqual(@Param("minAmount") java.math.BigDecimal minAmount);
    
    // Get total revenue
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = :status")
    java.math.BigDecimal getTotalRevenueByStatus(@Param("status") Order.OrderStatus status);
    
    // Get order statistics
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    long countOrdersByStatus(@Param("status") Order.OrderStatus status);
}
