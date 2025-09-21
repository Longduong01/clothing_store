package com.example.demo_store.repository;

import com.example.demo_store.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    // Soft delete support
    java.util.List<User> findByStatus(User.UserStatus status);
    
    java.util.List<User> findByRoleAndStatus(User.UserRole role, User.UserStatus status);
}
