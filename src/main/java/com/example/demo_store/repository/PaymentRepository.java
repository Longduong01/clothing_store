package com.example.demo_store.repository;

import com.example.demo_store.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    // Find payments by status
    Page<Payment> findByStatus(Payment.PaymentStatus status, Pageable pageable);
    
    // Find payments by status (String)
    Page<Payment> findByStatus(String status, Pageable pageable);
    
    // Find payments by payment method
    Page<Payment> findByPaymentMethod(String paymentMethod, Pageable pageable);
    
    // Find payments by status and payment method
    Page<Payment> findByStatusAndPaymentMethod(String status, String paymentMethod, Pageable pageable);
    
    // Find payments by order ID
    List<Payment> findByOrderOrderIdOrderByCreatedAtDesc(Long orderId);
    
    // Find payments by transaction ID
    Optional<Payment> findByTransactionId(String transactionId);
    
    // Count payments by status
    long countByStatus(Payment.PaymentStatus status);
    
    // Count payments by status (String)
    long countByStatus(String status);
    
    // Find payments by date range
    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    List<Payment> findByDateRange(@Param("startDate") java.time.LocalDateTime startDate, 
                                 @Param("endDate") java.time.LocalDateTime endDate);
    
    // Get total amount by status
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = :status")
    java.math.BigDecimal getTotalAmountByStatus(@Param("status") Payment.PaymentStatus status);
    
    // Get total amount by payment method
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.paymentMethod = :paymentMethod")
    java.math.BigDecimal getTotalAmountByPaymentMethod(@Param("paymentMethod") String paymentMethod);
    
    // Find payments by amount range
    @Query("SELECT p FROM Payment p WHERE p.amount BETWEEN :minAmount AND :maxAmount")
    List<Payment> findByAmountRange(@Param("minAmount") java.math.BigDecimal minAmount, 
                                   @Param("maxAmount") java.math.BigDecimal maxAmount);
    
    // Get payment statistics by method
    @Query("SELECT p.paymentMethod, COUNT(p), SUM(p.amount) FROM Payment p GROUP BY p.paymentMethod")
    List<Object[]> getPaymentStatsByMethod();
    
    // Find successful payments
    @Query("SELECT p FROM Payment p WHERE p.status = 'COMPLETED'")
    List<Payment> findSuccessfulPayments();
    
    // Find failed payments
    @Query("SELECT p FROM Payment p WHERE p.status = 'FAILED'")
    List<Payment> findFailedPayments();
}
