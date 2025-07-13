package com.etl.pipeline.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

/**
 * SQL Database source implementation
 * Supports MySQL, PostgreSQL, SQLite, SQL Server, Oracle
 */
public class SqlDatabaseSource implements DataSource {
    
    private static final Logger logger = LoggerFactory.getLogger(SqlDatabaseSource.class);
    private final Map<String, Object> config;
    private final DataSourceType databaseType;
    private final String connectionString;
    private final String username;
    private final String password;
    private final String query;
    private final String tableName;
    
    public SqlDatabaseSource(DataSourceType type, Map<String, Object> config) {
        this.config = config;
        this.databaseType = type;
        this.connectionString = (String) config.get("connectionString");
        this.username = (String) config.get("username");
        this.password = (String) config.get("password");
        this.query = (String) config.get("query");
        this.tableName = (String) config.get("tableName");
    }
    
    @Override
    public List<Map<String, Object>> read() throws Exception {
        String sqlQuery = buildQuery();
        logger.info("Executing SQL query: {}", sqlQuery);
        
        List<Map<String, Object>> data = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlQuery);
             ResultSet rs = stmt.executeQuery()) {
            
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnLabel(i);
                    Object value = rs.getObject(i);
                    row.put(columnName, value);
                }
                data.add(row);
            }
            
            logger.info("Successfully read {} records from database", data.size());
            return data;
            
        } catch (SQLException e) {
            logger.error("Error reading from database", e);
            throw e;
        }
    }
    
    @Override
    public void write(List<Map<String, Object>> data) throws Exception {
        throw new UnsupportedOperationException("SQL source is read-only. Use SqlDatabaseOutput for writing.");
    }
    
    @Override
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            logger.error("Database connection test failed", e);
            return false;
        }
    }
    
    @Override
    public DataSourceType getType() {
        return databaseType;
    }
    
    @Override
    public List<Map<String, Object>> getSchema() throws Exception {
        String sqlQuery = buildQuery() + " LIMIT 0"; // Get schema without data
        List<Map<String, Object>> schema = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlQuery);
             ResultSet rs = stmt.executeQuery()) {
            
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            for (int i = 1; i <= columnCount; i++) {
                Map<String, Object> column = new HashMap<>();
                column.put("name", metaData.getColumnLabel(i));
                column.put("type", metaData.getColumnTypeName(i));
                column.put("nullable", metaData.isNullable(i) != ResultSetMetaData.columnNoNulls);
                schema.add(column);
            }
            
            return schema;
        }
    }
    
    private Connection getConnection() throws SQLException {
        String url = buildConnectionUrl();
        logger.info("Connecting to database: {}", url);
        
        if (username != null && password != null) {
            return DriverManager.getConnection(url, username, password);
        } else {
            return DriverManager.getConnection(url);
        }
    }
    
    private String buildConnectionUrl() {
        if (connectionString != null && !connectionString.trim().isEmpty()) {
            return connectionString;
        }
        
        // Build connection URL based on database type and config
        String host = (String) config.getOrDefault("host", "localhost");
        Integer port = (Integer) config.get("port");
        String database = (String) config.get("database");
        
        switch (databaseType) {
            case MYSQL:
                int mysqlPort = port != null ? port : 3306;
                return String.format("jdbc:mysql://%s:%d/%s", host, mysqlPort, database);
                
            case POSTGRESQL:
                int pgPort = port != null ? port : 5432;
                return String.format("jdbc:postgresql://%s:%d/%s", host, pgPort, database);
                
            case SQLITE:
                String dbFile = database != null ? database : "database.db";
                return "jdbc:sqlite:" + dbFile;
                
            case MSSQL:
                int sqlPort = port != null ? port : 1433;
                return String.format("jdbc:sqlserver://%s:%d;databaseName=%s", host, sqlPort, database);
                
            case ORACLE:
                int oraclePort = port != null ? port : 1521;
                return String.format("jdbc:oracle:thin:@%s:%d:%s", host, oraclePort, database);
                
            default:
                throw new IllegalArgumentException("Unsupported database type: " + databaseType);
        }
    }
    
    private String buildQuery() {
        if (query != null && !query.trim().isEmpty()) {
            return query;
        } else if (tableName != null && !tableName.trim().isEmpty()) {
            return "SELECT * FROM " + tableName;
        } else {
            throw new IllegalArgumentException("Either 'query' or 'tableName' must be provided");
        }
    }
}
