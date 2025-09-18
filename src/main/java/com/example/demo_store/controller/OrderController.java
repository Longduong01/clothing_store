package com.example.demo_store.controller;

import com.example.demo_store.entity.Order;
import com.example.demo_store.entity.OrderItem;
import com.example.demo_store.repository.OrderRepository;
import com.example.demo_store.repository.OrderItemRepository;
import com.example.demo_store.repository.UserRepository;
import com.example.demo_store.repository.ProductRepository;
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
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    // GET /api/orders - Lấy tất cả đơn hàng với pagination
    @GetMapping
    public ResponseEntity<?> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long customerId
    ) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<Order> orders;
            if (status != null && customerId != null) {
                orders = orderRepository.findByStatusAndUserUserId(status, customerId, pageable);
            } else if (status != null) {
                orders = orderRepository.findByStatus(status, pageable);
            } else if (customerId != null) {
                orders = orderRepository.findByUserUserId(customerId, pageable);
            } else {
                orders = orderRepository.findAll(pageable);
            }
            
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to fetch orders: " + e.getMessage()));
        }
    }

    // GET /api/orders/{id} - Lấy đơn hàng theo ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        try {
            Optional<Order> order = orderRepository.findById(id);
            if (order.isPresent()) {
                return ResponseEntity.ok(order.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to fetch order: " + e.getMessage()));
        }
    }

    // GET /api/orders/customer/{customerId} - Lấy đơn hàng theo khách hàng
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getOrdersByCustomer(@PathVariable Long customerId) {
        try {
            List<Order> orders = orderRepository.findByUserUserIdOrderByCreatedAtDesc(customerId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to fetch customer orders: " + e.getMessage()));
        }
    }

    // POST /api/orders - Tạo đơn hàng mới
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderCreateRequest request) {
        try {
            // Validate customer exists
            if (!userRepository.existsById(request.getCustomerId())) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Customer not found"));
            }

            // Create order
            Order order = new Order();
            order.setUser(userRepository.findById(request.getCustomerId()).get());
            order.setStatus(Order.OrderStatus.PENDING);
            order.setTotalAmount(BigDecimal.ZERO);
            order.setShippingAddress(request.getShippingAddress());
            order.setNotes(request.getNotes());
            order.setCreatedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());

            Order savedOrder = orderRepository.save(order);

            // Create order items
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (OrderItemRequest itemRequest : request.getItems()) {
                if (!productRepository.existsById(itemRequest.getProductId())) {
                    return ResponseEntity.badRequest().body(new ErrorResponse("Product not found: " + itemRequest.getProductId()));
                }

                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(savedOrder);
                orderItem.setProduct(productRepository.findById(itemRequest.getProductId()).get());
                orderItem.setQuantity(itemRequest.getQuantity());
                orderItem.setPrice(itemRequest.getPrice());
                orderItem.setCreatedAt(LocalDateTime.now());
                orderItem.setUpdatedAt(LocalDateTime.now());

                orderItemRepository.save(orderItem);
                totalAmount = totalAmount.add(itemRequest.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
            }

            // Update total amount
            savedOrder.setTotalAmount(totalAmount);
            orderRepository.save(savedOrder);

            return ResponseEntity.ok(savedOrder);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to create order: " + e.getMessage()));
        }
    }

    // PUT /api/orders/{id} - Cập nhật đơn hàng
    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrder(@PathVariable Long id, @RequestBody OrderUpdateRequest request) {
        try {
            Optional<Order> orderOptional = orderRepository.findById(id);
            if (orderOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Order order = orderOptional.get();
            if (request.getStatus() != null) {
                order.setStatus(Order.OrderStatus.valueOf(request.getStatus()));
            }
            if (request.getShippingAddress() != null) {
                order.setShippingAddress(request.getShippingAddress());
            }
            if (request.getNotes() != null) {
                order.setNotes(request.getNotes());
            }
            order.setUpdatedAt(LocalDateTime.now());

            Order updatedOrder = orderRepository.save(order);
            return ResponseEntity.ok(updatedOrder);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to update order: " + e.getMessage()));
        }
    }

    // DELETE /api/orders/{id} - Xóa đơn hàng
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long id) {
        try {
            if (!orderRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }

            // Delete order items first
            orderItemRepository.deleteByOrderOrderId(id);
            // Delete order
            orderRepository.deleteById(id);

            return ResponseEntity.ok(new SuccessResponse("Order deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to delete order: " + e.getMessage()));
        }
    }

    // GET /api/orders/{id}/items - Lấy chi tiết đơn hàng
    @GetMapping("/{id}/items")
    public ResponseEntity<?> getOrderItems(@PathVariable Long id) {
        try {
            List<OrderItem> items = orderItemRepository.findByOrderOrderIdOrderByCreatedAtAsc(id);
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to fetch order items: " + e.getMessage()));
        }
    }

    // GET /api/orders/stats - Thống kê đơn hàng
    @GetMapping("/stats")
    public ResponseEntity<?> getOrderStats() {
        try {
            long totalOrders = orderRepository.count();
            long pendingOrders = orderRepository.countByStatus(Order.OrderStatus.PENDING);
            long completedOrders = orderRepository.countByStatus(Order.OrderStatus.COMPLETED);
            long cancelledOrders = orderRepository.countByStatus(Order.OrderStatus.CANCELLED);

            OrderStats stats = new OrderStats();
            stats.setTotalOrders(totalOrders);
            stats.setPendingOrders(pendingOrders);
            stats.setCompletedOrders(completedOrders);
            stats.setCancelledOrders(cancelledOrders);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to fetch order stats: " + e.getMessage()));
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

    public static class OrderCreateRequest {
        private Long customerId;
        private String shippingAddress;
        private String notes;
        private List<OrderItemRequest> items;

        // Getters and setters
        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }
        
        public String getShippingAddress() { return shippingAddress; }
        public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
        
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
        
        public List<OrderItemRequest> getItems() { return items; }
        public void setItems(List<OrderItemRequest> items) { this.items = items; }
    }

    public static class OrderItemRequest {
        private Long productId;
        private Integer quantity;
        private BigDecimal price;

        // Getters and setters
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
    }

    public static class OrderUpdateRequest {
        private String status;
        private String shippingAddress;
        private String notes;

        // Getters and setters
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getShippingAddress() { return shippingAddress; }
        public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
        
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class OrderStats {
        private long totalOrders;
        private long pendingOrders;
        private long completedOrders;
        private long cancelledOrders;

        // Getters and setters
        public long getTotalOrders() { return totalOrders; }
        public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }
        
        public long getPendingOrders() { return pendingOrders; }
        public void setPendingOrders(long pendingOrders) { this.pendingOrders = pendingOrders; }
        
        public long getCompletedOrders() { return completedOrders; }
        public void setCompletedOrders(long completedOrders) { this.completedOrders = completedOrders; }
        
        public long getCancelledOrders() { return cancelledOrders; }
        public void setCancelledOrders(long cancelledOrders) { this.cancelledOrders = cancelledOrders; }
    }
}
