package com.etl.pipeline.controller;

import com.etl.pipeline.service.DatabricksMetadataTransformationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Controller for DAG (Pipeline) save and load operations
 */
@RestController
@RequestMapping("/api/dags")
@CrossOrigin(origins = "http://localhost:3000", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class DagController {
    
    private static final Logger logger = LoggerFactory.getLogger(DagController.class);
    private static final String DAG_DIR = "saved-dags/";
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    private DatabricksMetadataTransformationService databricksTransformationService;
    
    @PostMapping("/save")
    public ResponseEntity<?> saveDag(@RequestBody Map<String, Object> dagData) {
        try {
            logger.info("Received DAG save request: {}", dagData.get("name"));
            
            // Validate required fields
            if (dagData.get("name") == null || ((String) dagData.get("name")).trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "error", "DAG name is required"
                    ));
            }
            
            // Create DAG directory if it doesn't exist
            Path dagPath = Paths.get(DAG_DIR);
            if (!Files.exists(dagPath)) {
                Files.createDirectories(dagPath);
            }
            
            // Generate filename with timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String dagName = ((String) dagData.get("name")).replaceAll("[^a-zA-Z0-9_-]", "_");
            String filename = String.format("%s_%s.json", dagName, timestamp);
            
            // Add metadata
            Map<String, Object> dagWithMetadata = new HashMap<>(dagData);
            dagWithMetadata.put("savedAt", LocalDateTime.now().toString());
            dagWithMetadata.put("version", "1.0");
            dagWithMetadata.put("filename", filename);
            
            // Save to file
            Path filePath = dagPath.resolve(filename);
            objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(filePath.toFile(), dagWithMetadata);
            
            logger.info("DAG saved successfully: {}", filename);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "filename", filename,
                "message", "DAG saved successfully",
                "savedAt", LocalDateTime.now().toString()
            ));
            
        } catch (IOException e) {
            logger.error("Error saving DAG", e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "error", "Failed to save DAG: " + e.getMessage()
                ));
        } catch (Exception e) {
            logger.error("Unexpected error saving DAG", e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "error", "Unexpected error: " + e.getMessage()
                ));
        }
    }
    
    @RequestMapping(value = "/save", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> saveDagOptions() {
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/list")
    public ResponseEntity<?> listSavedDags() {
        try {
            Path dagPath = Paths.get(DAG_DIR);
            if (!Files.exists(dagPath)) {
                return ResponseEntity.ok(Map.of("dags", new ArrayList<>()));
            }
            
            List<Map<String, Object>> dags = new ArrayList<>();
            
            Files.list(dagPath)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().toLowerCase().endsWith(".json"))
                .forEach(path -> {
                    try {
                        Map<String, Object> dagData = objectMapper.readValue(path.toFile(), Map.class);
                        
                        Map<String, Object> dagInfo = new HashMap<>();
                        dagInfo.put("filename", path.getFileName().toString());
                        dagInfo.put("name", dagData.get("name"));
                        dagInfo.put("savedAt", dagData.get("savedAt"));
                        dagInfo.put("nodeCount", getDagNodeCount(dagData));
                        dagInfo.put("description", dagData.get("description"));
                        
                        dags.add(dagInfo);
                    } catch (IOException e) {
                        logger.warn("Error reading DAG file: {}", path.getFileName(), e);
                    }
                });
            
            // Sort by saved date (newest first)
            dags.sort((a, b) -> {
                String dateA = (String) a.get("savedAt");
                String dateB = (String) b.get("savedAt");
                if (dateA == null && dateB == null) return 0;
                if (dateA == null) return 1;
                if (dateB == null) return -1;
                return dateB.compareTo(dateA);
            });
            
            return ResponseEntity.ok(Map.of("dags", dags));
            
        } catch (IOException e) {
            logger.error("Error listing DAGs", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to list DAGs: " + e.getMessage()));
        }
    }
    
    @GetMapping("/load/{filename}")
    public ResponseEntity<?> loadDag(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(DAG_DIR, filename);
            
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> dagData = objectMapper.readValue(filePath.toFile(), Map.class);
            
            logger.info("DAG loaded successfully: {}", filename);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "dag", dagData,
                "message", "DAG loaded successfully"
            ));
            
        } catch (IOException e) {
            logger.error("Error loading DAG: {}", filename, e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to load DAG: " + e.getMessage()));
        }
    }
    
    @PostMapping("/upload")
    public ResponseEntity<?> uploadDag(@RequestParam("file") MultipartFile file) {
        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
            }
            
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".json")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Only JSON files are allowed"));
            }
            
            // Validate JSON content
            Map<String, Object> dagData;
            try {
                dagData = objectMapper.readValue(file.getInputStream(), Map.class);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid JSON format"));
            }
            
            // Create DAG directory if it doesn't exist
            Path dagPath = Paths.get(DAG_DIR);
            if (!Files.exists(dagPath)) {
                Files.createDirectories(dagPath);
            }
            
            // Generate unique filename
            String filename = System.currentTimeMillis() + "_" + originalFilename;
            Path filePath = dagPath.resolve(filename);
            
            // Save uploaded DAG
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            logger.info("DAG uploaded successfully: {}", filename);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "filename", filename,
                "dag", dagData,
                "message", "DAG uploaded and loaded successfully"
            ));
            
        } catch (IOException e) {
            logger.error("Error uploading DAG", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to upload DAG: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/delete/{filename}")
    public ResponseEntity<?> deleteDag(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(DAG_DIR, filename);
            
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }
            
            Files.delete(filePath);
            logger.info("DAG deleted: {}", filename);
            
            return ResponseEntity.ok(Map.of("message", "DAG deleted successfully"));
            
        } catch (IOException e) {
            logger.error("Error deleting DAG", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to delete DAG: " + e.getMessage()));
        }
    }
    
    @PostMapping("/export-metadata")
    public ResponseEntity<?> exportDagMetadata(@RequestBody Map<String, Object> dagData) {
        try {
            logger.info("Exporting DAG metadata for: {}", dagData.get("name"));
            
            // Transform to Databricks format
            Map<String, Object> databricksMetadata = databricksTransformationService.transformToDatabricksFormat(dagData);
            
            logger.info("DAG metadata exported successfully for: {}", dagData.get("name"));
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "DAG metadata exported successfully", 
                "metadata", databricksMetadata
            ));
            
        } catch (Exception e) {
            logger.error("Error exporting DAG metadata", e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "error", "Failed to export DAG metadata: " + e.getMessage()
                ));
        }
    }
    
    @PostMapping("/export-databricks")
    public ResponseEntity<?> exportDatabricksMetadata(@RequestBody Map<String, Object> dagData) {
        try {
            logger.info("Exporting Databricks metadata for: {}", dagData.get("name"));
            
            // Transform to Databricks format
            Map<String, Object> databricksMetadata = databricksTransformationService.transformToDatabricksFormat(dagData);
            
            logger.info("Databricks metadata exported successfully for: {}", dagData.get("name"));
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Databricks metadata exported successfully", 
                "metadata", databricksMetadata
            ));
            
        } catch (Exception e) {
            logger.error("Error exporting Databricks metadata", e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "error", "Failed to export Databricks metadata: " + e.getMessage()
                ));
        }
    }

    private int getDagNodeCount(Map<String, Object> dagData) {
        try {
            List<?> nodes = (List<?>) dagData.get("nodes");
            return nodes != null ? nodes.size() : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}
