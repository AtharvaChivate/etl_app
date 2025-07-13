package com.etl.pipeline.datasource;

import java.util.List;
import java.util.Map;

/**
 * Interface for all data sources (input/output)
 */
public interface DataSource {
    
    /**
     * Read data from the source
     * @return List of data records as Map<String, Object>
     */
    List<Map<String, Object>> read() throws Exception;
    
    /**
     * Write data to the destination
     * @param data List of data records to write
     */
    void write(List<Map<String, Object>> data) throws Exception;
    
    /**
     * Test the connection to the data source
     * @return true if connection is successful
     */
    boolean testConnection();
    
    /**
     * Get the type of this data source
     * @return DataSourceType enum
     */
    DataSourceType getType();
    
    /**
     * Get schema/column information
     * @return List of column names and types
     */
    List<Map<String, Object>> getSchema() throws Exception;
}
