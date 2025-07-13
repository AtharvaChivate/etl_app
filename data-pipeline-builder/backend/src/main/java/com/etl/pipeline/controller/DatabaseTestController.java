package com.etl.pipeline.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for testing database connections
 */
@RestController
@RequestMapping("/api/database")
@CrossOrigin(origins = "http://localhost:3000", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
public class DatabaseTestController {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseTestController.class);
    
    @PostMapping("/test-connection")
    public ResponseEntity<Map<String, Object>> testConnection(@RequestBody Map<String, Object> connectionData) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String connectionString = buildConnectionString(connectionData);
            String username = (String) connectionData.get("username");
            String password = (String) connectionData.get("password");
            
            logger.info("Testing database connection to: {}", connectionString);
            
            // Test the connection
            try (Connection connection = DriverManager.getConnection(connectionString, username, password)) {
                // Simple test query to verify connection works
                try (Statement stmt = connection.createStatement()) {
                    // Try a simple query that should work on most databases
                    ResultSet rs = stmt.executeQuery("SELECT 1");
                    if (rs.next()) {
                        response.put("success", true);
                        response.put("message", "Connection successful!");
                        response.put("databaseProduct", connection.getMetaData().getDatabaseProductName());
                        response.put("databaseVersion", connection.getMetaData().getDatabaseProductVersion());
                    }
                }
            }
            
        } catch (Exception e) {
            logger.error("Database connection test failed: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Connection failed: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
        }
        
        return ResponseEntity.ok(response);
    }
    
    @RequestMapping(value = "/test-connection", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> testConnectionOptions() {
        return ResponseEntity.ok().build();
    }
    
    private String buildConnectionString(Map<String, Object> connectionData) {
        String existingConnectionString = (String) connectionData.get("connectionString");
        if (existingConnectionString != null && !existingConnectionString.trim().isEmpty()) {
            return existingConnectionString;
        }
        
        String databaseType = (String) connectionData.get("databaseType");
        String host = (String) connectionData.get("host");
        Object portObj = connectionData.get("port");
        String database = (String) connectionData.get("database");
        
        if (databaseType == null || host == null || database == null) {
            throw new IllegalArgumentException("Missing required connection parameters. Please provide either a connection string or host/database details.");
        }
        
        String port = portObj != null ? portObj.toString() : getDefaultPort(databaseType);
        
        switch (databaseType.toLowerCase()) {
            case "mysql":
                return String.format("jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true", host, port, database);
            case "postgresql":
                return String.format("jdbc:postgresql://%s:%s/%s", host, port, database);
            case "sqlite":
                // For SQLite, the database parameter is the file path
                return String.format("jdbc:sqlite:%s", database);
            case "mssql":
            case "sqlserver":
                return String.format("jdbc:sqlserver://%s:%s;databaseName=%s;trustServerCertificate=true", host, port, database);
            case "oracle":
                return String.format("jdbc:oracle:thin:@%s:%s:%s", host, port, database);
            default:
                throw new IllegalArgumentException("Unsupported database type: " + databaseType);
        }
    }
    
    private String getDefaultPort(String databaseType) {
        switch (databaseType.toLowerCase()) {
            case "mysql": return "3306";
            case "postgresql": return "5432";
            case "mssql":
            case "sqlserver": return "1433";
            case "oracle": return "1521";
            case "sqlite": return "";
            default: return "3306";
        }
    }
}
