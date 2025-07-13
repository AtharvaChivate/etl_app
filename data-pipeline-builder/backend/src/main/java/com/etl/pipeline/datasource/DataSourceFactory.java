package com.etl.pipeline.datasource;

import org.springframework.stereotype.Component;
import java.util.Map;

/**
 * Factory for creating data source instances
 */
@Component
public class DataSourceFactory {
    
    public DataSource createDataSource(DataSourceType type, Map<String, Object> config) {
        switch (type) {
            case CSV_FILE:
                return new CsvFileSource(config);
            case CSV_OUTPUT:
                return new CsvFileOutput(config);
            case SQL_DATABASE:
            case MYSQL:
            case POSTGRESQL:
            case SQLITE:
            case MSSQL:
            case ORACLE:
                return new SqlDatabaseSource(type, config);
            case SQL_OUTPUT:
                return new SqlDatabaseOutput(type, config);
            case JSON_FILE:
                return new JsonFileSource(config);
            case JSON_OUTPUT:
                return new JsonFileOutput(config);
            case REST_API:
                return new RestApiSource(config);
            case REST_POST:
                return new RestApiOutput(config);
            default:
                throw new UnsupportedOperationException("Data source type not implemented: " + type);
        }
    }
    
    public boolean isTypeSupported(DataSourceType type) {
        switch (type) {
            case CSV_FILE:
            case CSV_OUTPUT:
            case SQL_DATABASE:
            case MYSQL:
            case POSTGRESQL:
            case SQLITE:
            case MSSQL:
            case ORACLE:
            case SQL_OUTPUT:
            case JSON_FILE:
            case JSON_OUTPUT:
            case REST_API:
            case REST_POST:
                return true;
            default:
                return false;
        }
    }
}
