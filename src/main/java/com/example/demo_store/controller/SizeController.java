package com.example.demo_store.controller;

import com.example.demo_store.entity.Size;
import com.example.demo_store.entity.Size.SizeStatus;
import com.example.demo_store.repository.SizeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/sizes")
public class SizeController {

    @Autowired
    private SizeRepository sizeRepository;

    // GET /api/sizes - Lấy tất cả kích thước (mặc định chỉ ACTIVE, có thể includeInactive)
    @GetMapping
    public ResponseEntity<List<Size>> getAllSizes(@RequestParam(defaultValue = "false") boolean includeInactive) {
        try {
            List<Size> sizes;
            if (includeInactive) {
                sizes = sizeRepository.findAll();
            } else {
                sizes = sizeRepository.findByStatus(Size.SizeStatus.ACTIVE);
            }
            return ResponseEntity.ok(sizes);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to fetch sizes");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    // GET /api/sizes/{id} - Lấy kích thước theo ID
    @GetMapping("/{id}")
    public ResponseEntity<Size> getSizeById(@PathVariable Long id) {
        try {
            Optional<Size> size = sizeRepository.findById(id);
            return size.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // GET /api/sizes/name/{name} - Lấy kích thước theo tên
    @GetMapping("/name/{name}")
    public ResponseEntity<Size> getSizeByName(@PathVariable String name) {
        try {
            Optional<Size> size = sizeRepository.findBySizeName(name);
            return size.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // POST /api/sizes - Tạo kích thước mới
    @PostMapping
    public ResponseEntity<Size> createSize(@RequestBody SizeCreateRequest request) {
        try {
            Size size = new Size();
            size.setSizeName(request.getSizeName());
            
            Size savedSize = sizeRepository.save(size);
            return ResponseEntity.ok(savedSize);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // PUT /api/sizes/{id} - Cập nhật kích thước
    @PutMapping("/{id}")
    public ResponseEntity<Size> updateSize(@PathVariable Long id, @RequestBody SizeUpdateRequest request) {
        try {
            Optional<Size> sizeOptional = sizeRepository.findById(id);
            if (sizeOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Size size = sizeOptional.get();
            size.setSizeName(request.getSizeName());
            
            // Update status if provided
            if (request.getStatus() != null) {
                size.setStatus(SizeStatus.valueOf(request.getStatus()));
            }
            
            Size updatedSize = sizeRepository.save(size);
            return ResponseEntity.ok(updatedSize);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // DELETE /api/sizes/{id} - Soft delete size (set status to INACTIVE)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSize(@PathVariable Long id) {
        try {
            Optional<Size> sizeOptional = sizeRepository.findById(id);
            if (sizeOptional.isPresent()) {
                Size size = sizeOptional.get();
                size.setStatus(Size.SizeStatus.INACTIVE);
                sizeRepository.save(size);
                return ResponseEntity.ok(new SuccessResponse("Size deactivated successfully"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to deactivate size: " + e.getMessage()));
        }
    }

    // GET /api/sizes/count - Đếm số lượng kích thước
    @GetMapping("/count")
    public ResponseEntity<Long> getSizeCount() {
        try {
            long count = sizeRepository.count();
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
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

    public static class SizeCreateRequest {
        private String sizeName;

        public String getSizeName() { return sizeName; }
        public void setSizeName(String sizeName) { this.sizeName = sizeName; }
    }

    public static class SizeUpdateRequest {
        private String sizeName;
        private String status;

        public String getSizeName() { return sizeName; }
        public void setSizeName(String sizeName) { this.sizeName = sizeName; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
