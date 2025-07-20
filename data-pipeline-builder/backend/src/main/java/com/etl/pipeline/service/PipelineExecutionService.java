package com.etl.pipeline.service;

import com.etl.pipeline.model.ExecutionResult;
import com.etl.pipeline.model.Pipeline;
import com.etl.pipeline.model.PipelineNode;
import com.etl.pipeline.datasource.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class PipelineExecutionService {
    
    private static final Logger logger = LoggerFactory.getLogger(PipelineExecutionService.class);
    private final Map<String, ExecutionResult> executionResults = new ConcurrentHashMap<>();
    
    @Autowired
    private DataSourceFactory dataSourceFactory;
    
    public ExecutionResult executePipeline(Pipeline pipeline) {
        String executionId = UUID.randomUUID().toString();
        ExecutionResult result = new ExecutionResult(executionId, pipeline.getId());
        
        executionResults.put(executionId, result);
        
        try {
            logger.info("Starting pipeline execution: {}", executionId);
            
            // Execute nodes in order
            List<String> executionOrder = pipeline.getExecutionOrder();
            if (executionOrder == null || executionOrder.isEmpty()) {
                executionOrder = determineExecutionOrder(pipeline);
            }
            
            Map<String, List<Map<String, Object>>> dataCache = new HashMap<>();
            
            for (String nodeId : executionOrder) {
                PipelineNode node = findNodeById(pipeline, nodeId);
                if (node == null) {
                    throw new RuntimeException("Node not found: " + nodeId);
                }
                
                logger.info("Executing node: {} ({})", nodeId, node.getType());
                executeNode(node, dataCache, pipeline);
            }
            
            // Find the output location from SQL output nodes
            String outputLocation = findOutputLocation(pipeline);
            result.setOutputLocation(outputLocation);
            
            // Calculate records processed (simplified)
            long recordsProcessed = dataCache.values().stream()
                    .mapToLong(List::size)
                    .max()
                    .orElse(0L);
            
            result.setRecordsProcessed(recordsProcessed);
            result.markAsCompleted("Pipeline executed successfully");
            
            logger.info("Pipeline execution completed: {}", result);
            
        } catch (Exception e) {
            logger.error("Pipeline execution failed", e);
            result.markAsFailed(e.getMessage(), getStackTrace(e));
        }
        
        return result;
    }
    
    private void executeNode(PipelineNode node, Map<String, List<Map<String, Object>>> dataCache, Pipeline pipeline) {
        switch (node.getType()) {
            case "csvSource":
                executeDataSourceNode(node, dataCache, DataSourceType.CSV_FILE);
                break;
            case "sqlSource":
                executeDataSourceNode(node, dataCache, DataSourceType.SQL_DATABASE);
                break;
            case "mysqlSource":
                executeDataSourceNode(node, dataCache, DataSourceType.MYSQL);
                break;
            case "postgresqlSource":
                executeDataSourceNode(node, dataCache, DataSourceType.POSTGRESQL);
                break;
            case "sqliteSource":
                executeDataSourceNode(node, dataCache, DataSourceType.SQLITE);
                break;
            case "filter":
                executeFilterNode(node, dataCache, pipeline);
                break;
            case "map":
                executeMapNode(node, dataCache, pipeline);
                break;
            case "groupBy":
                executeGroupByNode(node, dataCache, pipeline);
                break;
            case "sort":
                executeSortNode(node, dataCache, pipeline);
                break;
            case "join":
                executeJoinNode(node, dataCache, pipeline);
                break;
            case "sqlOutput":
                executeSqlOutputNode(node, dataCache, pipeline);
                break;
            case "csvOutput":
                executeDataOutputNode(node, dataCache, pipeline, DataSourceType.CSV_OUTPUT);
                break;
            default:
                logger.warn("Unknown node type: {}", node.getType());
        }
    }
    
    private void executeDataSourceNode(PipelineNode node, Map<String, List<Map<String, Object>>> dataCache, DataSourceType sourceType) {
        try {
            Map<String, Object> config = node.getData();
            DataSource dataSource = dataSourceFactory.createDataSource(sourceType, config);
            
            List<Map<String, Object>> data = dataSource.read();
            dataCache.put(node.getId(), data);
            logger.info("Loaded {} records from {}: {}", data.size(), sourceType.getDisplayName(), config.get("filePath"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to read from " + sourceType.getDisplayName() + ": " + e.getMessage(), e);
        }
    }
    
    private void executeDataOutputNode(PipelineNode node, Map<String, List<Map<String, Object>>> dataCache, Pipeline pipeline, DataSourceType outputType) {
        try {
            // Get input data from previous node
            List<Map<String, Object>> inputData = getInputData(node, dataCache, pipeline);
            
            Map<String, Object> config = node.getData();
            logger.info("CSV Output node config: {}", config);
            logger.info("CSV Output node filePath: {}", config.get("filePath"));
            
            DataSource dataOutput = dataSourceFactory.createDataSource(outputType, config);
            
            dataOutput.write(inputData);
            logger.info("Wrote {} records to {}: {}", inputData.size(), outputType.getDisplayName(), config.get("filePath"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to write to " + outputType.getDisplayName() + ": " + e.getMessage(), e);
        }
    }
    
    private void executeCsvSourceNode(PipelineNode node, Map<String, List<Map<String, Object>>> dataCache) {
        try {
            String filePath = node.getDataString("filePath");
            List<Map<String, Object>> data = readCsvFile(filePath);
            dataCache.put(node.getId(), data);
            logger.info("Loaded {} records from CSV: {}", data.size(), filePath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read CSV file: " + e.getMessage(), e);
        }
    }
    
    private void executeFilterNode(PipelineNode node, Map<String, List<Map<String, Object>>> dataCache, Pipeline pipeline) {
        // Get input data from previous node
        List<Map<String, Object>> inputData = getInputData(node, dataCache, pipeline);
        
        String column = node.getDataString("column");
        String operator = node.getDataString("operator");
        String value = node.getDataString("value");
        
        List<Map<String, Object>> filteredData = new ArrayList<>();
        
        for (Map<String, Object> row : inputData) {
            if (matchesFilter(row, column, operator, value)) {
                filteredData.add(new HashMap<>(row));
            }
        }
        
        dataCache.put(node.getId(), filteredData);
        logger.info("Filtered {} -> {} records", inputData.size(), filteredData.size());
    }
    
    private void executeMapNode(PipelineNode node, Map<String, List<Map<String, Object>>> dataCache, Pipeline pipeline) {
        List<Map<String, Object>> inputData = getInputData(node, dataCache, pipeline);
        List<Map<String, Object>> mappedData = new ArrayList<>();
        
        // Get mappings from node data
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> mappings = (List<Map<String, Object>>) node.getData().get("mappings");
        
        for (Map<String, Object> row : inputData) {
            Map<String, Object> newRow = new HashMap<>(row);
            
            if (mappings != null) {
                for (Map<String, Object> mapping : mappings) {
                    String sourceCol = (String) mapping.get("sourceColumn");
                    String targetCol = (String) mapping.get("targetColumn");
                    String operation = (String) mapping.get("operation");
                    
                    if (sourceCol != null && targetCol != null && row.containsKey(sourceCol)) {
                        Object value = applyMapping(row.get(sourceCol), operation, mapping);
                        newRow.put(targetCol, value);
                        
                        // Remove source column if different from target
                        if (!sourceCol.equals(targetCol)) {
                            newRow.remove(sourceCol);
                        }
                    }
                }
            }
            
            mappedData.add(newRow);
        }
        
        dataCache.put(node.getId(), mappedData);
        logger.info("Mapped {} records", mappedData.size());
    }
    
    private void executeGroupByNode(PipelineNode node, Map<String, List<Map<String, Object>>> dataCache, Pipeline pipeline) {
        List<Map<String, Object>> inputData = getInputData(node, dataCache, pipeline);
        
        // Get groupBy configuration
        Map<String, Object> nodeData = node.getData();
        @SuppressWarnings("unchecked")
        List<String> groupByColumns = (List<String>) nodeData.get("groupByColumns");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> aggregations = (List<Map<String, Object>>) nodeData.get("aggregations");
        
        // Handle legacy parameter name
        if (groupByColumns == null) {
            groupByColumns = (List<String>) nodeData.get("groupColumns");
        }
        
        if (groupByColumns == null || groupByColumns.isEmpty()) {
            logger.warn("No groupBy columns specified, passing through data");
            dataCache.put(node.getId(), new ArrayList<>(inputData));
            return;
        }
        
        // Group data by specified columns
        Map<String, List<Map<String, Object>>> groups = new HashMap<>();
        
        for (Map<String, Object> row : inputData) {
            // Create group key from specified columns
            StringBuilder keyBuilder = new StringBuilder();
            for (String column : groupByColumns) {
                Object value = row.get(column);
                keyBuilder.append(value != null ? value.toString() : "null").append("|");
            }
            String groupKey = keyBuilder.toString();
            
            groups.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(row);
        }
        
        // Apply aggregations to each group
        List<Map<String, Object>> groupedData = new ArrayList<>();
        
        for (Map.Entry<String, List<Map<String, Object>>> groupEntry : groups.entrySet()) {
            List<Map<String, Object>> groupRows = groupEntry.getValue();
            Map<String, Object> aggregatedRow = new HashMap<>();
            
            // Add group by columns
            Map<String, Object> firstRow = groupRows.get(0);
            for (String column : groupByColumns) {
                aggregatedRow.put(column, firstRow.get(column));
            }
            
            // Apply aggregations
            if (aggregations != null) {
                for (Map<String, Object> aggregation : aggregations) {
                    String function = (String) aggregation.get("function"); // count, sum, avg, min, max
                    String column = (String) aggregation.get("column");
                    String alias = (String) aggregation.get("alias");
                    
                    if (alias == null) alias = function + "_" + column;
                    
                    Object aggregatedValue = applyAggregation(groupRows, function, column);
                    aggregatedRow.put(alias, aggregatedValue);
                }
            } else {
                // Default: just count the records in each group
                aggregatedRow.put("count", groupRows.size());
            }
            
            groupedData.add(aggregatedRow);
        }
        
        dataCache.put(node.getId(), groupedData);
        logger.info("Grouped {} records into {} groups", inputData.size(), groupedData.size());
    }
    
    private Object applyAggregation(List<Map<String, Object>> groupRows, String function, String column) {
        switch (function.toLowerCase()) {
            case "count":
                return groupRows.size();
            case "sum":
                return groupRows.stream()
                    .mapToDouble(row -> {
                        Object value = row.get(column);
                        if (value instanceof Number) {
                            return ((Number) value).doubleValue();
                        }
                        try {
                            return Double.parseDouble(value.toString());
                        } catch (NumberFormatException e) {
                            return 0.0;
                        }
                    })
                    .sum();
            case "avg":
                return groupRows.stream()
                    .mapToDouble(row -> {
                        Object value = row.get(column);
                        if (value instanceof Number) {
                            return ((Number) value).doubleValue();
                        }
                        try {
                            return Double.parseDouble(value.toString());
                        } catch (NumberFormatException e) {
                            return 0.0;
                        }
                    })
                    .average()
                    .orElse(0.0);
            case "min":
                return groupRows.stream()
                    .map(row -> row.get(column))
                    .filter(Objects::nonNull)
                    .min((a, b) -> a.toString().compareTo(b.toString()))
                    .orElse(null);
            case "max":
                return groupRows.stream()
                    .map(row -> row.get(column))
                    .filter(Objects::nonNull)
                    .max((a, b) -> a.toString().compareTo(b.toString()))
                    .orElse(null);
            default:
                logger.warn("Unknown aggregation function: {}", function);
                return null;
        }
    }
    
    private void executeSortNode(PipelineNode node, Map<String, List<Map<String, Object>>> dataCache, Pipeline pipeline) {
        List<Map<String, Object>> inputData = getInputData(node, dataCache, pipeline);
        List<Map<String, Object>> sortedData = new ArrayList<>(inputData);
        
        // Simple sort implementation (full implementation would handle multiple columns)
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> sortColumns = (List<Map<String, Object>>) node.getData().get("sortColumns");
        
        if (sortColumns != null && !sortColumns.isEmpty()) {
            String column = (String) sortColumns.get(0).get("column");
            String direction = (String) sortColumns.get(0).get("direction");
            
            sortedData.sort((a, b) -> {
                Object valA = a.get(column);
                Object valB = b.get(column);
                
                if (valA == null && valB == null) return 0;
                if (valA == null) return -1;
                if (valB == null) return 1;
                
                int comparison = valA.toString().compareTo(valB.toString());
                return "desc".equals(direction) ? -comparison : comparison;
            });
        }
        
        dataCache.put(node.getId(), sortedData);
        logger.info("Sorted {} records", sortedData.size());
    }
    
    private void executeJoinNode(PipelineNode node, Map<String, List<Map<String, Object>>> dataCache, Pipeline pipeline) {
        // Get join configuration
        Map<String, Object> nodeData = node.getData();
        String joinType = (String) nodeData.get("joinType"); // "inner", "left", "right", "full"
        String leftKey = (String) nodeData.get("leftKey");
        String rightKey = (String) nodeData.get("rightKey");
        
        // Handle legacy column names
        if (leftKey == null) {
            leftKey = (String) nodeData.get("leftColumn");
        }
        if (rightKey == null) {
            rightKey = (String) nodeData.get("rightColumn");
        }
        
        if (joinType == null) joinType = "inner"; // default to inner join
        
        logger.info("Join configuration: joinType={}, leftKey={}, rightKey={}", joinType, leftKey, rightKey);
        
        // Get input data from the two connected nodes
        List<String> inputNodeIds = getInputNodeIds(node, pipeline);
        if (inputNodeIds.size() != 2) {
            throw new RuntimeException("Join node must have exactly 2 input connections, but has: " + inputNodeIds.size());
        }
        
        List<Map<String, Object>> leftData = dataCache.get(inputNodeIds.get(0));
        List<Map<String, Object>> rightData = dataCache.get(inputNodeIds.get(1));
        
        if (leftData == null || rightData == null) {
            throw new RuntimeException("Input data not found for join operation");
        }
        
        if (leftKey == null || rightKey == null) {
            throw new RuntimeException("Join keys must be specified. leftKey: " + leftKey + ", rightKey: " + rightKey);
        }
        
        logger.info("Left data sample: {}", leftData.isEmpty() ? "empty" : leftData.get(0).keySet());
        logger.info("Right data sample: {}", rightData.isEmpty() ? "empty" : rightData.get(0).keySet());
        
        List<Map<String, Object>> joinedData = performJoin(leftData, rightData, leftKey, rightKey, joinType);
        
        dataCache.put(node.getId(), joinedData);
        logger.info("Joined {} + {} records -> {} records using {} join", 
                   leftData.size(), rightData.size(), joinedData.size(), joinType);
    }
    
    private List<Map<String, Object>> performJoin(List<Map<String, Object>> leftData, 
                                                  List<Map<String, Object>> rightData,
                                                  String leftKey, String rightKey, String joinType) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        switch (joinType.toLowerCase()) {
            case "inner":
                return performInnerJoin(leftData, rightData, leftKey, rightKey);
            case "left":
                return performLeftJoin(leftData, rightData, leftKey, rightKey);
            case "right":
                return performRightJoin(leftData, rightData, leftKey, rightKey);
            case "full":
                return performFullJoin(leftData, rightData, leftKey, rightKey);
            default:
                logger.warn("Unknown join type: {}, defaulting to inner join", joinType);
                return performInnerJoin(leftData, rightData, leftKey, rightKey);
        }
    }
    
    private List<Map<String, Object>> performInnerJoin(List<Map<String, Object>> leftData,
                                                       List<Map<String, Object>> rightData,
                                                       String leftKey, String rightKey) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Map<String, Object> leftRow : leftData) {
            Object leftValue = leftRow.get(leftKey);
            if (leftValue == null) continue;
            
            for (Map<String, Object> rightRow : rightData) {
                Object rightValue = rightRow.get(rightKey);
                if (rightValue != null && leftValue.toString().equals(rightValue.toString())) {
                    Map<String, Object> joinedRow = new HashMap<>(leftRow);
                    // Add right row data with prefix to avoid column name conflicts
                    for (Map.Entry<String, Object> entry : rightRow.entrySet()) {
                        String key = entry.getKey();
                        if (!joinedRow.containsKey(key)) {
                            joinedRow.put(key, entry.getValue());
                        } else {
                            joinedRow.put("right_" + key, entry.getValue());
                        }
                    }
                    result.add(joinedRow);
                }
            }
        }
        
        return result;
    }
    
    private List<Map<String, Object>> performLeftJoin(List<Map<String, Object>> leftData,
                                                      List<Map<String, Object>> rightData,
                                                      String leftKey, String rightKey) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Map<String, Object> leftRow : leftData) {
            Object leftValue = leftRow.get(leftKey);
            boolean foundMatch = false;
            
            if (leftValue != null) {
                for (Map<String, Object> rightRow : rightData) {
                    Object rightValue = rightRow.get(rightKey);
                    if (rightValue != null && leftValue.toString().equals(rightValue.toString())) {
                        Map<String, Object> joinedRow = new HashMap<>(leftRow);
                        for (Map.Entry<String, Object> entry : rightRow.entrySet()) {
                            String key = entry.getKey();
                            if (!joinedRow.containsKey(key)) {
                                joinedRow.put(key, entry.getValue());
                            } else {
                                joinedRow.put("right_" + key, entry.getValue());
                            }
                        }
                        result.add(joinedRow);
                        foundMatch = true;
                    }
                }
            }
            
            // If no match found, add left row with null values for right columns
            if (!foundMatch) {
                Map<String, Object> joinedRow = new HashMap<>(leftRow);
                // Add null values for right table columns
                if (!rightData.isEmpty()) {
                    Map<String, Object> sampleRightRow = rightData.get(0);
                    for (String key : sampleRightRow.keySet()) {
                        if (!joinedRow.containsKey(key)) {
                            joinedRow.put(key, null);
                        } else {
                            joinedRow.put("right_" + key, null);
                        }
                    }
                }
                result.add(joinedRow);
            }
        }
        
        return result;
    }
    
    private List<Map<String, Object>> performRightJoin(List<Map<String, Object>> leftData,
                                                       List<Map<String, Object>> rightData,
                                                       String leftKey, String rightKey) {
        // Right join is just a left join with tables swapped
        return performLeftJoin(rightData, leftData, rightKey, leftKey);
    }
    
    private List<Map<String, Object>> performFullJoin(List<Map<String, Object>> leftData,
                                                      List<Map<String, Object>> rightData,
                                                      String leftKey, String rightKey) {
        List<Map<String, Object>> result = new ArrayList<>();
        Set<String> rightMatchedKeys = new HashSet<>();
        
        // First, perform left join
        result.addAll(performLeftJoin(leftData, rightData, leftKey, rightKey));
        
        // Track which right rows were matched
        for (Map<String, Object> leftRow : leftData) {
            Object leftValue = leftRow.get(leftKey);
            if (leftValue != null) {
                for (Map<String, Object> rightRow : rightData) {
                    Object rightValue = rightRow.get(rightKey);
                    if (rightValue != null && leftValue.toString().equals(rightValue.toString())) {
                        rightMatchedKeys.add(rightValue.toString());
                    }
                }
            }
        }
        
        // Add unmatched right rows
        for (Map<String, Object> rightRow : rightData) {
            Object rightValue = rightRow.get(rightKey);
            if (rightValue != null && !rightMatchedKeys.contains(rightValue.toString())) {
                Map<String, Object> joinedRow = new HashMap<>(rightRow);
                // Add null values for left table columns
                if (!leftData.isEmpty()) {
                    Map<String, Object> sampleLeftRow = leftData.get(0);
                    for (String key : sampleLeftRow.keySet()) {
                        if (!joinedRow.containsKey(key)) {
                            joinedRow.put(key, null);
                        }
                    }
                }
                result.add(joinedRow);
            }
        }
        
        return result;
    }

    private void executeSqlOutputNode(PipelineNode node, Map<String, List<Map<String, Object>>> dataCache, Pipeline pipeline) {
        try {
            List<Map<String, Object>> inputData = getInputData(node, dataCache, pipeline);
            
            String tableName = node.getDataString("tableName");
            String databaseType = node.getDataString("databaseType");
            
            if (databaseType == null || databaseType.trim().isEmpty()) {
                throw new IllegalArgumentException("Database type must be specified for SQL output node");
            }
            
            if (tableName == null || tableName.trim().isEmpty()) {
                throw new IllegalArgumentException("Table name must be specified for SQL output node");
            }
            
            switch (databaseType.toLowerCase()) {
                case "sqlite":
                    writeSqliteTable(tableName, inputData);
                    break;
                case "mysql":
                    writeMySqlTable(node, tableName, inputData);
                    break;
                case "postgresql":
                    writePostgreSqlTable(node, tableName, inputData);
                    break;
                case "mssql":
                case "sqlserver":
                    writeSqlServerTable(node, tableName, inputData);
                    break;
                case "oracle":
                    writeOracleTable(node, tableName, inputData);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported database type: " + databaseType);
            }
            
            logger.info("Written {} records to table: {} ({})", inputData.size(), tableName, databaseType);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to write to SQL output: " + e.getMessage(), e);
        }
    }
    
    // MySQL Output Implementation
    private void writeMySqlTable(PipelineNode node, String tableName, List<Map<String, Object>> data) throws SQLException {
        if (data.isEmpty()) {
            logger.warn("No data to write to MySQL table: {}", tableName);
            return;
        }
        
        String connectionUrl = buildConnectionUrl(node, "mysql", 3306);
        String username = node.getDataString("username");
        String password = node.getDataString("password");
        
        try (Connection conn = DriverManager.getConnection(connectionUrl, username, password)) {
            // Create table with auto-detection of column types
            createTableWithTypes(conn, tableName, data);
            
            // Insert data
            insertData(conn, tableName, data);
            
            logger.info("MySQL table '{}' created/updated with {} records", tableName, data.size());
        }
    }
    
    // PostgreSQL Output Implementation
    private void writePostgreSqlTable(PipelineNode node, String tableName, List<Map<String, Object>> data) throws SQLException {
        if (data.isEmpty()) {
            logger.warn("No data to write to PostgreSQL table: {}", tableName);
            return;
        }
        
        String connectionUrl = buildConnectionUrl(node, "postgresql", 5432);
        String username = node.getDataString("username");
        String password = node.getDataString("password");
        
        try (Connection conn = DriverManager.getConnection(connectionUrl, username, password)) {
            // Create table with auto-detection of column types
            createTableWithTypesPostgreSQL(conn, tableName, data);
            
            // Insert data
            insertData(conn, tableName, data);
            
            logger.info("PostgreSQL table '{}' created/updated with {} records", tableName, data.size());
        }
    }
    
    // SQL Server Output Implementation
    private void writeSqlServerTable(PipelineNode node, String tableName, List<Map<String, Object>> data) throws SQLException {
        if (data.isEmpty()) {
            logger.warn("No data to write to SQL Server table: {}", tableName);
            return;
        }
        
        String connectionUrl = buildConnectionUrl(node, "sqlserver", 1433);
        String username = node.getDataString("username");
        String password = node.getDataString("password");
        
        try (Connection conn = DriverManager.getConnection(connectionUrl, username, password)) {
            // Create table with auto-detection of column types
            createTableWithTypesSqlServer(conn, tableName, data);
            
            // Insert data
            insertData(conn, tableName, data);
            
            logger.info("SQL Server table '{}' created/updated with {} records", tableName, data.size());
        }
    }
    
    // Oracle Output Implementation
    private void writeOracleTable(PipelineNode node, String tableName, List<Map<String, Object>> data) throws SQLException {
        if (data.isEmpty()) {
            logger.warn("No data to write to Oracle table: {}", tableName);
            return;
        }
        
        String connectionUrl = buildConnectionUrl(node, "oracle", 1521);
        String username = node.getDataString("username");
        String password = node.getDataString("password");
        
        try (Connection conn = DriverManager.getConnection(connectionUrl, username, password)) {
            // Create table with auto-detection of column types
            createTableWithTypesOracle(conn, tableName, data);
            
            // Insert data
            insertData(conn, tableName, data);
            
            logger.info("Oracle table '{}' created/updated with {} records", tableName, data.size());
        }
    }
    
    // Helper method to build connection URL for different database types
    private String buildConnectionUrl(PipelineNode node, String dbType, int defaultPort) {
        String connectionString = node.getDataString("connectionString");
        if (connectionString != null && !connectionString.trim().isEmpty()) {
            return connectionString;
        }
        
        String host = node.getDataString("host");
        if (host == null) host = "localhost";
        
        String portStr = node.getDataString("port");
        int port = defaultPort;
        if (portStr != null && !portStr.trim().isEmpty()) {
            try {
                port = Integer.parseInt(portStr);
            } catch (NumberFormatException e) {
                logger.warn("Invalid port number: {}, using default: {}", portStr, defaultPort);
            }
        }
        
        String database = node.getDataString("database");
        if (database == null || database.trim().isEmpty()) {
            throw new IllegalArgumentException("Database name must be specified");
        }
        
        switch (dbType) {
            case "mysql":
                return String.format("jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true", host, port, database);
            case "postgresql":
                return String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
            case "sqlserver":
                return String.format("jdbc:sqlserver://%s:%d;databaseName=%s;trustServerCertificate=true", host, port, database);
            case "oracle":
                return String.format("jdbc:oracle:thin:@%s:%d:%s", host, port, database);
            default:
                throw new IllegalArgumentException("Unsupported database type: " + dbType);
        }
    }
    
    // PostgreSQL-specific table creation (handles different data types)
    private void createTableWithTypesPostgreSQL(Connection conn, String tableName, List<Map<String, Object>> data) throws SQLException {
        if (data.isEmpty()) return;
        
        // Drop table if exists and recreate (for demo purposes)
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS " + tableName);
        }
        
        Map<String, Object> firstRow = data.get(0);
        StringBuilder sql = new StringBuilder("CREATE TABLE ").append(tableName).append(" (");
        
        boolean first = true;
        for (Map.Entry<String, Object> entry : firstRow.entrySet()) {
            if (!first) sql.append(", ");
            sql.append(entry.getKey()).append(" ");
            
            // PostgreSQL-specific type mapping
            Object value = entry.getValue();
            if (value instanceof Integer || value instanceof Long) {
                sql.append("BIGINT");
            } else if (value instanceof Double || value instanceof Float) {
                sql.append("DOUBLE PRECISION");
            } else if (value instanceof Boolean) {
                sql.append("BOOLEAN");
            } else {
                sql.append("TEXT");
            }
            first = false;
        }
        sql.append(")");
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql.toString());
            logger.info("PostgreSQL table created: {}", sql.toString());
        }
    }
    
    // SQL Server-specific table creation
    private void createTableWithTypesSqlServer(Connection conn, String tableName, List<Map<String, Object>> data) throws SQLException {
        if (data.isEmpty()) return;
        
        // Drop table if exists and recreate (for demo purposes)
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("IF OBJECT_ID('" + tableName + "', 'U') IS NOT NULL DROP TABLE " + tableName);
        }
        
        Map<String, Object> firstRow = data.get(0);
        StringBuilder sql = new StringBuilder("CREATE TABLE ").append(tableName).append(" (");
        
        boolean first = true;
        for (Map.Entry<String, Object> entry : firstRow.entrySet()) {
            if (!first) sql.append(", ");
            sql.append("[").append(entry.getKey()).append("] ");
            
            // SQL Server-specific type mapping
            Object value = entry.getValue();
            if (value instanceof Integer) {
                sql.append("INT");
            } else if (value instanceof Long) {
                sql.append("BIGINT");
            } else if (value instanceof Double || value instanceof Float) {
                sql.append("FLOAT");
            } else if (value instanceof Boolean) {
                sql.append("BIT");
            } else {
                sql.append("NVARCHAR(MAX)");
            }
            first = false;
        }
        sql.append(")");
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql.toString());
            logger.info("SQL Server table created: {}", sql.toString());
        }
    }
    
    // Oracle-specific table creation
    private void createTableWithTypesOracle(Connection conn, String tableName, List<Map<String, Object>> data) throws SQLException {
        if (data.isEmpty()) return;
        
        // Drop table if exists and recreate (for demo purposes)
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("BEGIN EXECUTE IMMEDIATE 'DROP TABLE " + tableName + "'; EXCEPTION WHEN OTHERS THEN NULL; END;");
        }
        
        Map<String, Object> firstRow = data.get(0);
        StringBuilder sql = new StringBuilder("CREATE TABLE ").append(tableName).append(" (");
        
        boolean first = true;
        for (Map.Entry<String, Object> entry : firstRow.entrySet()) {
            if (!first) sql.append(", ");
            sql.append(entry.getKey()).append(" ");
            
            // Oracle-specific type mapping
            Object value = entry.getValue();
            if (value instanceof Integer || value instanceof Long) {
                sql.append("NUMBER");
            } else if (value instanceof Double || value instanceof Float) {
                sql.append("NUMBER(38,2)");
            } else if (value instanceof Boolean) {
                sql.append("NUMBER(1)"); // Oracle doesn't have BOOLEAN, use NUMBER(1)
            } else {
                sql.append("VARCHAR2(4000)");
            }
            first = false;
        }
        sql.append(")");
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql.toString());
            logger.info("Oracle table created: {}", sql.toString());
        }
    }
    
    private List<Map<String, Object>> readCsvFile(String filePath) throws IOException {
        List<Map<String, Object>> data = new ArrayList<>();
        
        // Handle different path formats
        String resolvedPath = filePath;
        
        if (filePath.startsWith("/sample-data/")) {
            resolvedPath = "../sample-data/" + filePath.substring("/sample-data/".length());
        } else if (filePath.startsWith("sample-data/")) {
            resolvedPath = "../" + filePath;
        } else if (filePath.startsWith("/uploads/")) {
            resolvedPath = "../sample-data/" + filePath.substring("/uploads/".length());
        } else if (filePath.startsWith("\\uploads\\")) {
            resolvedPath = "../sample-data/" + filePath.substring("\\uploads\\".length());
        } else if (!Paths.get(filePath).isAbsolute()) {
            // Try relative to project root (backend runs from backend/ directory)
            resolvedPath = "../sample-data/" + filePath;
        }
        
        logger.info("Attempting to read CSV file: {} -> resolved to: {}", filePath, resolvedPath);
        
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(resolvedPath))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                logger.warn("CSV file is empty: {}", resolvedPath);
                return data;
            }
            
            String[] headers = parseCsvLine(headerLine);
            logger.info("CSV headers: {}", Arrays.toString(headers));
            String line;
            int lineNumber = 1;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
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
            
            logger.info("Successfully read {} records from CSV file: {}", data.size(), resolvedPath);
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
    
    private void writeSqliteTable(String tableName, List<Map<String, Object>> data) throws SQLException {
        if (data.isEmpty()) {
            logger.warn("No data to write to table: {}", tableName);
            return;
        }
        
        // Create output directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get("output"));
        } catch (IOException e) {
            logger.warn("Could not create output directory: {}", e.getMessage());
        }
        
        String dbPath = "output/pipeline_results.db";
        String url = "jdbc:sqlite:" + dbPath;
        
        try (Connection conn = DriverManager.getConnection(url)) {
            // Enable foreign keys and other SQLite features
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
                stmt.execute("PRAGMA journal_mode = WAL");
            }
            
            // Create table with auto-detection of column types
            createTableWithTypes(conn, tableName, data);
            
            // Insert data
            insertData(conn, tableName, data);
            
            logger.info("SQLite database created/updated at: {}", new File(dbPath).getAbsolutePath());
            logger.info("Table '{}' contains {} records", tableName, data.size());
        }
    }
    
    private void createTableWithTypes(Connection conn, String tableName, List<Map<String, Object>> data) throws SQLException {
        if (data.isEmpty()) return;
        
        Map<String, Object> sampleRow = data.get(0);
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " (");
        
        boolean first = true;
        for (Map.Entry<String, Object> entry : sampleRow.entrySet()) {
            if (!first) sql.append(", ");
            
            String columnName = entry.getKey();
            String columnType = detectColumnType(columnName, data);
            
            sql.append(columnName).append(" ").append(columnType);
            first = false;
        }
        
        sql.append(")");
        
        try (Statement stmt = conn.createStatement()) {
            // Drop table if exists to ensure clean structure
            stmt.execute("DROP TABLE IF EXISTS " + tableName);
            stmt.execute(sql.toString());
            logger.info("Created table: {} with schema: {}", tableName, sql.toString());
        }
    }
    
    private String detectColumnType(String columnName, List<Map<String, Object>> data) {
        // Sample a few rows to detect type
        int sampleSize = Math.min(10, data.size());
        boolean allNumeric = true;
        boolean allInteger = true;
        
        for (int i = 0; i < sampleSize; i++) {
            Object value = data.get(i).get(columnName);
            if (value == null || value.toString().trim().isEmpty()) continue;
            
            String strValue = value.toString().trim();
            
            try {
                if (strValue.contains(".")) {
                    Double.parseDouble(strValue);
                    allInteger = false;
                } else {
                    Long.parseLong(strValue);
                }
            } catch (NumberFormatException e) {
                allNumeric = false;
                break;
            }
        }
        
        if (allNumeric) {
            return allInteger ? "INTEGER" : "REAL";
        } else {
            return "TEXT";
        }
    }
    
    private void insertData(Connection conn, String tableName, List<Map<String, Object>> data) throws SQLException {
        if (data.isEmpty()) return;
        
        Set<String> columns = data.get(0).keySet();
        StringBuilder sql = new StringBuilder("INSERT INTO " + tableName + " (");
        sql.append(String.join(", ", columns));
        sql.append(") VALUES (");
        sql.append(String.join(", ", Collections.nCopies(columns.size(), "?")));
        sql.append(")");
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            for (Map<String, Object> row : data) {
                int index = 1;
                for (String column : columns) {
                    pstmt.setString(index++, String.valueOf(row.get(column)));
                }
                pstmt.executeUpdate();
            }
        }
    }
    
    private List<Map<String, Object>> getInputData(PipelineNode node, Map<String, List<Map<String, Object>>> dataCache, Pipeline pipeline) {
        // Find the source node for this node
        String sourceNodeId = findSourceNode(node.getId(), pipeline);
        if (sourceNodeId != null && dataCache.containsKey(sourceNodeId)) {
            return dataCache.get(sourceNodeId);
        }
        return new ArrayList<>();
    }
    
    private List<String> getInputNodeIds(PipelineNode node, Pipeline pipeline) {
        // Find all source nodes for this node (needed for join operations)
        return pipeline.getEdges().stream()
                .filter(edge -> edge.getTarget().equals(node.getId()))
                .map(edge -> edge.getSource())
                .collect(Collectors.toList());
    }
    
    private String findSourceNode(String nodeId, Pipeline pipeline) {
        return pipeline.getEdges().stream()
                .filter(edge -> edge.getTarget().equals(nodeId))
                .map(edge -> edge.getSource())
                .findFirst()
                .orElse(null);
    }
    
    private PipelineNode findNodeById(Pipeline pipeline, String nodeId) {
        return pipeline.getNodes().stream()
                .filter(node -> node.getId().equals(nodeId))
                .findFirst()
                .orElse(null);
    }
    
    private List<String> determineExecutionOrder(Pipeline pipeline) {
        // Simple topological sort implementation
        List<String> order = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        
        for (PipelineNode node : pipeline.getNodes()) {
            if ("csvSource".equals(node.getType()) && !visited.contains(node.getId())) {
                visitNode(node.getId(), pipeline, visited, order);
            }
        }
        
        // Reverse the order to get correct execution sequence (sources first, outputs last)
        Collections.reverse(order);
        
        return order;
    }
    
    private void visitNode(String nodeId, Pipeline pipeline, Set<String> visited, List<String> order) {
        if (visited.contains(nodeId)) {
            return;
        }
        
        visited.add(nodeId);
        
        // Visit all target nodes
        pipeline.getEdges().stream()
                .filter(edge -> edge.getSource().equals(nodeId))
                .map(edge -> edge.getTarget())
                .forEach(targetId -> visitNode(targetId, pipeline, visited, order));
        
        order.add(nodeId);
    }
    
    private String findOutputLocation(Pipeline pipeline) {
        return pipeline.getNodes().stream()
                .filter(node -> "sqlOutput".equals(node.getType()) || "csvOutput".equals(node.getType()))
                .map(node -> {
                    if ("sqlOutput".equals(node.getType())) {
                        return node.getDataString("tableName");
                    } else if ("csvOutput".equals(node.getType())) {
                        return node.getDataString("filePath");
                    }
                    return null;
                })
                .filter(location -> location != null && !location.trim().isEmpty())
                .findFirst()
                .orElse("unknown");
    }
    
    private boolean matchesFilter(Map<String, Object> row, String column, String operator, String value) {
        Object cellValue = row.get(column);
        if (cellValue == null) {
            return false;
        }
        
        String cellStr = cellValue.toString();
        
        switch (operator) {
            case "==": return cellStr.equals(value);
            case "!=": return !cellStr.equals(value);
            case "contains": return cellStr.contains(value);
            case "startswith": return cellStr.startsWith(value);
            case "endswith": return cellStr.endsWith(value);
            default:
                // Numeric comparisons
                try {
                    double cellNum = Double.parseDouble(cellStr);
                    double valueNum = Double.parseDouble(value);
                    
                    switch (operator) {
                        case ">": return cellNum > valueNum;
                        case "<": return cellNum < valueNum;
                        case ">=": return cellNum >= valueNum;
                        case "<=": return cellNum <= valueNum;
                    }
                } catch (NumberFormatException e) {
                    return false;
                }
        }
        
        return false;
    }
    
    private Object applyMapping(Object value, String operation, Map<String, Object> mapping) {
        if (value == null) return null;
        
        String strValue = value.toString();
        
        switch (operation) {
            case "uppercase": return strValue.toUpperCase();
            case "lowercase": return strValue.toLowerCase();
            case "multiply":
                try {
                    double num = Double.parseDouble(strValue);
                    double factor = Double.parseDouble(mapping.get("value").toString());
                    return num * factor;
                } catch (NumberFormatException e) {
                    return value;
                }
            case "add":
                try {
                    double num = Double.parseDouble(strValue);
                    double addValue = Double.parseDouble(mapping.get("value").toString());
                    return num + addValue;
                } catch (NumberFormatException e) {
                    return value;
                }
            default: return value;
        }
    }
    
    private String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
    
    public ExecutionResult getExecutionStatus(String executionId) {
        return executionResults.get(executionId);
    }
}
