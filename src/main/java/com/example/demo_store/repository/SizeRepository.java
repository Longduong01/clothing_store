package com.example.demo_store.repository;

import com.example.demo_store.entity.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SizeRepository extends JpaRepository<Size, Long> {
    
    // Find size by name
    Optional<Size> findBySizeName(String sizeName);
    
    // Check if size exists by name
    boolean existsBySizeName(String sizeName);
    
    // Find sizes by name containing
    List<Size> findBySizeNameContainingIgnoreCase(String name);
    
    // Get all size names
    @Query("SELECT s.sizeName FROM Size s ORDER BY s.sizeName")
    List<String> findAllSizeNames();
    
    // Count sizes
    long count();
    
    // Find sizes by name pattern
    @Query("SELECT s FROM Size s WHERE LOWER(s.sizeName) LIKE LOWER(CONCAT('%', :pattern, '%'))")
    List<Size> findBySizeNamePattern(@Param("pattern") String pattern);
    
    // Find sizes by numeric range (for clothing sizes like S, M, L, XL, etc.)
    @Query("SELECT s FROM Size s WHERE s.sizeName IN :sizeNames")
    List<Size> findBySizeNameIn(@Param("sizeNames") List<String> sizeNames);
    
    // Soft delete support
    List<Size> findByStatus(Size.SizeStatus status);
    
    @Query("SELECT s FROM Size s WHERE s.status = :status ORDER BY s.sizeName")
    List<Size> findByStatusOrderBySizeName(@Param("status") Size.SizeStatus status);
}
