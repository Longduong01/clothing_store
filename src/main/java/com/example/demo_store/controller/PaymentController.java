package com.example.demo_store.controller;

import com.example.demo_store.entity.Payment;
import com.example.demo_store.entity.Order;
import com.example.demo_store.repository.PaymentRepository;
import com.example.demo_store.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    // GET /api/payments - Lấy tất cả thanh toán với pagination
    @GetMapping
    public ResponseEntity<?> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String method
    ) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<Payment> payments;
            if (status != null && method != null) {
                payments = paymentRepository.findByStatusAndPaymentMethod(status, method, pageable);
            } else if (status != null) {
                payments = paymentRepository.findByStatus(status, pageable);
            } else if (method != null) {
                payments = paymentRepository.findByPaymentMethod(method, pageable);
            } else {
                payments = paymentRepository.findAll(pageable);
            }
            
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to fetch payments: " + e.getMessage()));
        }
    }

    // GET /api/payments/{id} - Lấy thanh toán theo ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getPaymentById(@PathVariable Long id) {
        try {
            Optional<Payment> payment = paymentRepository.findById(id);
            if (payment.isPresent()) {
                return ResponseEntity.ok(payment.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to fetch payment: " + e.getMessage()));
        }
    }

    // GET /api/payments/order/{orderId} - Lấy thanh toán theo đơn hàng
    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getPaymentsByOrder(@PathVariable Long orderId) {
        try {
            List<Payment> payments = paymentRepository.findByOrderOrderIdOrderByCreatedAtDesc(orderId);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to fetch order payments: " + e.getMessage()));
        }
    }

    // POST /api/payments - Tạo thanh toán mới
    @PostMapping
    public ResponseEntity<?> createPayment(@RequestBody PaymentCreateRequest request) {
        try {
            // Validate order exists
            if (!orderRepository.existsById(request.getOrderId())) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Order not found"));
            }

            Order order = orderRepository.findById(request.getOrderId()).get();

            // Create payment
            Payment payment = new Payment();
            payment.setOrder(order);
            payment.setAmount(request.getAmount());
            payment.setPaymentMethod(request.getPaymentMethod());
            payment.setStatus(Payment.PaymentStatus.PENDING);
            payment.setTransactionId(request.getTransactionId());
            payment.setNotes(request.getNotes());
            payment.setCreatedAt(LocalDateTime.now());
            payment.setUpdatedAt(LocalDateTime.now());

            Payment savedPayment = paymentRepository.save(payment);
            return ResponseEntity.ok(savedPayment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to create payment: " + e.getMessage()));
        }
    }

    // PUT /api/payments/{id} - Cập nhật thanh toán
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePayment(@PathVariable Long id, @RequestBody PaymentUpdateRequest request) {
        try {
            Optional<Payment> paymentOptional = paymentRepository.findById(id);
            if (paymentOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Payment payment = paymentOptional.get();
            if (request.getStatus() != null) {
                payment.setStatus(Payment.PaymentStatus.valueOf(request.getStatus()));
            }
            if (request.getTransactionId() != null) {
                payment.setTransactionId(request.getTransactionId());
            }
            if (request.getNotes() != null) {
                payment.setNotes(request.getNotes());
            }
            payment.setUpdatedAt(LocalDateTime.now());

            Payment updatedPayment = paymentRepository.save(payment);
            return ResponseEntity.ok(updatedPayment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to update payment: " + e.getMessage()));
        }
    }

    // DELETE /api/payments/{id} - Xóa thanh toán
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePayment(@PathVariable Long id) {
        try {
            if (!paymentRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }

            paymentRepository.deleteById(id);
            return ResponseEntity.ok(new SuccessResponse("Payment deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to delete payment: " + e.getMessage()));
        }
    }

    // POST /api/payments/{id}/process - Xử lý thanh toán
    @PostMapping("/{id}/process")
    public ResponseEntity<?> processPayment(@PathVariable Long id, @RequestBody ProcessPaymentRequest request) {
        try {
            Optional<Payment> paymentOptional = paymentRepository.findById(id);
            if (paymentOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Payment payment = paymentOptional.get();
            
            // Simulate payment processing
            if (request.isSuccess()) {
                payment.setStatus(Payment.PaymentStatus.COMPLETED);
                payment.setTransactionId(request.getTransactionId());
                payment.setProcessedAt(LocalDateTime.now());
            } else {
                payment.setStatus(Payment.PaymentStatus.FAILED);
                payment.setNotes(request.getFailureReason());
            }
            
            payment.setUpdatedAt(LocalDateTime.now());
            Payment updatedPayment = paymentRepository.save(payment);

            return ResponseEntity.ok(updatedPayment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to process payment: " + e.getMessage()));
        }
    }

    // GET /api/payments/stats - Thống kê thanh toán
    @GetMapping("/stats")
    public ResponseEntity<?> getPaymentStats() {
        try {
            long totalPayments = paymentRepository.count();
            long pendingPayments = paymentRepository.countByStatus(Payment.PaymentStatus.PENDING);
            long completedPayments = paymentRepository.countByStatus(Payment.PaymentStatus.COMPLETED);
            long failedPayments = paymentRepository.countByStatus(Payment.PaymentStatus.FAILED);

            PaymentStats stats = new PaymentStats();
            stats.setTotalPayments(totalPayments);
            stats.setPendingPayments(pendingPayments);
            stats.setCompletedPayments(completedPayments);
            stats.setFailedPayments(failedPayments);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to fetch payment stats: " + e.getMessage()));
        }
    }

    // Response classes
    public static class ErrorResponse {
        private String error;
        
        public ErrorResponse(String error) {
            this.error = error;
        }
        
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }

    public static class SuccessResponse {
        private String message;
        
        public SuccessResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class PaymentCreateRequest {
        private Long orderId;
        private BigDecimal amount;
        private String paymentMethod;
        private String transactionId;
        private String notes;

        // Getters and setters
        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class PaymentUpdateRequest {
        private String status;
        private String transactionId;
        private String notes;

        // Getters and setters
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class ProcessPaymentRequest {
        private boolean success;
        private String transactionId;
        private String failureReason;

        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        
        public String getFailureReason() { return failureReason; }
        public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    }

    public static class PaymentStats {
        private long totalPayments;
        private long pendingPayments;
        private long completedPayments;
        private long failedPayments;

        // Getters and setters
        public long getTotalPayments() { return totalPayments; }
        public void setTotalPayments(long totalPayments) { this.totalPayments = totalPayments; }
        
        public long getPendingPayments() { return pendingPayments; }
        public void setPendingPayments(long pendingPayments) { this.pendingPayments = pendingPayments; }
        
        public long getCompletedPayments() { return completedPayments; }
        public void setCompletedPayments(long completedPayments) { this.completedPayments = completedPayments; }
        
        public long getFailedPayments() { return failedPayments; }
        public void setFailedPayments(long failedPayments) { this.failedPayments = failedPayments; }
    }
}
