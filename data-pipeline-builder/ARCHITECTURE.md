# 🚀 Flexible ETL Framework - Architecture Overview

## 🏗️ System Architecture

This Visual Data Transformation Builder has been completely redesigned to be **flexible and extensible** for any data source and output destination, with an intuitive drag-and-drop interface and comprehensive database support.

### Core Components

#### 1. **Data Source Interface** (`DataSource.java`)
- Universal interface for all input/output operations
- Supports `read()`, `write()`, `testConnection()`, and `getSchema()`
- Enables seamless switching between different data source types

#### 2. **Data Source Factory** (`DataSourceFactory.java`)
- Creates appropriate data source instances based on configuration
- Supports all major database types and file formats
- Easily extensible for new data source types

#### 3. **Flexible Pipeline Execution**
- Updated `PipelineExecutionService` uses the data source factory
- Supports any combination of input → transformations → output
- Maintains comprehensive transformation capabilities (filter, map, group, sort, join)

#### 4. **Interactive UI Components**
- **Deletable Edges**: Hover over connections to see delete button for easy pipeline modification
- **Simplified Sidebar**: Clean interface with generic "SQL Source" instead of database-specific options
- **Enhanced ConfigPanel**: Database type selection within node configuration
- **Execution Results**: Dismissible execution result notifications with close button

## 📊 Supported Data Sources

### Input Sources
| Type | Status | Configuration |
|------|---------|--------------|
| CSV Files | ✅ Implemented | File path, delimiter, headers |
| SQL Source | ✅ Implemented | Universal SQL connection (MySQL, PostgreSQL, SQLite, SQL Server, Oracle) |
| MySQL | ✅ Implemented | Host, port, database, credentials, query |
| PostgreSQL | ✅ Implemented | Host, port, database, credentials, query |
| SQLite | ✅ Implemented | Database file path, query |
| SQL Server | ✅ Implemented | Host, port, database, credentials, query |
| Oracle | ✅ Implemented | Host, port, database, credentials, query |
| JSON Files | 🔄 Placeholder | File path |
| REST APIs | 🔄 Placeholder | URL, headers, authentication |
| Excel Files | 📋 Planned | File path, sheet name |
| MongoDB | 📋 Planned | Connection string, collection |

### Output Destinations
| Type | Status | Configuration |
|------|---------|--------------|
| CSV Files | ✅ Implemented | Output path, delimiter, headers |
| SQL Output | ✅ Implemented | Universal SQL output (All database types) |
| MySQL Output | ✅ Implemented | Connection details, table name, auto table creation |
| PostgreSQL Output | ✅ Implemented | Connection details, table name, auto table creation |
| SQLite Output | ✅ Implemented | Database file, table name, auto table creation |
| SQL Server Output | ✅ Implemented | Connection details, table name, auto table creation |
| Oracle Output | ✅ Implemented | Connection details, table name, auto table creation |
| JSON Files | 🔄 Placeholder | Output path, format |
| REST APIs | 🔄 Placeholder | URL, method, headers |
| Email Reports | 📋 Planned | Recipients, template |
| Excel Files | 📋 Planned | Output path, sheet formatting |

## 🎯 Current Implementation: Universal SQL Database Support

### Backend Implementation
```java
// Universal SQL Database Source/Output
SqlDatabaseSource source = new SqlDatabaseSource(DataSourceType.MYSQL, config);
List<Map<String, Object>> data = source.read();

// Apply transformations (filter, map, etc.)
// Data flows through transformation pipeline

// Universal SQL Output (supports all database types)
switch (databaseType.toLowerCase()) {
    case "mysql":
        writeMySqlTable(node, tableName, inputData);
        break;
    case "postgresql":
        writePostgreSqlTable(node, tableName, inputData);
        break;
    case "sqlite":
        writeSqliteTable(tableName, inputData);
        break;
    case "mssql":
    case "sqlserver":
        writeSqlServerTable(node, tableName, inputData);
        break;
    case "oracle":
        writeOracleTable(node, tableName, inputData);
        break;
}
```

### Frontend Components
- **Universal SQL Source Node**: Single node type that adapts to any database type
- **Universal SQL Output Node**: Single output node supporting all databases
- **Simplified Sidebar**: Clean interface with generic "SQL Source" option
- **Enhanced ConfigPanel**: Database type selection within node configuration
- **Deletable Edge System**: Interactive connection management
- **Execution Result Manager**: Dismissible execution notifications

### Current UI Improvements

#### Simplified Sidebar
- **📄 CSV Source**: File input with upload capability
- **🗄️ SQL Source**: Universal database input (user selects database type in configuration)
- **🔍 Filter**: Row filtering based on conditions
- **🔄 Map**: Column transformation and mapping
- **📊 Group By**: Data aggregation and grouping
- **🔗 Join**: Combine data from multiple sources
- **📋 Sort**: Data sorting capabilities
- **📊 CSV Output**: File export with configurable options
- **💾 SQL Output**: Universal database output (user selects database type in configuration)

#### Interactive Edge Management
- **Hover Detection**: Connections highlight on mouse hover
- **Delete Button**: Red "×" button appears on connection hover
- **Visual Feedback**: Connection changes color and thickness when hovered
- **Confirmation Dialog**: Prevents accidental deletion
- **Keyboard Support**: Delete selected connections with Delete/Backspace keys

#### Enhanced Execution Results
- **Fixed Position**: Results appear in bottom-right corner
- **Close Button**: Red "×" button to dismiss results
- **Formatted Output**: JSON formatted execution details
- **Persistent Until Dismissed**: Results stay visible until user closes them

### Configuration Options

