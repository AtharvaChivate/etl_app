package com.etl.pipeline.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PipelineEdge {
    
    private String id;
    private String source;
    private String target;
    private String sourceHandle;
    private String targetHandle;
    
    // Constructors
    public PipelineEdge() {}
    
    public PipelineEdge(String id, String source, String target) {
        this.id = id;
        this.source = source;
        this.target = target;
    }
    
    public PipelineEdge(String id, String source, String target, String sourceHandle, String targetHandle) {
        this.id = id;
        this.source = source;
        this.target = target;
        this.sourceHandle = sourceHandle;
        this.targetHandle = targetHandle;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public String getTarget() {
        return target;
    }
    
    public void setTarget(String target) {
        this.target = target;
    }
    
    public String getSourceHandle() {
        return sourceHandle;
    }
    
    public void setSourceHandle(String sourceHandle) {
        this.sourceHandle = sourceHandle;
    }
    
    public String getTargetHandle() {
        return targetHandle;
    }
    
    public void setTargetHandle(String targetHandle) {
        this.targetHandle = targetHandle;
    }
    
    @Override
    public String toString() {
        return "PipelineEdge{" +
                "id='" + id + '\'' +
                ", source='" + source + '\'' +
                ", target='" + target + '\'' +
                '}';
    }
}
