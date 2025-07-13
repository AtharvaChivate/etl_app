package com.etl.pipeline.model;

import java.time.LocalDateTime;
import java.util.Map;

public class ExecutionResult {
    
    private String executionId;
    private String pipelineId;
    private ExecutionStatus status;
    private String message;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long recordsProcessed;
    private String outputLocation;
    private Map<String, Object> statistics;
    private String errorMessage;
    private String stackTrace;
    
    // Constructors
    public ExecutionResult() {
        this.startTime = LocalDateTime.now();
        this.status = ExecutionStatus.RUNNING;
    }
    
    public ExecutionResult(String executionId, String pipelineId) {
        this();
        this.executionId = executionId;
        this.pipelineId = pipelineId;
    }
    
    // Enums
    public enum ExecutionStatus {
        PENDING,
        RUNNING,
        SUCCESS,
        FAILED,
        CANCELLED
    }
    
    // Getters and Setters
    public String getExecutionId() {
        return executionId;
    }
    
    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }
    
    public String getPipelineId() {
        return pipelineId;
    }
    
    public void setPipelineId(String pipelineId) {
        this.pipelineId = pipelineId;
    }
    
    public ExecutionStatus getStatus() {
        return status;
    }
    
    public void setStatus(ExecutionStatus status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public Long getRecordsProcessed() {
        return recordsProcessed;
    }
    
    public void setRecordsProcessed(Long recordsProcessed) {
        this.recordsProcessed = recordsProcessed;
    }
    
    public String getOutputLocation() {
        return outputLocation;
    }
    
    public void setOutputLocation(String outputLocation) {
        this.outputLocation = outputLocation;
    }
    
    public Map<String, Object> getStatistics() {
        return statistics;
    }
    
    public void setStatistics(Map<String, Object> statistics) {
        this.statistics = statistics;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public String getStackTrace() {
        return stackTrace;
    }
    
    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }
    
    // Helper methods
    public void markAsCompleted(String message) {
        this.status = ExecutionStatus.SUCCESS;
        this.message = message;
        this.endTime = LocalDateTime.now();
    }
    
    public void markAsFailed(String errorMessage) {
        this.status = ExecutionStatus.FAILED;
        this.errorMessage = errorMessage;
        this.endTime = LocalDateTime.now();
    }
    
    public void markAsFailed(String errorMessage, String stackTrace) {
        markAsFailed(errorMessage);
        this.stackTrace = stackTrace;
    }
    
    public long getExecutionTimeMs() {
        if (startTime != null && endTime != null) {
            return java.time.Duration.between(startTime, endTime).toMillis();
        }
        return 0;
    }
    
    @Override
    public String toString() {
        return "ExecutionResult{" +
                "executionId='" + executionId + '\'' +
                ", status=" + status +
                ", recordsProcessed=" + recordsProcessed +
                ", executionTime=" + getExecutionTimeMs() + "ms" +
                '}';
    }
}
