package com.example.demo_store.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

@Entity
@Table(name = "Users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;
    
    @NotBlank(message = "Username is required")
    @jakarta.validation.constraints.Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;
    
    @NotBlank(message = "Password is required")
    @jakarta.validation.constraints.Size(min = 6, message = "Password must be at least 6 characters")
    @Column(name = "password", nullable = false)
    @JsonIgnore
    private String password;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @jakarta.validation.constraints.Size(max = 100, message = "Email must not exceed 100 characters")
    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;
    
    @Pattern(regexp = "^[0-9+\\-\\s()]*$", message = "Phone number should contain only digits, +, -, spaces, and parentheses")
    @jakarta.validation.constraints.Size(max = 20, message = "Phone number must not exceed 20 characters")
    @Column(name = "phone", length = 20)
    private String phone;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role = UserRole.CUSTOMER;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum UserRole {
        CUSTOMER, ADMIN
    }
}
