package com.etl.pipeline.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * CSV File output destination implementation
 */
public class CsvFileOutput implements DataSource {
    
    private static final Logger logger = LoggerFactory.getLogger(CsvFileOutput.class);
    private final Map<String, Object> config;
    private final String filePath;
    private final String delimiter;
    private final boolean includeHeaders;
    
    public CsvFileOutput(Map<String, Object> config) {
        this.config = config;
        this.filePath = (String) config.getOrDefault("filePath", "output/output.csv");
        this.delimiter = (String) config.getOrDefault("delimiter", ",");
        this.includeHeaders = (Boolean) config.getOrDefault("includeHeaders", true);
    }
    
    @Override
    public List<Map<String, Object>> read() throws Exception {
        throw new UnsupportedOperationException("CSV output destination is write-only");
    }
    
    @Override
    public void write(List<Map<String, Object>> data) throws Exception {
        if (data == null || data.isEmpty()) {
            logger.warn("No data to write to CSV file: {}", filePath);
            return;
        }
        
        // Validate file path
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Output file path cannot be null or empty");
        }
        
        // Create output directory if it doesn't exist and has parent directory
        try {
            Path outputPath = Paths.get(filePath);
            Path parentDir = outputPath.getParent();
            if (parentDir != null) {
                Files.createDirectories(parentDir);
            }
        } catch (IOException e) {
            logger.warn("Could not create output directory: {}", e.getMessage());
        }
        
        logger.info("Writing {} records to CSV file: {}", data.size(), filePath);
        
        try (BufferedWriter writer = Files.newBufferedWriter(
                Paths.get(filePath), 
                StandardOpenOption.CREATE, 
                StandardOpenOption.TRUNCATE_EXISTING)) {
            
            // Get all unique column names from data
            Set<String> allColumns = new LinkedHashSet<>();
            for (Map<String, Object> row : data) {
                allColumns.addAll(row.keySet());
            }
            
            List<String> columnOrder = new ArrayList<>(allColumns);
            
            // Write headers if enabled
            if (includeHeaders) {
                writer.write(String.join(delimiter, columnOrder));
                writer.newLine();
            }
            
            // Write data rows
            for (Map<String, Object> row : data) {
                List<String> values = new ArrayList<>();
                for (String column : columnOrder) {
                    Object value = row.get(column);
                    String stringValue = value != null ? value.toString() : "";
                    
                    // Escape CSV special characters
                    if (stringValue.contains(delimiter) || stringValue.contains("\"") || stringValue.contains("\n")) {
                        stringValue = "\"" + stringValue.replace("\"", "\"\"") + "\"";
                    }
                    
                    values.add(stringValue);
                }
                
                writer.write(String.join(delimiter, values));
                writer.newLine();
            }
            
            logger.info("Successfully wrote {} records to CSV file: {}", data.size(), filePath);
            
        } catch (IOException e) {
            logger.error("Error writing CSV file: {}", filePath, e);
            throw e;
        }
    }
    
    @Override
    public boolean testConnection() {
        try {
            // Test if we can create/write to the file
            Files.createDirectories(Paths.get(filePath).getParent());
            return Files.isWritable(Paths.get(filePath).getParent());
        } catch (Exception e) {
            logger.error("Error testing CSV output connection", e);
            return false;
        }
    }
    
    @Override
    public DataSourceType getType() {
        return DataSourceType.CSV_OUTPUT;
    }
    
    @Override
    public List<Map<String, Object>> getSchema() throws Exception {
        // For output destinations, schema is determined by input data
        return new ArrayList<>();
    }
}
