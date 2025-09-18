package com.example.demo_store.controller;

import com.example.demo_store.entity.User;
import com.example.demo_store.entity.Size;
import com.example.demo_store.entity.Color;
import com.example.demo_store.repository.UserRepository;
import com.example.demo_store.repository.SizeRepository;
import com.example.demo_store.repository.ColorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class TestController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SizeRepository sizeRepository;

    @Autowired
    private ColorRepository colorRepository;

    @GetMapping("/api/test/connection")
    public ResponseEntity<Map<String, Object>> testConnection() {
        Map<String, Object> response = new HashMap<>();

        try {
            // Test database connection by counting records
            long userCount = userRepository.count();

            response.put("status", "success");
            response.put("message", "Database connection successful!");
            response.put("data", Map.of(
                    "users", userCount
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Database connection failed: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/api/test/users")
    public ResponseEntity<List<User>> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // Root mapping moved to HomeController to avoid duplicate mappings

    @GetMapping("/api/test/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello! Spring Boot is working!");
    }

    @GetMapping("/api/test/simple-user")
    public ResponseEntity<Map<String, Object>> getSimpleUser() {
        Map<String, Object> user = new HashMap<>();
        user.put("id", 1L);
        user.put("username", "testuser");
        user.put("email", "test@example.com");
        user.put("role", "CUSTOMER");
        return ResponseEntity.ok(user);
    }

    @GetMapping("/api/test/database")
    public ResponseEntity<?> testDatabase() {
        try {
            Map<String, Object> result = new HashMap<>();

            // Test users table
            long userCount = userRepository.count();
            result.put("users_count", userCount);

            // Test sizes table
            long sizeCount = sizeRepository.count();
            result.put("sizes_count", sizeCount);

            // Test colors table
            long colorCount = colorRepository.count();
            result.put("colors_count", colorCount);

            result.put("database_status", "connected");
            result.put("message", "Database connection successful");

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Database connection failed");
            error.put("message", e.getMessage());
            error.put("cause", e.getCause() != null ? e.getCause().getMessage() : "Unknown");
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/api/test/sizes-simple")
    public ResponseEntity<?> testSizesSimple() {
        try {
            Map<String, Object> result = new HashMap<>();

            // Test direct repository access
            long count = sizeRepository.count();
            result.put("sizes_count", count);

            // Try to get first size
            try {
                List<Size> sizes = sizeRepository.findAll();
                result.put("sizes_data", sizes);
                result.put("message", "Sizes data retrieved successfully");
            } catch (Exception e) {
                result.put("sizes_error", e.getMessage());
                result.put("message", "Count works but findAll fails");
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to test sizes");
            error.put("message", e.getMessage());
            error.put("cause", e.getCause() != null ? e.getCause().getMessage() : "Unknown");
            return ResponseEntity.status(500).body(error);
        }
    }

    // Temporary endpoints to provide data while fixing main controllers
    @GetMapping("/api/test/sizes-data")
    public ResponseEntity<?> getSizesData() {
        try {
            List<Size> sizes = sizeRepository.findAll();
            return ResponseEntity.ok(sizes);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to fetch sizes");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/api/test/colors-data")
    public ResponseEntity<?> getColorsData() {
        try {
            List<Color> colors = colorRepository.findAll();
            return ResponseEntity.ok(colors);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to fetch colors");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // Temporary CRUD endpoints for Sizes
    @PostMapping("/api/test/sizes")
    public ResponseEntity<?> createSize(@RequestBody Map<String, String> request) {
        try {
            String sizeName = request.get("sizeName");
            if (sizeName == null || sizeName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Size name is required"));
            }

            Size size = new Size();
            size.setSizeName(sizeName.trim().toUpperCase());
            Size savedSize = sizeRepository.save(size);
            return ResponseEntity.ok(savedSize);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to create size");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @PutMapping("/api/test/sizes/{id}")
    public ResponseEntity<?> updateSize(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String sizeName = request.get("sizeName");
            if (sizeName == null || sizeName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Size name is required"));
            }

            Optional<Size> existingSize = sizeRepository.findById(id);
            if (!existingSize.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Size size = existingSize.get();
            size.setSizeName(sizeName.trim().toUpperCase());
            Size updatedSize = sizeRepository.save(size);
            return ResponseEntity.ok(updatedSize);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to update size");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @DeleteMapping("/api/test/sizes/{id}")
    public ResponseEntity<?> deleteSize(@PathVariable Long id) {
        try {
            if (!sizeRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }

            sizeRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Size deleted successfully"));
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to delete size");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // Temporary CRUD endpoints for Colors
    @PostMapping("/api/test/colors")
    public ResponseEntity<?> createColor(@RequestBody Map<String, String> request) {
        try {
            String colorName = request.get("colorName");
            if (colorName == null || colorName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Color name is required"));
            }

            Color color = new Color();
            color.setColorName(colorName.trim().toLowerCase());
            Color savedColor = colorRepository.save(color);
            return ResponseEntity.ok(savedColor);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to create color");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @PutMapping("/api/test/colors/{id}")
    public ResponseEntity<?> updateColor(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String colorName = request.get("colorName");
            if (colorName == null || colorName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Color name is required"));
            }

            Optional<Color> existingColor = colorRepository.findById(id);
            if (!existingColor.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Color color = existingColor.get();
            color.setColorName(colorName.trim().toLowerCase());
            Color updatedColor = colorRepository.save(color);
            return ResponseEntity.ok(updatedColor);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to update color");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @DeleteMapping("/api/test/colors/{id}")
    public ResponseEntity<?> deleteColor(@PathVariable Long id) {
        try {
            if (!colorRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }

            colorRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Color deleted successfully"));
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to delete color");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}
