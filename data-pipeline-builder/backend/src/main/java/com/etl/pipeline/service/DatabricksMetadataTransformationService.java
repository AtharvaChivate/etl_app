package com.etl.pipeline.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for transforming DAG metadata into Databricks-ready format
 */
@Service
public class DatabricksMetadataTransformationService {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabricksMetadataTransformationService.class);
    
    /**
     * Transform DAG metadata to Databricks format
     */
    public Map<String, Object> transformToDatabricksFormat(Map<String, Object> dagData) {
        Map<String, Object> databricksMetadata = new HashMap<>();
        
        // Basic DAG information
        databricksMetadata.put("name", dagData.get("name"));
        databricksMetadata.put("description", dagData.get("description"));
        databricksMetadata.put("version", dagData.get("version"));
        
        // Extract nodes and edges
        List<Map<String, Object>> nodes = (List<Map<String, Object>>) dagData.get("nodes");
        List<Map<String, Object>> edges = (List<Map<String, Object>>) dagData.get("edges");
        
        // Build dependency graph
        Map<String, String> predecessorMap = buildPredecessorMap(edges);
        
        // Transform sources
        List<Map<String, Object>> sources = extractSources(nodes);
        databricksMetadata.put("sources", sources);
        
        // Transform transformations
        List<Map<String, Object>> transformations = extractTransformations(nodes, predecessorMap);
        databricksMetadata.put("transformations", transformations);
        
        // Transform targets
        List<Map<String, Object>> targets = extractTargets(nodes, predecessorMap);
        databricksMetadata.put("targets", targets);
        
        logger.info("Transformed DAG '{}' to Databricks format with {} sources, {} transformations, {} targets", 
                   dagData.get("name"), sources.size(), transformations.size(), targets.size());
        
        return databricksMetadata;
    }
    
    /**
     * Build predecessor mapping from edges
     */
    private Map<String, String> buildPredecessorMap(List<Map<String, Object>> edges) {
        Map<String, String> predecessorMap = new HashMap<>();
        
        for (Map<String, Object> edge : edges) {
            String source = (String) edge.get("source");
            String target = (String) edge.get("target");
            predecessorMap.put(target, source);
        }
        
        return predecessorMap;
    }
    
    /**
     * Extract and transform source nodes
     */
    private List<Map<String, Object>> extractSources(List<Map<String, Object>> nodes) {
        return nodes.stream()
                .filter(node -> isSourceNode((String) node.get("type")))
                .map(this::transformSourceNode)
                .collect(Collectors.toList());
    }
    
    /**
     * Extract and transform transformation nodes
     */
    private List<Map<String, Object>> extractTransformations(List<Map<String, Object>> nodes, 
                                                            Map<String, String> predecessorMap) {
        return nodes.stream()
                .filter(node -> isTransformationNode((String) node.get("type")))
                .map(node -> transformTransformationNode(node, predecessorMap))
                .collect(Collectors.toList());
    }
    
    /**
     * Extract and transform target nodes
     */
    private List<Map<String, Object>> extractTargets(List<Map<String, Object>> nodes, 
                                                     Map<String, String> predecessorMap) {
        return nodes.stream()
                .filter(node -> isTargetNode((String) node.get("type")))
                .map(node -> transformTargetNode(node, predecessorMap))
                .collect(Collectors.toList());
    }
    
    /**
     * Transform a source node to Databricks format
     */
    private Map<String, Object> transformSourceNode(Map<String, Object> node) {
        Map<String, Object> source = new HashMap<>();
        Map<String, Object> data = (Map<String, Object>) node.get("data");
        
        source.put("id", node.get("id"));
        
        // Determine format and table based on node type
        String nodeType = (String) node.get("type");
        if ("csvSource".equals(nodeType)) {
            source.put("format", "csv");
            source.put("table", data.get("fileName"));
            source.put("path", data.get("filePath"));
        } else if ("sqlSource".equals(nodeType)) {
            source.put("format", "sql");
            source.put("table", data.get("tableName"));
            source.put("database", data.get("database"));
            source.put("connectionDetails", extractConnectionDetails(data));
        }
        
        return source;
    }
    
    /**
     * Transform a transformation node to Databricks format
     */
    private Map<String, Object> transformTransformationNode(Map<String, Object> node, 
                                                           Map<String, String> predecessorMap) {
        Map<String, Object> transformation = new HashMap<>();
        Map<String, Object> data = (Map<String, Object>) node.get("data");
        String nodeType = (String) node.get("type");
        String nodeId = (String) node.get("id");
        
        transformation.put("id", nodeId);
        transformation.put("type", nodeType);
        transformation.put("predecessor", predecessorMap.get(nodeId));
        
        // Add type-specific configuration
        switch (nodeType) {
            case "filter":
                addFilterConfiguration(transformation, data);
                break;
            case "map":
                addMapConfiguration(transformation, data);
                break;
            case "join":
                addJoinConfiguration(transformation, data, predecessorMap, nodeId);
                break;
            case "groupBy":
                addGroupByConfiguration(transformation, data);
                break;
            case "sort":
                addSortConfiguration(transformation, data);
                break;
        }
        
        return transformation;
    }
    
    /**
     * Transform a target node to Databricks format
     */
    private Map<String, Object> transformTargetNode(Map<String, Object> node, 
                                                   Map<String, String> predecessorMap) {
        Map<String, Object> target = new HashMap<>();
        Map<String, Object> data = (Map<String, Object>) node.get("data");
        String nodeType = (String) node.get("type");
        String nodeId = (String) node.get("id");
        
        target.put("id", nodeId);
        target.put("predecessor", predecessorMap.get(nodeId));
        
        if ("csvOutput".equals(nodeType)) {
            target.put("format", "csv");
            target.put("path", data.get("filePath"));
            target.put("mode", "overwrite");
        } else if ("sqlOutput".equals(nodeType)) {
            target.put("format", "sql");
            target.put("table", data.get("tableName"));
            target.put("database", data.get("database"));
            target.put("mode", data.getOrDefault("writeMode", "overwrite"));
            target.put("connectionDetails", extractConnectionDetails(data));
        }
        
        return target;
    }
    
    /**
     * Add filter-specific configuration
     */
    private void addFilterConfiguration(Map<String, Object> transformation, Map<String, Object> data) {
        Map<String, Object> condition = new HashMap<>();
        condition.put("column", data.get("column"));
        condition.put("operator", data.get("operator"));
        condition.put("value", data.get("value"));
        
        transformation.put("condition", condition);
    }
    
    /**
     * Add map-specific configuration
     */
    private void addMapConfiguration(Map<String, Object> transformation, Map<String, Object> data) {
        // Extract column mappings
        List<Map<String, Object>> mappings = (List<Map<String, Object>>) data.get("mappings");
        if (mappings != null) {
            List<String> columns = new ArrayList<>();
            List<String> select = new ArrayList<>();
            
            for (Map<String, Object> mapping : mappings) {
                columns.add((String) mapping.get("sourceColumn"));
                select.add((String) mapping.get("targetColumn"));
            }
            
            transformation.put("columns", columns);
            transformation.put("select", select);
        }
    }
    
    /**
     * Add join-specific configuration for multiple sources
     */
    private void addJoinConfiguration(Map<String, Object> transformation, Map<String, Object> data, 
                                     Map<String, String> predecessorMap, String nodeId) {
        // For joins, we need to find all input sources by analyzing edges
        List<String> sources = findJoinSources(predecessorMap, nodeId);
        transformation.put("sources", sources);
        
        // Join conditions
        List<Map<String, Object>> joinConditions = new ArrayList<>();
        Map<String, Object> condition = new HashMap<>();
        
        String leftColumn = (String) data.getOrDefault("leftColumn", "department");
        String rightColumn = (String) data.getOrDefault("rightColumn", "department");
        
        condition.put("left", sources.size() > 0 ? sources.get(0) : "A");
        condition.put("right", sources.size() > 1 ? sources.get(1) : "B");
        condition.put("condition", String.format("%s.%s = %s.%s", 
                     condition.get("left"), leftColumn,
                     condition.get("right"), rightColumn));
        joinConditions.add(condition);
        
        transformation.put("on", joinConditions);
        transformation.put("joinType", mapJoinType((String) data.getOrDefault("joinType", "inner")));
    }
    
    /**
     * Map internal join types to Databricks join types
     */
    private String mapJoinType(String internalJoinType) {
        switch (internalJoinType.toLowerCase()) {
            case "inner": return "inner";
            case "left": return "leftOuter";
            case "right": return "rightOuter";
            case "full": return "fullOuter";
            default: return "inner";
        }
    }
    
    /**
     * Add groupBy-specific configuration
     */
    private void addGroupByConfiguration(Map<String, Object> transformation, Map<String, Object> data) {
        transformation.put("groupColumns", data.get("groupByColumns"));
        transformation.put("aggregations", data.get("aggregations"));
    }
    
    /**
     * Add sort-specific configuration
     */
    private void addSortConfiguration(Map<String, Object> transformation, Map<String, Object> data) {
        transformation.put("sortColumns", data.get("sortColumns"));
    }
    
    /**
     * Extract connection details for database sources/targets
     */
    private Map<String, Object> extractConnectionDetails(Map<String, Object> data) {
        Map<String, Object> connectionDetails = new HashMap<>();
        
        // Common connection fields
        if (data.containsKey("host")) connectionDetails.put("host", data.get("host"));
        if (data.containsKey("port")) connectionDetails.put("port", data.get("port"));
        if (data.containsKey("database")) connectionDetails.put("database", data.get("database"));
        if (data.containsKey("username")) connectionDetails.put("username", data.get("username"));
        if (data.containsKey("databaseType")) connectionDetails.put("type", data.get("databaseType"));
        
        return connectionDetails.isEmpty() ? null : connectionDetails;
    }
    
    /**
     * Find all sources for a join node
     */
    private List<String> findJoinSources(Map<String, String> predecessorMap, String joinNodeId) {
        List<String> sources = new ArrayList<>();
        
        // For simplicity, we'll use the direct predecessor
        // In a more complex scenario, you'd need to trace back through the graph
        String predecessor = predecessorMap.get(joinNodeId);
        if (predecessor != null) {
            sources.add(predecessor);
        }
        
        // For joins, typically there are two sources
        // This is a simplified approach - you might need more complex logic
        // to properly identify multiple input sources for joins
        
        return sources;
    }
    
    /**
     * Check if node is a source node
     */
    private boolean isSourceNode(String nodeType) {
        return nodeType != null && (nodeType.equals("csvSource") || nodeType.equals("sqlSource"));
    }
    
    /**
     * Check if node is a transformation node
     */
    private boolean isTransformationNode(String nodeType) {
        return nodeType != null && (nodeType.equals("filter") || nodeType.equals("map") || 
                                   nodeType.equals("join") || nodeType.equals("groupBy") || 
                                   nodeType.equals("sort"));
    }
    
    /**
     * Check if node is a target node
     */
    private boolean isTargetNode(String nodeType) {
        return nodeType != null && (nodeType.equals("csvOutput") || nodeType.equals("sqlOutput"));
    }
}
