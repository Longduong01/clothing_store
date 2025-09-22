package com.example.demo_store.controller;

import com.example.demo_store.entity.Color;
import com.example.demo_store.entity.Color.ColorStatus;
import com.example.demo_store.entity.ColorImage;
import com.example.demo_store.repository.ColorRepository;
import com.example.demo_store.service.ColorImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/colors")
public class ColorController {

    @Autowired
    private ColorRepository colorRepository;
    
    @Autowired
    private ColorImageService colorImageService;

    // GET /api/colors - Lấy tất cả màu sắc (mặc định chỉ ACTIVE, có thể includeInactive)
    @GetMapping
    public ResponseEntity<List<Color>> getAllColors(@RequestParam(defaultValue = "false") boolean includeInactive) {
        try {
            List<Color> colors;
            if (includeInactive) {
                colors = colorRepository.findAll();
            } else {
                colors = colorRepository.findByStatus(Color.ColorStatus.ACTIVE);
            }
            return ResponseEntity.ok(colors);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // GET /api/colors/{id} - Lấy màu sắc theo ID
    @GetMapping("/{id}")
    public ResponseEntity<Color> getColorById(@PathVariable Long id) {
        try {
            Optional<Color> color = colorRepository.findById(id);
            return color.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // GET /api/colors/name/{name} - Lấy màu sắc theo tên
    @GetMapping("/name/{name}")
    public ResponseEntity<Color> getColorByName(@PathVariable String name) {
        try {
            Optional<Color> color = colorRepository.findByColorName(name);
            return color.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // POST /api/colors - Tạo màu sắc mới
    @PostMapping
    public ResponseEntity<Color> createColor(@RequestBody ColorCreateRequest request) {
        try {
            Color color = new Color();
            color.setColorName(request.getColorName());
            
            Color savedColor = colorRepository.save(color);
            return ResponseEntity.ok(savedColor);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // PUT /api/colors/{id} - Cập nhật màu sắc
    @PutMapping("/{id}")
    public ResponseEntity<Color> updateColor(@PathVariable Long id, @RequestBody ColorUpdateRequest request) {
        try {
            Optional<Color> colorOptional = colorRepository.findById(id);
            if (colorOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Color color = colorOptional.get();
            color.setColorName(request.getColorName());
            
            // Update status if provided
            if (request.getStatus() != null) {
                color.setStatus(ColorStatus.valueOf(request.getStatus()));
            }
            
            Color updatedColor = colorRepository.save(color);
            return ResponseEntity.ok(updatedColor);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // DELETE /api/colors/{id} - Soft delete color (set status to INACTIVE)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteColor(@PathVariable Long id) {
        try {
            Optional<Color> colorOptional = colorRepository.findById(id);
            if (colorOptional.isPresent()) {
                Color color = colorOptional.get();
                color.setStatus(Color.ColorStatus.INACTIVE);
                colorRepository.save(color);
                return ResponseEntity.ok(new SuccessResponse("Color deactivated successfully"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to deactivate color: " + e.getMessage()));
        }
    }

    // GET /api/colors/count - Đếm số lượng màu sắc
    @GetMapping("/count")
    public ResponseEntity<Long> getColorCount() {
        try {
            long count = colorRepository.count();
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

    public static class ColorCreateRequest {
        private String colorName;

        public String getColorName() { return colorName; }
        public void setColorName(String colorName) { this.colorName = colorName; }
    }

    public static class ColorUpdateRequest {
        private String colorName;
        private String status;

        public String getColorName() { return colorName; }
        public void setColorName(String colorName) { this.colorName = colorName; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
    
    // GET /api/colors/{id}/images - Lấy ảnh của màu sắc
    @GetMapping("/{id}/images")
    public ResponseEntity<List<ColorImage>> getColorImages(@PathVariable Long id) {
        List<ColorImage> images = colorImageService.getColorImages(id.intValue());
        return ResponseEntity.ok(images);
    }
    
    // POST /api/colors/{id}/images - Upload ảnh cho màu sắc
    @PostMapping(value = "/{id}/images", consumes = "multipart/form-data")
    public ResponseEntity<List<ColorImage>> uploadColorImages(
            @PathVariable Long id,
            @RequestParam("images") MultipartFile[] images) {
        try {
            List<ColorImage> savedImages = colorImageService.saveColorImages(id.intValue(), images);
            return ResponseEntity.ok(savedImages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // PUT /api/colors/{id}/images/{imageId}/primary - Đặt ảnh làm ảnh chính
    @PutMapping("/{id}/images/{imageId}/primary")
    public ResponseEntity<ColorImage> setPrimaryColorImage(@PathVariable Long id, @PathVariable Integer imageId) {
        ColorImage primaryImage = colorImageService.updatePrimaryImage(id.intValue(), imageId);
        if (primaryImage != null) {
            return ResponseEntity.ok(primaryImage);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // DELETE /api/colors/{id}/images - Xóa tất cả ảnh của màu sắc
    @DeleteMapping("/{id}/images")
    public ResponseEntity<Void> deleteColorImages(@PathVariable Long id) {
        try {
            colorImageService.deleteColorImages(id.intValue());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // DELETE /api/colors/{id}/images/{imageId} - Xóa 1 ảnh của màu sắc
    @DeleteMapping("/{id}/images/{imageId}")
    public ResponseEntity<Void> deleteColorImage(
            @PathVariable Long id,
            @PathVariable Long imageId) {
        try {
            colorImageService.deleteColorImage(id.intValue(), imageId.intValue());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
