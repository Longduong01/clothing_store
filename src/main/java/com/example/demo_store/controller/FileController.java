package com.example.demo_store.controller;

import com.example.demo_store.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
@RequestMapping("/api/files")
public class FileController {
    
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);
    
    @Autowired
    private FileStorageService fileStorageService;
    
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".webp"};
    
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        logger.info("Upload request received for file: {}", file.getOriginalFilename());
        
        try {
            // Validate file
            if (file.isEmpty()) {
                logger.warn("Upload failed: File is empty");
                return ResponseEntity.badRequest().body(new ErrorResponse("File is empty"));
            }
            
            if (file.getSize() > MAX_FILE_SIZE) {
                logger.warn("Upload failed: File too large. Size: {} bytes", file.getSize());
                return ResponseEntity.badRequest().body(new ErrorResponse("File size exceeds 5MB limit"));
            }
            
            // Validate file extension
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !isValidFileExtension(originalFilename)) {
                logger.warn("Upload failed: Invalid file extension. File: {}", originalFilename);
                return ResponseEntity.badRequest().body(new ErrorResponse("Invalid file type. Only JPG, PNG, GIF, WEBP are allowed"));
            }
            
            String fileName = fileStorageService.storeFile(file);
            logger.info("File uploaded successfully: {}", fileName);
            
            return ResponseEntity.ok().body(new UploadFileResponse(
                fileName,
                file.getContentType(),
                file.getSize()
            ));
        } catch (Exception e) {
            logger.error("Upload failed for file: {}", file.getOriginalFilename(), e);
            return ResponseEntity.badRequest().body(new ErrorResponse("Could not upload file: " + e.getMessage()));
        }
    }
    
    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        logger.info("Download request for file: {}", fileName);
        
        try {
            Resource resource = fileStorageService.loadFileAsResource(fileName);
            
            String contentType = getContentType(resource, request);
            
            logger.info("File downloaded successfully: {}", fileName);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            logger.error("Download failed for file: {}", fileName, e);
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/view/{fileName:.+}")
    public ResponseEntity<Resource> viewFile(@PathVariable String fileName, HttpServletRequest request) {
        logger.info("View request for file: {}", fileName);
        
        try {
            Resource resource = fileStorageService.loadFileAsResource(fileName);
            
            String contentType = getContentType(resource, request);
            
            logger.info("File viewed successfully: {}", fileName);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (Exception e) {
            logger.error("View failed for file: {}", fileName, e);
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/delete/{fileName:.+}")
    public ResponseEntity<?> deleteFile(@PathVariable String fileName) {
        logger.info("Delete request for file: {}", fileName);
        
        try {
            fileStorageService.deleteFile(fileName);
            logger.info("File deleted successfully: {}", fileName);
            return ResponseEntity.ok().body(new SuccessResponse("File deleted successfully"));
        } catch (Exception e) {
            logger.error("Delete failed for file: {}", fileName, e);
            return ResponseEntity.badRequest().body(new ErrorResponse("Could not delete file: " + e.getMessage()));
        }
    }
    
    // Helper methods
    private boolean isValidFileExtension(String filename) {
        if (filename == null) return false;
        
        String lowerCaseFilename = filename.toLowerCase();
        for (String extension : ALLOWED_EXTENSIONS) {
            if (lowerCaseFilename.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }
    
    private String getContentType(Resource resource, HttpServletRequest request) {
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            logger.warn("Could not determine file type for: {}", resource.getFilename());
        }
        
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        
        return contentType;
    }
    
    // Response classes
    public static class UploadFileResponse {
        private String fileName;
        private String fileDownloadUri;
        private String fileType;
        private long size;
        
        public UploadFileResponse(String fileName, String fileType, long size) {
            this.fileName = fileName;
            this.fileDownloadUri = "/api/files/download/" + fileName;
            this.fileType = fileType;
            this.size = size;
        }
        
        // Getters and setters
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        
        public String getFileDownloadUri() { return fileDownloadUri; }
        public void setFileDownloadUri(String fileDownloadUri) { this.fileDownloadUri = fileDownloadUri; }
        
        public String getFileType() { return fileType; }
        public void setFileType(String fileType) { this.fileType = fileType; }
        
        public long getSize() { return size; }
        public void setSize(long size) { this.size = size; }
    }
    
    public static class SuccessResponse {
        private String message;
        
        public SuccessResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
    
    public static class ErrorResponse {
        private String error;
        
        public ErrorResponse(String error) {
            this.error = error;
        }
        
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }
}
