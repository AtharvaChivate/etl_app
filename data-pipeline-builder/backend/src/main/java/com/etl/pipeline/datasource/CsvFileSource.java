package com.etl.pipeline.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * CSV File data source implementation
 */
public class CsvFileSource implements DataSource {
    
    private static final Logger logger = LoggerFactory.getLogger(CsvFileSource.class);
    private final Map<String, Object> config;
    private final String filePath;
    
    public CsvFileSource(Map<String, Object> config) {
        this.config = config;
        this.filePath = (String) config.get("filePath");
    }
    
    @Override
    public List<Map<String, Object>> read() throws Exception {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path is required for CSV source");
        }
        
        return readCsvFile(resolveFilePath(filePath));
    }
    
    @Override
    public void write(List<Map<String, Object>> data) throws Exception {
        throw new UnsupportedOperationException("CSV source is read-only");
    }
    
    @Override
    public boolean testConnection() {
        try {
            String resolvedPath = resolveFilePath(filePath);
            return Files.exists(Paths.get(resolvedPath)) && Files.isReadable(Paths.get(resolvedPath));
        } catch (Exception e) {
            logger.error("Error testing CSV file connection", e);
            return false;
        }
    }
    
    @Override
    public DataSourceType getType() {
        return DataSourceType.CSV_FILE;
    }
    
    @Override
    public List<Map<String, Object>> getSchema() throws Exception {
        String resolvedPath = resolveFilePath(filePath);
        
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(resolvedPath))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return new ArrayList<>();
            }
            
            String[] headers = parseCsvLine(headerLine);
            List<Map<String, Object>> schema = new ArrayList<>();
            
            for (String header : headers) {
                Map<String, Object> column = new HashMap<>();
                column.put("name", header.trim());
                column.put("type", "STRING"); // Default type for CSV
                column.put("nullable", true);
                schema.add(column);
            }
            
            return schema;
        }
    }
    
    private String resolveFilePath(String filePath) {
        // Handle different path formats
        String resolvedPath = filePath;
        
        // Handle uploaded files
        if (filePath.startsWith("uploads/")) {
            resolvedPath = filePath; // Direct path to uploaded file
        } else if (filePath.startsWith("/uploads/")) {
            resolvedPath = filePath.substring(1); // Remove leading slash
        } else if (filePath.startsWith("\\uploads\\")) {
            resolvedPath = filePath.replace("\\", "/");
        }
        // Handle sample data files
        else if (filePath.startsWith("/sample-data/")) {
            resolvedPath = "../sample-data/" + filePath.substring("/sample-data/".length());
        } else if (filePath.startsWith("sample-data/")) {
            resolvedPath = "../" + filePath;
        } else if (!Paths.get(filePath).isAbsolute()) {
            // Check if it's an uploaded file first
            if (Files.exists(Paths.get("uploads/" + filePath))) {
                resolvedPath = "uploads/" + filePath;
            } else {
                // Try relative to project root (backend runs from backend/ directory)
                resolvedPath = "../sample-data/" + filePath;
            }
        }
        
        return resolvedPath;
    }
    
    private List<Map<String, Object>> readCsvFile(String resolvedPath) throws IOException {
        List<Map<String, Object>> data = new ArrayList<>();
        
        logger.info("Reading CSV file: {}", resolvedPath);
        
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(resolvedPath))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                logger.warn("CSV file is empty: {}", resolvedPath);
                return data;
            }
            
            String[] headers = parseCsvLine(headerLine);
            logger.info("CSV headers: {}", Arrays.toString(headers));
            String line;
            
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue; // Skip empty lines
                }
                
                String[] values = parseCsvLine(line);
                Map<String, Object> row = new HashMap<>();
                
                for (int i = 0; i < headers.length && i < values.length; i++) {
                    row.put(headers[i].trim(), values[i].trim());
                }
                
                data.add(row);
            }
            
            logger.info("Successfully read {} records from CSV file", data.size());
        } catch (IOException e) {
            logger.error("Error reading CSV file: {}", resolvedPath, e);
            throw e;
        }
        
        return data;
    }
    
    private String[] parseCsvLine(String line) {
        // Simple CSV parser that handles basic quoted fields
        List<String> fields = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentField = new StringBuilder();
        
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            
            if (ch == '"' && (i == 0 || line.charAt(i-1) != '\\')) {
                inQuotes = !inQuotes;
            } else if (ch == ',' && !inQuotes) {
                fields.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(ch);
            }
        }
        
        fields.add(currentField.toString());
        return fields.toArray(new String[0]);
    }
}
