package com.example.demo_store.repository;

import com.example.demo_store.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    
    // Find cart by user ID
    Optional<Cart> findByUserUserId(Long userId);
    
    // Find carts by user ID
    List<Cart> findByUserUserIdOrderByCreatedAtDesc(Long userId);
    
    // Check if cart exists for user
    boolean existsByUserUserId(Long userId);
    
    // Delete cart by user ID
    void deleteByUserUserId(Long userId);
    
    // Find carts created after a specific date
    @Query("SELECT c FROM Cart c WHERE c.createdAt >= :date")
    List<Cart> findCartsCreatedAfter(@Param("date") java.time.LocalDateTime date);
    
    // Count carts by user
    long countByUserUserId(Long userId);
    
    // Find active carts (created within last 30 days)
    @Query("SELECT c FROM Cart c WHERE c.createdAt >= :date")
    List<Cart> findActiveCarts(@Param("date") java.time.LocalDateTime date);
}
