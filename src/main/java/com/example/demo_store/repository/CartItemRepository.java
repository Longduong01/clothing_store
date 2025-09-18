package com.example.demo_store.repository;

import com.example.demo_store.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    // Find cart items by cart ID
    List<CartItem> findByCartCartIdOrderByCreatedAtAsc(Long cartId);
    
    // Find cart items by cart ID (descending)
    List<CartItem> findByCartCartIdOrderByCreatedAtDesc(Long cartId);
    
    // Find cart item by cart ID and product ID
    Optional<CartItem> findByCartCartIdAndProductProductId(Long cartId, Long productId);
    
    // Find cart items by product ID
    List<CartItem> findByProductProductId(Long productId);
    
    // Delete cart items by cart ID
    void deleteByCartCartId(Long cartId);
    
    // Count cart items by cart ID
    long countByCartCartId(Long cartId);
    
    // Get total quantity in cart
    @Query("SELECT SUM(ci.quantity) FROM CartItem ci WHERE ci.cart.cartId = :cartId")
    Long getTotalQuantityByCartId(@Param("cartId") Long cartId);
    
    // Get total amount in cart
    @Query("SELECT SUM(ci.price * ci.quantity) FROM CartItem ci WHERE ci.cart.cartId = :cartId")
    java.math.BigDecimal getTotalAmountByCartId(@Param("cartId") Long cartId);
    
    // Find cart items by user ID
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.user.userId = :userId")
    List<CartItem> findByUserId(@Param("userId") Long userId);
    
    // Delete cart items by user ID
    @Query("DELETE FROM CartItem ci WHERE ci.cart.user.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
    
    // Find cart items created after a specific date
    @Query("SELECT ci FROM CartItem ci WHERE ci.createdAt >= :date")
    List<CartItem> findCartItemsCreatedAfter(@Param("date") java.time.LocalDateTime date);
}
