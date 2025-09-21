package com.example.demo_store.controller;

import com.example.demo_store.entity.Brand;
import com.example.demo_store.repository.BrandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/brands")
public class BrandController {
    
    @Autowired
    private BrandRepository brandRepository;
    
    // GET /api/brands - Lấy tất cả brands (mặc định chỉ active)
    @GetMapping
    public ResponseEntity<List<Brand>> getAllBrands(@RequestParam(defaultValue = "false") boolean includeInactive) {
        try {
            List<Brand> brands;
            if (includeInactive) {
                brands = brandRepository.findAll();
            } else {
                brands = brandRepository.findByStatus(Brand.BrandStatus.ACTIVE);
            }
            return ResponseEntity.ok(brands);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // GET /api/brands/{id} - Lấy brand theo ID
    @GetMapping("/{id}")
    public ResponseEntity<Brand> getBrandById(@PathVariable Long id) {
        try {
            Optional<Brand> brand = brandRepository.findById(id);
            return brand.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // GET /api/brands/name/{name} - Lấy brand theo tên
    @GetMapping("/name/{name}")
    public ResponseEntity<Brand> getBrandByName(@PathVariable String name) {
        try {
            Optional<Brand> brand = brandRepository.findByBrandName(name);
            return brand.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // POST /api/brands - Tạo brand mới
    @PostMapping
    public ResponseEntity<Brand> createBrand(@RequestBody Brand brand) {
        try {
            Brand savedBrand = brandRepository.save(brand);
            return ResponseEntity.ok(savedBrand);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // PUT /api/brands/{id} - Cập nhật brand
    @PutMapping("/{id}")
    public ResponseEntity<Brand> updateBrand(@PathVariable Long id, @RequestBody Brand brandDetails) {
        try {
            Optional<Brand> brandOptional = brandRepository.findById(id);
            if (brandOptional.isPresent()) {
                Brand brand = brandOptional.get();
                brand.setBrandName(brandDetails.getBrandName());
                
                // Update status if provided
                if (brandDetails.getStatus() != null) {
                    brand.setStatus(brandDetails.getStatus());
                }
                
                Brand updatedBrand = brandRepository.save(brand);
                return ResponseEntity.ok(updatedBrand);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // DELETE /api/brands/{id} - Soft delete brand (set status to INACTIVE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBrand(@PathVariable Long id) {
        try {
            Optional<Brand> brandOptional = brandRepository.findById(id);
            if (brandOptional.isPresent()) {
                Brand brand = brandOptional.get();
                brand.setStatus(Brand.BrandStatus.INACTIVE);
                brandRepository.save(brand);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // GET /api/brands/count - Đếm số lượng brands
    @GetMapping("/count")
    public ResponseEntity<Long> getBrandCount() {
        try {
            long count = brandRepository.count();
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
