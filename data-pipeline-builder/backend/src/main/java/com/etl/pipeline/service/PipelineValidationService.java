package com.etl.pipeline.service;

import com.etl.pipeline.model.Pipeline;
import com.etl.pipeline.model.PipelineNode;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PipelineValidationService {
    
    public ValidationResult validatePipeline(Pipeline pipeline) {
        ValidationResult result = new ValidationResult();
        
        if (pipeline == null) {
            result.addError("Pipeline cannot be null");
            return result;
        }
        
        // Validate nodes
        if (pipeline.getNodes() == null || pipeline.getNodes().isEmpty()) {
            result.addError("Pipeline must contain at least one node");
        } else {
            validateNodes(pipeline, result);
        }
        
        // Validate edges
        if (pipeline.getEdges() != null) {
            validateEdges(pipeline, result);
        }
        
        // Validate execution order
        if (pipeline.getExecutionOrder() == null || pipeline.getExecutionOrder().isEmpty()) {
            result.addWarning("No execution order specified - will use default ordering");
        }
        
        return result;
    }
    
    private void validateNodes(Pipeline pipeline, ValidationResult result) {
        boolean hasSource = false;
        
        for (PipelineNode node : pipeline.getNodes()) {
            if (node.getId() == null || node.getId().trim().isEmpty()) {
                result.addError("Node must have a valid ID");
            }
            
            if (node.getType() == null || node.getType().trim().isEmpty()) {
                result.addError("Node must have a valid type");
            } else {
                // Check for required node types
                if ("csvSource".equals(node.getType())) {
                    hasSource = true;
                    validateCsvSourceNode(node, result);
                } else if (node.getType().endsWith("Source") || 
                          "sqlSource".equals(node.getType()) || 
                          "mysqlSource".equals(node.getType()) || 
                          "postgresqlSource".equals(node.getType()) || 
                          "sqliteSource".equals(node.getType())) {
                    hasSource = true;
                    validateSqlSourceNode(node, result);
                } else if ("sqlOutput".equals(node.getType())) {
                    validateSqlOutputNode(node, result);
                } else if ("csvOutput".equals(node.getType())) {
                    validateCsvOutputNode(node, result);
                } else if ("filter".equals(node.getType())) {
                    validateFilterNode(node, result);
                }
            }
        }
        
        if (!hasSource) {
            result.addError("Pipeline must contain at least one source node");
        }
    }
    
    private void validateCsvSourceNode(PipelineNode node, ValidationResult result) {
        if (node.getData() == null) {
            result.addError("CSV source node must have configuration data");
            return;
        }
        
        String filePath = node.getDataString("filePath");
        if (filePath == null || filePath.trim().isEmpty()) {
            result.addError("CSV source node must specify a file path");
        }
    }
    
    private void validateSqlOutputNode(PipelineNode node, ValidationResult result) {
        if (node.getData() == null) {
            result.addError("SQL output node must have configuration data");
            return;
        }
        
        String tableName = node.getDataString("tableName");
        if (tableName == null || tableName.trim().isEmpty()) {
            result.addError("SQL output node must specify a table name");
        }
        
        String databaseType = node.getDataString("databaseType");
        if (databaseType == null || databaseType.trim().isEmpty()) {
            result.addError("SQL output node must specify a database type");
            return;
        }
        
        // For SQLite, we don't need connection parameters
        if ("sqlite".equalsIgnoreCase(databaseType)) {
            return;
        }
        
        // For other databases, validate connection parameters
        String connectionString = node.getDataString("connectionString");
        String host = node.getDataString("host");
        String database = node.getDataString("database");
        String username = node.getDataString("username");
        String password = node.getDataString("password");
        
        // Check if either connection string or individual connection parameters are provided
        if ((connectionString == null || connectionString.trim().isEmpty()) && 
            (host == null || host.trim().isEmpty() || database == null || database.trim().isEmpty())) {
            result.addError("SQL output node must specify either a connection string or host/database details");
        }
        
        // Username and password are required for most databases (except SQLite)
        if (username == null || username.trim().isEmpty()) {
            result.addWarning("SQL output node should specify a username for database authentication");
        }
    }
    
    private void validateCsvOutputNode(PipelineNode node, ValidationResult result) {
        if (node.getData() == null) {
            result.addError("CSV output node must have configuration data");
            return;
        }
        
        String filePath = node.getDataString("filePath");
        if (filePath == null || filePath.trim().isEmpty()) {
            result.addError("CSV output node must specify a file path");
        }
    }
    
    private void validateFilterNode(PipelineNode node, ValidationResult result) {
        if (node.getData() == null) {
            result.addError("Filter node must have configuration data");
            return;
        }
        
        String column = node.getDataString("column");
        String operator = node.getDataString("operator");
        String value = node.getDataString("value");
        
        if (column == null || column.trim().isEmpty()) {
            result.addError("Filter node must specify a column");
        }
        
        if (operator == null || operator.trim().isEmpty()) {
            result.addError("Filter node must specify an operator");
        }
        
        if (value == null || value.trim().isEmpty()) {
            result.addError("Filter node must specify a value");
        }
    }
    
    private void validateSqlSourceNode(PipelineNode node, ValidationResult result) {
        if (node.getData() == null) {
            result.addError("SQL source node must have configuration data");
            return;
        }
        
        // Check if either connection string or individual connection parameters are provided
        String connectionString = node.getDataString("connectionString");
        String host = node.getDataString("host");
        String database = node.getDataString("database");
        
        if ((connectionString == null || connectionString.trim().isEmpty()) && 
            (host == null || host.trim().isEmpty() || database == null || database.trim().isEmpty())) {
            result.addError("SQL source node must specify either a connection string or host/database details");
        }
        
        // Check if query or table name is provided
        String query = node.getDataString("query");
        String tableName = node.getDataString("tableName");
        
        if ((query == null || query.trim().isEmpty()) && 
            (tableName == null || tableName.trim().isEmpty())) {
            result.addError("SQL source node must specify either a table name or SQL query");
        }
    }

    private void validateEdges(Pipeline pipeline, ValidationResult result) {
        for (var edge : pipeline.getEdges()) {
            if (edge.getSource() == null || edge.getTarget() == null) {
                result.addError("Edge must have both source and target nodes");
            }
            
            // Verify that source and target nodes exist
            boolean sourceExists = pipeline.getNodes().stream()
                    .anyMatch(node -> node.getId().equals(edge.getSource()));
            boolean targetExists = pipeline.getNodes().stream()
                    .anyMatch(node -> node.getId().equals(edge.getTarget()));
            
            if (!sourceExists) {
                result.addError("Edge references non-existent source node: " + edge.getSource());
            }
            
            if (!targetExists) {
                result.addError("Edge references non-existent target node: " + edge.getTarget());
            }
        }
    }
    
    public static class ValidationResult {
        private List<String> errors = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();
        
        public void addError(String error) {
            errors.add(error);
        }
        
        public void addWarning(String warning) {
            warnings.add(warning);
        }
        
        public boolean isValid() {
            return errors.isEmpty();
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        public List<String> getWarnings() {
            return warnings;
        }
    }
}
