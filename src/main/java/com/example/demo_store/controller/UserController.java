package com.example.demo_store.controller;

import com.example.demo_store.entity.User;
import com.example.demo_store.repository.UserRepository;
import com.example.demo_store.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserRepository userRepository;
    
    // GET /api/users - Lấy tất cả users
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            List<UserDTO> userDTOs = users.stream()
                    .map(UserDTO::fromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(userDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // GET /api/users/{id} - Lấy user theo ID
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        try {
            Optional<User> user = userRepository.findById(id);
            return user.map(u -> ResponseEntity.ok(UserDTO.fromEntity(u)))
                      .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // GET /api/users/username/{username} - Lấy user theo username
    @GetMapping("/username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        try {
            Optional<User> user = userRepository.findByUsername(username);
            return user.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // GET /api/users/email/{email} - Lấy user theo email
    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        try {
            Optional<User> user = userRepository.findByEmail(email);
            return user.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // POST /api/users - Tạo user mới
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        try {
            // Kiểm tra username và email đã tồn tại chưa
            if (userRepository.existsByUsername(user.getUsername())) {
                return ResponseEntity.badRequest().build();
            }
            if (userRepository.existsByEmail(user.getEmail())) {
                return ResponseEntity.badRequest().build();
            }
            
            User savedUser = userRepository.save(user);
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // PUT /api/users/{id} - Cập nhật user
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        try {
            Optional<User> userOptional = userRepository.findById(id);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                user.setUsername(userDetails.getUsername());
                user.setEmail(userDetails.getEmail());
                user.setPhone(userDetails.getPhone());
                user.setRole(userDetails.getRole());
                
                User updatedUser = userRepository.save(user);
                return ResponseEntity.ok(updatedUser);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // DELETE /api/users/{id} - Xóa user
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            if (userRepository.existsById(id)) {
                userRepository.deleteById(id);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // GET /api/users/count - Đếm số lượng users
    @GetMapping("/count")
    public ResponseEntity<Long> getUserCount() {
        try {
            long count = userRepository.count();
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
