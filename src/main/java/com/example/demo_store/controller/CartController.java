package com.example.demo_store.controller;

import com.example.demo_store.entity.Cart;
import com.example.demo_store.entity.CartItem;
import com.example.demo_store.entity.Product;
import com.example.demo_store.entity.User;
import com.example.demo_store.repository.CartRepository;
import com.example.demo_store.repository.CartItemRepository;
import com.example.demo_store.repository.UserRepository;
import com.example.demo_store.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/carts")
@CrossOrigin(origins = "*")
public class CartController {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    // GET /api/carts/user/{userId} - Lấy giỏ hàng của user
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getCartByUser(@PathVariable Long userId) {
        try {
            Optional<Cart> cart = cartRepository.findByUserUserId(userId);
            if (cart.isPresent()) {
                List<CartItem> items = cartItemRepository.findByCartCartIdOrderByCreatedAtAsc(cart.get().getCartId());
                CartResponse response = new CartResponse();
                response.setCart(cart.get());
                response.setItems(items);
                response.setTotalItems(items.size());
                response.setTotalAmount(calculateTotalAmount(items));
                return ResponseEntity.ok(response);
            } else {
                // Create new cart if not exists
                Cart newCart = createNewCart(userId);
                CartResponse response = new CartResponse();
                response.setCart(newCart);
                response.setItems(List.of());
                response.setTotalItems(0);
                response.setTotalAmount(BigDecimal.ZERO);
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to fetch cart: " + e.getMessage()));
        }
    }

    // POST /api/carts/user/{userId}/items - Thêm sản phẩm vào giỏ hàng
    @PostMapping("/user/{userId}/items")
    public ResponseEntity<?> addItemToCart(@PathVariable Long userId, @RequestBody AddItemRequest request) {
        try {
            // Validate user exists
            if (!userRepository.existsById(userId)) {
                return ResponseEntity.badRequest().body(new ErrorResponse("User not found"));
            }

            // Validate product exists
            if (!productRepository.existsById(request.getProductId())) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Product not found"));
            }

            // Get or create cart
            Cart cart = cartRepository.findByUserUserId(userId).orElse(createNewCart(userId));

            // Check if item already exists in cart
            Optional<CartItem> existingItem = cartItemRepository.findByCartCartIdAndProductProductId(cart.getCartId(), request.getProductId());
            
            if (existingItem.isPresent()) {
                // Update quantity
                CartItem item = existingItem.get();
                item.setQuantity(item.getQuantity() + request.getQuantity());
                item.setUpdatedAt(LocalDateTime.now());
                cartItemRepository.save(item);
            } else {
                // Create new cart item
                Product product = productRepository.findById(request.getProductId()).get();
                CartItem cartItem = new CartItem();
                cartItem.setCart(cart);
                cartItem.setProduct(product);
                cartItem.setQuantity(request.getQuantity());
                cartItem.setPrice(product.getPrice());
                cartItem.setCreatedAt(LocalDateTime.now());
                cartItem.setUpdatedAt(LocalDateTime.now());
                cartItemRepository.save(cartItem);
            }

            // Return updated cart
            return getCartByUser(userId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to add item to cart: " + e.getMessage()));
        }
    }

    // PUT /api/carts/items/{itemId} - Cập nhật số lượng sản phẩm trong giỏ hàng
    @PutMapping("/items/{itemId}")
    public ResponseEntity<?> updateCartItem(@PathVariable Long itemId, @RequestBody UpdateItemRequest request) {
        try {
            Optional<CartItem> itemOptional = cartItemRepository.findById(itemId);
            if (itemOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            CartItem item = itemOptional.get();
            if (request.getQuantity() <= 0) {
                // Remove item if quantity is 0 or negative
                cartItemRepository.delete(item);
                return ResponseEntity.ok(new SuccessResponse("Item removed from cart"));
            }

            item.setQuantity(request.getQuantity());
            item.setUpdatedAt(LocalDateTime.now());
            cartItemRepository.save(item);

            return ResponseEntity.ok(new SuccessResponse("Cart item updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to update cart item: " + e.getMessage()));
        }
    }

    // DELETE /api/carts/items/{itemId} - Xóa sản phẩm khỏi giỏ hàng
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<?> removeItemFromCart(@PathVariable Long itemId) {
        try {
            if (!cartItemRepository.existsById(itemId)) {
                return ResponseEntity.notFound().build();
            }

            cartItemRepository.deleteById(itemId);
            return ResponseEntity.ok(new SuccessResponse("Item removed from cart"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to remove item from cart: " + e.getMessage()));
        }
    }

    // DELETE /api/carts/user/{userId} - Xóa toàn bộ giỏ hàng
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<?> clearCart(@PathVariable Long userId) {
        try {
            Optional<Cart> cart = cartRepository.findByUserUserId(userId);
            if (cart.isPresent()) {
                cartItemRepository.deleteByCartCartId(cart.get().getCartId());
                return ResponseEntity.ok(new SuccessResponse("Cart cleared successfully"));
            } else {
                return ResponseEntity.ok(new SuccessResponse("Cart is already empty"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to clear cart: " + e.getMessage()));
        }
    }

    // GET /api/carts/user/{userId}/count - Lấy số lượng sản phẩm trong giỏ hàng
    @GetMapping("/user/{userId}/count")
    public ResponseEntity<?> getCartItemCount(@PathVariable Long userId) {
        try {
            Optional<Cart> cart = cartRepository.findByUserUserId(userId);
            if (cart.isPresent()) {
                long count = cartItemRepository.countByCartCartId(cart.get().getCartId());
                return ResponseEntity.ok(new CartCountResponse(count));
            } else {
                return ResponseEntity.ok(new CartCountResponse(0));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to get cart count: " + e.getMessage()));
        }
    }

    // Helper methods
    private Cart createNewCart(Long userId) {
        User user = userRepository.findById(userId).get();
        Cart cart = new Cart();
        cart.setUser(user);
        cart.setCreatedAt(LocalDateTime.now());
        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepository.save(cart);
    }

    private BigDecimal calculateTotalAmount(List<CartItem> items) {
        return items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
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

    public static class CartResponse {
        private Cart cart;
        private List<CartItem> items;
        private int totalItems;
        private BigDecimal totalAmount;

        // Getters and setters
        public Cart getCart() { return cart; }
        public void setCart(Cart cart) { this.cart = cart; }
        
        public List<CartItem> getItems() { return items; }
        public void setItems(List<CartItem> items) { this.items = items; }
        
        public int getTotalItems() { return totalItems; }
        public void setTotalItems(int totalItems) { this.totalItems = totalItems; }
        
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    }

    public static class CartCountResponse {
        private long count;

        public CartCountResponse(long count) {
            this.count = count;
        }

        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }
    }

    public static class AddItemRequest {
        private Long productId;
        private Integer quantity;

        // Getters and setters
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    public static class UpdateItemRequest {
        private Integer quantity;

        // Getters and setters
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }
}
