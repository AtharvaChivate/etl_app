package com.etl.pipeline.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for handling file uploads
 */
@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "http://localhost:3000", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
public class FileUploadController {
    
    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);
    private static final String UPLOAD_DIR = "uploads/";
    
    @PostMapping("/upload/csv")
    public ResponseEntity<?> uploadCsvFile(@RequestParam("file") MultipartFile file) {
        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
            }
            
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".csv")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Only CSV files are allowed"));
            }
            
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Generate unique filename to avoid conflicts
            String filename = System.currentTimeMillis() + "_" + originalFilename;
            Path filePath = uploadPath.resolve(filename);
            
            // Save file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            logger.info("File uploaded successfully: {}", filename);
            
            // Return file information
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("filename", filename);
            response.put("originalName", originalFilename);
            response.put("filePath", "uploads/" + filename);
            response.put("size", file.getSize());
            response.put("message", "File uploaded successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            logger.error("Error uploading file", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to upload file: " + e.getMessage()));
        }
    }
    
    @RequestMapping(value = "/upload/csv", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> uploadCsvOptions() {
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/list")
    public ResponseEntity<?> listUploadedFiles() {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                return ResponseEntity.ok(Map.of("files", new String[0]));
            }
            
            String[] files = Files.list(uploadPath)
                .filter(Files::isRegularFile)
                .map(path -> path.getFileName().toString())
                .filter(name -> name.toLowerCase().endsWith(".csv"))
                .toArray(String[]::new);
            
            return ResponseEntity.ok(Map.of("files", files));
            
        } catch (IOException e) {
            logger.error("Error listing files", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to list files: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/delete/{filename}")
    public ResponseEntity<?> deleteFile(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(UPLOAD_DIR, filename);
            
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }
            
            Files.delete(filePath);
            logger.info("File deleted: {}", filename);
            
            return ResponseEntity.ok(Map.of("message", "File deleted successfully"));
            
        } catch (IOException e) {
            logger.error("Error deleting file", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to delete file: " + e.getMessage()));
        }
    }
    
    @PostMapping("/test-connection")
    public ResponseEntity<?> testDatabaseConnection(@RequestBody Map<String, Object> config) {
        try {
            String databaseType = (String) config.get("databaseType");
            String host = (String) config.get("host");
            String port = String.valueOf(config.get("port"));
            String database = (String) config.get("database");
            String username = (String) config.get("username");
            String password = (String) config.get("password");
            String connectionString = (String) config.get("connectionString");
            
            logger.info("Testing database connection: {} at {}:{}", databaseType, host, port);
            
            // Create data source for testing
            com.etl.pipeline.datasource.DataSourceType type = parseDataSourceType(databaseType);
            com.etl.pipeline.datasource.SqlDatabaseSource source = 
                new com.etl.pipeline.datasource.SqlDatabaseSource(type, config);
            
            boolean connectionSuccessful = source.testConnection();
            
            if (connectionSuccessful) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Database connection successful"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Database connection failed"
                ));
            }
            
        } catch (Exception e) {
            logger.error("Database connection test failed", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Connection failed: " + e.getMessage()
            ));
        }
    }
    
    private com.etl.pipeline.datasource.DataSourceType parseDataSourceType(String type) {
        switch (type.toLowerCase()) {
            case "mysql": return com.etl.pipeline.datasource.DataSourceType.MYSQL;
            case "postgresql": return com.etl.pipeline.datasource.DataSourceType.POSTGRESQL;
            case "sqlite": return com.etl.pipeline.datasource.DataSourceType.SQLITE;
            case "mssql": case "sqlserver": return com.etl.pipeline.datasource.DataSourceType.MSSQL;
            case "oracle": return com.etl.pipeline.datasource.DataSourceType.ORACLE;
            default: return com.etl.pipeline.datasource.DataSourceType.SQL_DATABASE;
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        logger.info("Health check endpoint called");
        return ResponseEntity.ok(Map.of(
            "status", "OK",
            "message", "FileUploadController is working",
            "timestamp", System.currentTimeMillis()
        ));
    }
}
