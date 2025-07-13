package com.etl.pipeline.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PipelineNode {
    
    private String id;
    private String type;
    private Position position;
    private Map<String, Object> data;
    
    // Constructors
    public PipelineNode() {}
    
    public PipelineNode(String id, String type, Position position, Map<String, Object> data) {
        this.id = id;
        this.type = type;
        this.position = position;
        this.data = data;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public Position getPosition() {
        return position;
    }
    
    public void setPosition(Position position) {
        this.position = position;
    }
    
    public Map<String, Object> getData() {
        return data;
    }
    
    public void setData(Map<String, Object> data) {
        this.data = data;
    }
    
    // Helper methods to get specific data fields
    public String getDataString(String key) {
        return data != null ? (String) data.get(key) : null;
    }
    
    public Boolean getDataBoolean(String key) {
        return data != null ? (Boolean) data.get(key) : null;
    }
    
    public Integer getDataInteger(String key) {
        Object value = data != null ? data.get(key) : null;
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    
    @Override
    public String toString() {
        return "PipelineNode{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", position=" + position +
                '}';
    }
    
    // Inner class for position
    public static class Position {
        private double x;
        private double y;
        
        public Position() {}
        
        public Position(double x, double y) {
            this.x = x;
            this.y = y;
        }
        
        public double getX() {
            return x;
        }
        
        public void setX(double x) {
            this.x = x;
        }
        
        public double getY() {
            return y;
        }
        
        public void setY(double y) {
            this.y = y;
        }
        
        @Override
        public String toString() {
            return "Position{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }
    }
}
