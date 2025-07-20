# Visual Data Transformation Builder

A modern drag-and-drop ETL framework with React frontend and Java Spring Boot backend, supporting universal database connectivity and intuitive pipeline creation.

## 🚀 Key Features

- **🎯 Universal Database Support**: Single SQL node type supporting MySQL, PostgreSQL, SQLite, SQL Server, and Oracle
- **🔗 Interactive Connections**: Hover over connections to delete them with a simple click
- **📊 Comprehensive Transformations**: Filter, Map, Group By, Sort, Join, and more
- **💾 Flexible Output**: Export to CSV files or any supported database
- **🎨 Modern UI**: Simplified sidebar, dismissible execution results, and enhanced configuration panels
- **🔌 Connection Testing**: Real-time database connection validation
- **📁 File Upload**: Drag-and-drop CSV file uploads with progress tracking

## Project Structure

```
data-pipeline-builder/
├── frontend/                    # React application
│   ├── public/
│   │   └── index.html
│   ├── src/
│   │   ├── components/          # React components
│   │   │   ├── Edges/           # Deletable edge components
│   │   │   ├── Nodes/           # Transformation node components
│   │   │   ├── Sidebar/         # Simplified node palette
│   │   │   ├── ConfigPanel/     # Enhanced configuration panel
│   │   │   ├── DagManager/      # Pipeline save/load manager
│   │   │   └── FileUpload/      # File upload components
│   │   ├── utils/               # Utility functions
│   │   ├── services/            # API service calls
│   │   ├── App.js               # Main application with edge management
│   │   └── index.js
│   ├── package.json
│   └── package-lock.json
├── backend/                     # Java Spring Boot application
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── com/etl/pipeline/
│   │       │       ├── controller/      # REST controllers
│   │       │       ├── service/         # Universal database services
│   │       │       ├── datasource/      # Universal data source implementations
│   │       │       ├── model/           # Data models
│   │       │       ├── config/          # Configuration
│   │       │       └── PipelineApplication.java
│   │       └── resources/
│   │           ├── application.properties
│   │           └── static/
│   ├── output/                  # Generated output files
│   ├── uploads/                 # Uploaded CSV files
│   ├── saved-dags/             # Saved pipeline configurations
│   ├── pom.xml
│   └── mvnw
├── sample-data/                 # Sample CSV files for testing
└── README.md
```

## Quick Start

### Prerequisites
- Node.js (v16 or higher)
- Java 11 or higher
- Maven 3.6+

### Setup Instructions

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd data-pipeline-builder
   ```

2. **Install frontend dependencies**
   ```powershell
   cd frontend
   npm install
   ```

3. **Build and run backend**
   ```powershell
   cd backend
   mvn clean install
   mvn spring-boot:run
   ```

4. **Start frontend development server**
   ```powershell
   cd frontend
   npm start
   ```

5. **Access the application**
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8080

### Supported Databases

Configure the connection in your SQL nodes using these formats:

- **MySQL**: `jdbc:mysql://localhost:3306/database_name`
- **PostgreSQL**: `jdbc:postgresql://localhost:5432/database_name`  
- **SQLite**: `jdbc:sqlite:path/to/database.db`
- **SQL Server**: `jdbc:sqlserver://localhost:1433;databaseName=database_name`
- **Oracle**: `jdbc:oracle:thin:@localhost:1521:xe`

## Usage Guide

### Creating a Pipeline

1. **Upload Data**: Drag CSV files into the upload area
2. **Build Pipeline**: Drag "SQL Source" from sidebar to canvas
3. **Configure Nodes**: Click nodes to configure database connections or transformations
4. **Connect Components**: Drag between node connection points
5. **Delete Connections**: Hover over connections and click the delete button
6. **Execute Pipeline**: Click "Execute" to run your data transformation
7. **Save/Load**: Use DAG Manager to save and reload pipeline configurations

### Database Configuration

- Select database type from dropdown
- Enter connection details (host, port, database, credentials)
- Test connection using the "Test Connection" button
- Configure table name for output operations

### Available Transformations

- **Filter**: Apply WHERE conditions to filter data
- **Map**: Transform columns with custom expressions
- **Group By**: Aggregate data by specified columns
- **Sort**: Order data by one or more columns
- **Join**: Combine data from multiple sources

## 🔧 Configuration Examples

### Local Development (SQLite)
```
Database Type: SQLite
Connection URL: jdbc:sqlite:output/test.db
Table Name: my_output_table
(No username/password required)
```

### Production Database Examples
```
# MySQL
Database Type: MySQL
Connection URL: jdbc:mysql://localhost:3306/etl_database
Username: your_username
Password: your_password
Table Name: processed_data

# PostgreSQL
Database Type: PostgreSQL
Connection URL: jdbc:postgresql://localhost:5432/analytics_db
Username: postgres
Password: your_password
Table Name: customer_insights
```

## 🚨 Troubleshooting

### Common Issues

**Pipeline Execution Fails:**
- Check database connection settings
- Verify table permissions for write operations
- Ensure all required node configurations are complete

**Connection Test Fails:**
- Verify database server is running
- Check firewall settings and port accessibility
- Confirm credentials and database existence

**File Upload Issues:**
- Ensure CSV files are properly formatted
- Check file size limits (backend configuration)
- Verify upload directory permissions

**UI/UX Tips:**
- Click execution result header "X" to dismiss notifications
- Hover over connections to reveal delete buttons
- Use generic "SQL Source" for any database type
- Test connections before pipeline execution

### Performance Optimization

- Use appropriate database indexes for large datasets
- Consider batch processing for very large files
- Monitor memory usage during complex transformations
- Use connection pooling for production deployments

For detailed troubleshooting and advanced configuration, see [GETTING_STARTED.md](GETTING_STARTED.md#troubleshooting).
