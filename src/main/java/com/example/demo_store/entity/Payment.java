package com.example.demo_store.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @NotNull(message = "Order is required")
    private Order order;
    
    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Payment amount must be greater than 0")
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;
    
    @NotBlank(message = "Payment method is required")
    @jakarta.validation.constraints.Size(max = 50, message = "Payment method must not exceed 50 characters")
    @Column(name = "payment_method", nullable = false, length = 50)
    private String paymentMethod;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.PENDING;
    
    @NotBlank(message = "Transaction ID is required")
    @jakarta.validation.constraints.Size(max = 100, message = "Transaction ID must not exceed 100 characters")
    @Column(name = "transaction_id", unique = true, nullable = false, length = 100)
    private String transactionId;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @jakarta.validation.constraints.Size(max = 500, message = "Notes must not exceed 500 characters")
    @Column(name = "notes", length = 500)
    private String notes;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum PaymentStatus {
        PENDING, COMPLETED, FAILED, CANCELLED, REFUNDED
    }
}
