package com.etl.pipeline.controller;

import com.etl.pipeline.model.ExecutionResult;
import com.etl.pipeline.model.Pipeline;
import com.etl.pipeline.service.PipelineExecutionService;
import com.etl.pipeline.service.PipelineValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/pipeline")
@CrossOrigin(origins = "http://localhost:3000")
public class PipelineController {
    
    private static final Logger logger = LoggerFactory.getLogger(PipelineController.class);
    
    @Autowired
    private PipelineExecutionService executionService;
    
    @Autowired
    private PipelineValidationService validationService;
    
    @PostMapping("/execute")
    public ResponseEntity<?> executePipeline(@RequestBody Pipeline pipeline) {
        try {
            logger.info("Received pipeline execution request: {}", pipeline);
            
            // Log detailed node data for debugging
            if (pipeline.getNodes() != null) {
                for (var node : pipeline.getNodes()) {
                    logger.info("Node {}: type={}, data={}", node.getId(), node.getType(), node.getData());
                }
            }
            
            // Validate pipeline first
            var validationResult = validationService.validatePipeline(pipeline);
            if (!validationResult.isValid()) {
                logger.warn("Pipeline validation failed: {}", validationResult.getErrors());
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Pipeline validation failed",
                    "details", validationResult.getErrors()
                ));
            }
            
            // Execute pipeline
            ExecutionResult result = executionService.executePipeline(pipeline);
            
            logger.info("Pipeline execution completed: {}", result);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Pipeline executed successfully",
                "executionId", result.getExecutionId() != null ? result.getExecutionId() : "unknown",
                "recordsProcessed", result.getRecordsProcessed() != null ? result.getRecordsProcessed() : 0L,
                "outputLocation", result.getOutputLocation() != null ? result.getOutputLocation() : "unknown",
                "executionTime", result.getExecutionTimeMs() + "ms"
            ));
            
        } catch (Exception e) {
            logger.error("Pipeline execution failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "Pipeline execution failed: " + e.getMessage(),
                "details", e.getClass().getSimpleName()
            ));
        }
    }
    
    @PostMapping("/validate")
    public ResponseEntity<?> validatePipeline(@RequestBody Pipeline pipeline) {
        try {
            logger.info("Received pipeline validation request: {}", pipeline);
            
            var validationResult = validationService.validatePipeline(pipeline);
            
            return ResponseEntity.ok(Map.of(
                "valid", validationResult.isValid(),
                "errors", validationResult.getErrors(),
                "warnings", validationResult.getWarnings()
            ));
            
        } catch (Exception e) {
            logger.error("Pipeline validation failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Validation failed: " + e.getMessage()
            ));
        }
    }
    
    @GetMapping("/status/{executionId}")
    public ResponseEntity<?> getExecutionStatus(@PathVariable String executionId) {
        try {
            ExecutionResult result = executionService.getExecutionStatus(executionId);
            
            if (result == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Failed to get execution status for ID: " + executionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Failed to get execution status: " + e.getMessage()
            ));
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "healthy",
            "service", "Data Pipeline Builder",
            "timestamp", System.currentTimeMillis()
        ));
    }
    
    @PostMapping("/test")
    public ResponseEntity<?> testEndpoint(@RequestBody(required = false) Object payload) {
        logger.info("Test endpoint called with payload: {}", payload);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Test endpoint working correctly");
        response.put("timestamp", System.currentTimeMillis());
        response.put("receivedPayload", payload != null);
        
        return ResponseEntity.ok(response);
    }
}
