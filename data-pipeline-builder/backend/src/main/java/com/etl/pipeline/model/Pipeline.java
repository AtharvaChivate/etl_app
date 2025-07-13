package com.etl.pipeline.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Pipeline {
    
    private String id;
    private String name;
    private String description;
    private List<PipelineNode> nodes;
    private List<PipelineEdge> edges;
    private List<String> executionOrder;
    private Map<String, Object> metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public Pipeline() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Pipeline(String name, String description) {
        this();
        this.name = name;
        this.description = description;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public List<PipelineNode> getNodes() {
        return nodes;
    }
    
    public void setNodes(List<PipelineNode> nodes) {
        this.nodes = nodes;
    }
    
    public List<PipelineEdge> getEdges() {
        return edges;
    }
    
    public void setEdges(List<PipelineEdge> edges) {
        this.edges = edges;
    }
    
    @JsonProperty("executionOrder")
    public List<String> getExecutionOrder() {
        return executionOrder;
    }
    
    public void setExecutionOrder(List<String> executionOrder) {
        this.executionOrder = executionOrder;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "Pipeline{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", nodes=" + (nodes != null ? nodes.size() : 0) +
                ", edges=" + (edges != null ? edges.size() : 0) +
                '}';
    }
}
