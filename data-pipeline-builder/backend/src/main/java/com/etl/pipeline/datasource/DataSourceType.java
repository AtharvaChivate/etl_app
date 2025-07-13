package com.etl.pipeline.datasource;

/**
 * Enum for supported data source types
 */
public enum DataSourceType {
    // Input Sources
    CSV_FILE("CSV File", true, false),
    EXCEL_FILE("Excel File", true, false),
    JSON_FILE("JSON File", true, false),
    SQL_DATABASE("SQL Database", true, false),
    MYSQL("MySQL Database", true, false),
    POSTGRESQL("PostgreSQL Database", true, false),
    SQLITE("SQLite Database", true, false),
    MSSQL("SQL Server Database", true, false),
    ORACLE("Oracle Database", true, false),
    REST_API("REST API", true, false),
    MONGODB("MongoDB", true, false),
    
    // Output Destinations
    CSV_OUTPUT("CSV Output", false, true),
    EXCEL_OUTPUT("Excel Output", false, true),
    JSON_OUTPUT("JSON Output", false, true),
    SQL_OUTPUT("SQL Database Output", false, true),
    REST_POST("REST API Post", false, true),
    EMAIL_REPORT("Email Report", false, true),
    
    // Both (can be input or output)
    DATABASE_TABLE("Database Table", true, true),
    FILE_SYSTEM("File System", true, true);
    
    private final String displayName;
    private final boolean canRead;
    private final boolean canWrite;
    
    DataSourceType(String displayName, boolean canRead, boolean canWrite) {
        this.displayName = displayName;
        this.canRead = canRead;
        this.canWrite = canWrite;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean canRead() {
        return canRead;
    }
    
    public boolean canWrite() {
        return canWrite;
    }
}
