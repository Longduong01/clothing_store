package com.example.demo_store.repository;

import com.example.demo_store.entity.Color;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ColorRepository extends JpaRepository<Color, Long> {
    
    // Find color by name
    Optional<Color> findByColorName(String colorName);
    
    // Check if color exists by name
    boolean existsByColorName(String colorName);
    
    // Find colors by name containing
    List<Color> findByColorNameContainingIgnoreCase(String name);
    
    // Get all color names
    @Query("SELECT c.colorName FROM Color c ORDER BY c.colorName")
    List<String> findAllColorNames();
    
    // Count colors
    long count();
    
    // Find colors by name pattern
    @Query("SELECT c FROM Color c WHERE LOWER(c.colorName) LIKE LOWER(CONCAT('%', :pattern, '%'))")
    List<Color> findByColorNamePattern(@Param("pattern") String pattern);
    
    // Soft delete support
    List<Color> findByStatus(Color.ColorStatus status);
    
    @Query("SELECT c FROM Color c WHERE c.status = :status ORDER BY c.colorName")
    List<Color> findByStatusOrderByColorName(@Param("status") Color.ColorStatus status);
}
