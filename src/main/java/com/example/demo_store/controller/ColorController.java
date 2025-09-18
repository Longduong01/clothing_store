package com.example.demo_store.controller;

import com.example.demo_store.entity.Color;
import com.example.demo_store.repository.ColorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/colors")
public class ColorController {

    @Autowired
    private ColorRepository colorRepository;

    // GET /api/colors - Lấy tất cả màu sắc
    @GetMapping
    public ResponseEntity<List<Color>> getAllColors() {
        try {
            List<Color> colors = colorRepository.findAll();
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
            
            Color updatedColor = colorRepository.save(color);
            return ResponseEntity.ok(updatedColor);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // DELETE /api/colors/{id} - Xóa màu sắc
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteColor(@PathVariable Long id) {
        try {
            if (!colorRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }

            colorRepository.deleteById(id);
            return ResponseEntity.ok(new SuccessResponse("Color deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to delete color: " + e.getMessage()));
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

        public String getColorName() { return colorName; }
        public void setColorName(String colorName) { this.colorName = colorName; }
    }
}
