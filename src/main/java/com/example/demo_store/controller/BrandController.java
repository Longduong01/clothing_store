package com.example.demo_store.controller;

import com.example.demo_store.entity.Brand;
import com.example.demo_store.repository.BrandRepository;
import com.example.demo_store.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/brands")
public class BrandController {
    
    @Autowired
    private BrandRepository brandRepository;
    
    @Autowired
    private FileStorageService fileStorageService;
    
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
    public ResponseEntity<Brand> createBrand(
            @RequestParam("brandName") String brandName,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "logo", required = false) MultipartFile logo) {
        try {
            Brand brand = new Brand();
            brand.setBrandName(brandName);
            brand.setDescription(description);
            
            // Upload logo if provided
            if (logo != null && !logo.isEmpty()) {
                String fileName = fileStorageService.storeFile(logo);
                String logoUrl = "/api/files/view/" + fileName;
                brand.setLogoUrl(logoUrl);
            }
            
            Brand savedBrand = brandRepository.save(brand);
            return ResponseEntity.ok(savedBrand);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // PUT /api/brands/{id} - Cập nhật brand
    @PutMapping("/{id}")
    public ResponseEntity<Brand> updateBrand(
            @PathVariable Long id,
            @RequestParam("brandName") String brandName,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "logo", required = false) MultipartFile logo,
            @RequestParam(value = "logoUrl", required = false) String logoUrl) {
        try {
            Optional<Brand> brandOptional = brandRepository.findById(id);
            if (brandOptional.isPresent()) {
                Brand brand = brandOptional.get();
                brand.setBrandName(brandName);
                
                if (description != null) {
                    brand.setDescription(description);
                }
                
                // Update status if provided
                if (status != null) {
                    brand.setStatus(Brand.BrandStatus.valueOf(status));
                }
                
                // Handle logo update
                if (logo != null && !logo.isEmpty()) {
                    // Upload new logo
                    String fileName = fileStorageService.storeFile(logo);
                    String newLogoUrl = "/api/files/view/" + fileName;
                    brand.setLogoUrl(newLogoUrl);
                } else if (logoUrl != null && logoUrl.isEmpty()) {
                    // Remove existing logo
                    brand.setLogoUrl(null);
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
