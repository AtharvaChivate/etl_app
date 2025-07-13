package com.etl.pipeline.datasource;

import java.util.*;

/**
 * Placeholder implementations for data sources that are not yet fully implemented
 * These are basic stubs to prevent compilation errors
 * Classes are package-private to avoid file naming conflicts
 */

class JsonFileSource implements DataSource {
    private final Map<String, Object> config;
    
    public JsonFileSource(Map<String, Object> config) {
        this.config = config;
    }
    
    @Override
    public List<Map<String, Object>> read() throws Exception {
        throw new UnsupportedOperationException("JSON input not yet implemented");
    }
    
    @Override
    public void write(List<Map<String, Object>> data) throws Exception {
        throw new UnsupportedOperationException("JSON source is read-only");
    }
    
    @Override
    public boolean testConnection() {
        return false;
    }
    
    @Override
    public DataSourceType getType() {
        return DataSourceType.JSON_FILE;
    }
    
    @Override
    public List<Map<String, Object>> getSchema() throws Exception {
        return new ArrayList<>();
    }
}

class JsonFileOutput implements DataSource {
    private final Map<String, Object> config;
    
    public JsonFileOutput(Map<String, Object> config) {
        this.config = config;
    }
    
    @Override
    public List<Map<String, Object>> read() throws Exception {
        throw new UnsupportedOperationException("JSON output is write-only");
    }
    
    @Override
    public void write(List<Map<String, Object>> data) throws Exception {
        throw new UnsupportedOperationException("JSON output not yet implemented");
    }
    
    @Override
    public boolean testConnection() {
        return false;
    }
    
    @Override
    public DataSourceType getType() {
        return DataSourceType.JSON_OUTPUT;
    }
    
    @Override
    public List<Map<String, Object>> getSchema() throws Exception {
        return new ArrayList<>();
    }
}

class RestApiSource implements DataSource {
    private final Map<String, Object> config;
    
    public RestApiSource(Map<String, Object> config) {
        this.config = config;
    }
    
    @Override
    public List<Map<String, Object>> read() throws Exception {
        throw new UnsupportedOperationException("REST API input not yet implemented");
    }
    
    @Override
    public void write(List<Map<String, Object>> data) throws Exception {
        throw new UnsupportedOperationException("REST API source is read-only");
    }
    
    @Override
    public boolean testConnection() {
        return false;
    }
    
    @Override
    public DataSourceType getType() {
        return DataSourceType.REST_API;
    }
    
    @Override
    public List<Map<String, Object>> getSchema() throws Exception {
        return new ArrayList<>();
    }
}

class RestApiOutput implements DataSource {
    private final Map<String, Object> config;
    
    public RestApiOutput(Map<String, Object> config) {
        this.config = config;
    }
    
    @Override
    public List<Map<String, Object>> read() throws Exception {
        throw new UnsupportedOperationException("REST API output is write-only");
    }
    
    @Override
    public void write(List<Map<String, Object>> data) throws Exception {
        throw new UnsupportedOperationException("REST API output not yet implemented");
    }
    
    @Override
    public boolean testConnection() {
        return false;
    }
    
    @Override
    public DataSourceType getType() {
        return DataSourceType.REST_POST;
    }
    
    @Override
    public List<Map<String, Object>> getSchema() throws Exception {
        return new ArrayList<>();
    }
}