#### Universal SQL Source Configuration
```javascript
{
  databaseType: "mysql",           // mysql, postgresql, sqlite, mssql, oracle
  host: "localhost",
  port: 3306,
  database: "my_database",
  username: "user",
  password: "password",
  query: "SELECT * FROM employees WHERE department = 'Engineering'"
}
```

#### Universal SQL Output Configuration
```javascript
{
  databaseType: "postgresql",      // mysql, postgresql, sqlite, mssql, oracle
  host: "localhost",
  port: 5432,
  database: "target_database", 
  username: "user",
  password: "password",
  tableName: "processed_employees"  // Auto-created with proper data types
}
```

#### CSV Output Configuration
```javascript
{
  filePath: "output/exported_data.csv",
  delimiter: ",",                  // comma, semicolon, tab, pipe
  includeHeaders: true
}
```

## 🔧 Adding New Data Sources

To add a new data source type:

1. **Add enum value** in `DataSourceType.java`
2. **Implement DataSource interface** (e.g., `MongoDbSource.java`)
3. **Update DataSourceFactory** to handle the new type
4. **Create React component** for the node UI
5. **Add to nodeTypes** and sidebar configuration
6. **Update ConfigPanel** with configuration form

Example for MongoDB:
```java
public class MongoDbSource implements DataSource {
    @Override
    public List<Map<String, Object>> read() throws Exception {
        // MongoDB connection and query logic
    }
    // ... other methods
}
```

## 🚦 Pipeline Execution Flow

1. **Pipeline Definition**: User creates visual pipeline with drag-and-drop
2. **Node Configuration**: Each node configured with appropriate settings
3. **Execution Order**: System determines optimal execution sequence
4. **Data Flow**: Data flows through transformation nodes
5. **Output Generation**: Final result written to configured destination

## 🎨 UI/UX Enhancements

### Simplified and Intuitive Interface
- **Single SQL Source**: Generic "SQL Source" instead of database-specific nodes
- **Database Type Selection**: Choose database type within node configuration
- **Visual Connection Management**: Hover over connections to see delete option
- **Clean Sidebar Organization**: Streamlined node palette for better usability

### Interactive Connection Management
- **Hover-to-Delete**: Hover over any connection to reveal delete button
- **Visual Feedback**: Connections change color and thickness on hover
- **Confirmation Dialogs**: Prevent accidental connection deletion
- **Keyboard Shortcuts**: Delete selected connections with Delete/Backspace

### Enhanced Configuration Experience
- **Dynamic Forms**: Configuration panel adapts to node and database type
- **Connection Testing**: Real-time database connection validation
- **Help Integration**: Context-sensitive help for each node type
- **Input Field Improvements**: No more single-character input limitations

### Execution Management
- **Dismissible Results**: Close execution results when done reviewing
- **Fixed Position Display**: Results appear consistently in bottom-right
- **Formatted Output**: Clean JSON display of execution details
- **Visual Success/Error States**: Clear indication of pipeline execution status

## 🔮 Current Features & Future Enhancements

### ✅ **Implemented Features**
1. **Universal Database Support**: All major databases with single node types
2. **Interactive Edge Management**: Hover-to-delete connections
3. **Enhanced UI/UX**: Simplified sidebar and dismissible execution results
4. **Comprehensive Validation**: Database connection testing and pipeline validation
5. **Auto Table Creation**: Automatic table creation with proper data type mapping
6. **Transformation Pipeline**: Filter, Map, Group By, Sort, Join operations
7. **File Upload System**: Drag-and-drop CSV file uploads
8. **Configuration Management**: Save and load pipeline configurations

### 🔮 **Future Enhancements**
1. **Schema Detection**: Automatic schema inference from data sources
2. **Data Preview**: Preview data at each pipeline stage
3. **Performance Monitoring**: Execution time and memory usage tracking
4. **Pipeline Templates**: Pre-built pipelines for common use cases
5. **Scheduling**: Automated pipeline execution on schedules
6. **Error Handling**: Advanced error recovery and logging
7. **Data Quality**: Built-in data validation and cleansing nodes
8. **Real-time Processing**: Stream processing capabilities
9. **Custom Transformations**: User-defined transformation functions
10. **Multi-tenant Support**: User authentication and workspace isolation

## 📚 Example Use Cases

1. **Database Migration**: MySQL → Transformations → PostgreSQL
2. **Data Export**: Oracle → Filter → CSV Export  
3. **API Integration**: REST API → Map → Database (planned)
4. **Report Generation**: Database → Group By → Excel Export (planned)
5. **Data Cleansing**: CSV → Multiple Transforms → Clean CSV
6. **Real-time ETL**: SQL Server → Transform → Multiple Outputs
7. **Cross-Platform Migration**: Oracle → Transform → MySQL
8. **Data Warehouse Loading**: Multiple Sources → Transform → Target Database

### Current Workflow Examples

#### Example 1: CSV Upload → Database Storage
```
📄 CSV Source (upload) → 🔍 Filter → 🔄 Map → 💾 SQL Output (PostgreSQL)
```

#### Example 2: Database Export → CSV
```  
🗄️ SQL Source (MySQL) → 📊 Group By → 📋 Sort → 📊 CSV Output
```

#### Example 3: Cross-Database Migration
```
🗄️ SQL Source (Oracle) → 🔍 Filter → 🔄 Map → 💾 SQL Output (MySQL)
```

#### Example 4: Data Transformation Pipeline
```
🗄️ SQL Source → 🔍 Filter → 🔗 Join → 📊 Group By → 📋 Sort → 💾 SQL Output
```

This architecture provides the foundation for a truly flexible and scalable ETL framework that can handle any data integration scenario with an intuitive, user-friendly interface! 🚀
