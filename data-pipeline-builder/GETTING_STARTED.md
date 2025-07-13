# 🚀 Getting Started with Visual Data Transformation Builder

A comprehensive guide to setting up and using the modern ETL framework with universal database support and interactive pipeline creation.

## Prerequisites

Before you begin, ensure you have the following installed:

### Required Software
- **Node.js** (version 16 or higher) - [Download here](https://nodejs.org/)
- **Java** (version 11 or higher) - [Download here](https://www.oracle.com/java/technologies/javase/jdk11-archive-downloads.html)
- **Git** (for cloning and version control) - [Download here](https://git-scm.com/)

### Optional but Recommended
- **VS Code** (for development) - [Download here](https://code.visualstudio.com/)
- **Maven** (Java build tool) - [Download here](https://maven.apache.org/) (Note: This project includes Maven Wrapper)
- **Database Client** (DBeaver, pgAdmin, etc.) for viewing output data

## 🔧 Setup Instructions

### Step 1: Verify Prerequisites

Open your terminal/command prompt and verify installations:

```powershell
# Check Node.js version (should be 16+)
node --version

# Check Java version (should be 11+)
java --version

# Check if npm is available
npm --version
```

### Step 2: Navigate to the Project

```powershell
cd "d:\My Learning\ETL_Framework\etl_app\data-pipeline-builder"
```

### Step 3: Set Up the Frontend (React)

```powershell
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install

# Start the development server
npm start
```

This will:
- Install all React dependencies
- Start the development server on http://localhost:3000
- Open your browser automatically

**Expected output:**
```
Compiled successfully!

You can now view data-pipeline-frontend in the browser.

  Local:            http://localhost:3000
  On Your Network:  http://192.168.1.xxx:3000
```

### Step 4: Set Up the Backend (Java Spring Boot)

Open a **new terminal window/tab** and run:

```powershell
# Navigate to backend directory (from project root)
cd backend

# Run the Spring Boot application using Maven Wrapper
./mvnw.cmd spring-boot:run
```

**For Linux/Mac users:**
```bash
./mvnw spring-boot:run
```

This will:
- Download Maven dependencies (first time only)
- Compile the Java code
- Start the Spring Boot server on http://localhost:8080

**Expected output:**
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::

Started PipelineApplication in 3.421 seconds
```

## 🏃‍♂️ Running the Application

### Once Both Services Are Running:

1. **Frontend**: http://localhost:3000 (React app)
2. **Backend**: http://localhost:8080/api (Spring Boot API)

### First Time Setup Verification:

1. Open your browser to http://localhost:3000
2. You should see the Data Pipeline Builder interface
3. The sidebar should show transformation tools
4. Try clicking the "Execute Pipeline" button to test connectivity

## 📝 Creating Your First Pipeline

### Step 1: Upload CSV Data
1. **File Upload**: Drag and drop a CSV file into the upload area, or click to browse
2. **Progress Tracking**: Watch the upload progress bar
3. **Confirmation**: Success message confirms file upload

### Step 2: Add SQL Source
1. **Drag Node**: Click on "SQL Source" in the simplified sidebar
2. **Configure Database**: Click the node to open the configuration panel
3. **Database Selection**: Choose from MySQL, PostgreSQL, SQLite, SQL Server, or Oracle
4. **Connection Details**: Enter your database connection information
5. **Test Connection**: Use the "Test Connection" button to verify connectivity

### Step 3: Add Transformations (Optional)
1. **Filter**: Add WHERE conditions to filter your data
2. **Map**: Transform columns with custom expressions  
3. **Group By**: Aggregate data by specified columns
4. **Sort**: Order data by one or more columns
5. **Join**: Combine data from multiple sources

### Step 4: Add Output Destination
1. **CSV Output**: Export processed data to CSV files
2. **SQL Output**: Write data to any supported database
3. **Auto Table Creation**: Tables are automatically created if they don't exist

### Step 5: Connect and Execute
1. **Connect Nodes**: Drag between connection points to link components
2. **Delete Connections**: Hover over any connection and click the delete button
3. **Execute Pipeline**: Click "Execute" to run your transformation
4. **View Results**: Results appear in a dismissible notification panel

## 🔄 Universal Database Support

This ETL framework provides **universal database connectivity** through a single SQL node type:

### Supported Databases
- **MySQL**: Full CRUD operations, auto table creation, connection testing
- **PostgreSQL**: Complete functionality with proper data type mapping
- **SQLite**: Local database support, perfect for development and testing
- **SQL Server**: Enterprise database integration with Windows authentication
- **Oracle**: Production-ready Oracle database connectivity

### Configuration Examples

#### SQLite (Development)
```
Database Type: SQLite
Connection URL: jdbc:sqlite:output/mydata.db
Table Name: processed_results
(No username/password required)
```

#### MySQL (Production)
```
Database Type: MySQL
Connection URL: jdbc:mysql://localhost:3306/analytics_db
Username: analytics_user
Password: secure_password
Table Name: customer_data
```

#### PostgreSQL (Production)
```
Database Type: PostgreSQL
Connection URL: jdbc:postgresql://localhost:5432/data_warehouse
Username: etl_user
Password: etl_password
Table Name: sales_summary
```

## 🛠️ Development Tips

### Frontend Development
- React components are in `frontend/src/components/`
- The main app logic is in `frontend/src/App.js`
- Styles are in corresponding `.css` files
- Hot reload is enabled - changes appear immediately

### Backend Development
- Java source code is in `backend/src/main/java/`
- REST endpoints are in the `controller` package
- Business logic is in the `service` package
- Database models are in the `model` package

### Making Changes
1. **Frontend changes**: Save the file and the browser will auto-refresh
2. **Backend changes**: 
   - Stop the server (Ctrl+C)
   - Run `./mvnw.cmd spring-boot:run` again
   - Or use your IDE's hot reload feature

## 🔍 Troubleshooting

### Common Issues

#### Connection Test Fails
If your database connection test fails:

1. **Verify Database Server**: Ensure your database server is running
2. **Check Credentials**: Confirm username, password, and database name
3. **Network Access**: Verify firewall settings and port accessibility
4. **Connection URL**: Ensure proper JDBC URL format for your database type

#### Pipeline Execution Issues
If pipeline execution fails or returns unexpected results:

1. **Node Configuration**: Verify all nodes are properly configured
2. **Connection Validation**: Test database connections before execution
3. **Data Validation**: Check input data format and content
4. **Permission Issues**: Ensure database user has appropriate write permissions

#### UI/UX Issues

**Connections Won't Delete**:
- Hover directly over the connection line to reveal the delete button
- Click the red delete button that appears on hover

**Execution Results Won't Dismiss**:
- Click the "X" button in the execution result header
- Results automatically dismiss after successful operations

**Nodes Not Updating**:
- Ensure all required fields are filled in the configuration panel
- Check browser console for any JavaScript errors

### Performance Tips

**Large Datasets**:
- Use appropriate database indexes for better query performance
- Consider batch processing for very large data sets
- Monitor memory usage during complex transformations

**Production Deployment**:
- Use connection pooling for database connections
- Implement proper error handling and logging
- Set up monitoring for pipeline execution times

## 🎉 Success Indicators

You'll know everything is working when:
- ✅ Frontend loads at http://localhost:3000 with simplified sidebar
- ✅ You can drag "SQL Source" nodes onto the canvas
- ✅ Database connection tests pass successfully
- ✅ Hover functionality reveals delete buttons on connections
- ✅ Pipeline execution shows success notifications
- ✅ Output data appears in your configured destination
- ✅ Execution result notifications can be dismissed with the close button

## 🚀 Advanced Features

### Interactive Edge Management
- **Hover-to-Delete**: Simply hover over any connection to reveal delete button
- **Visual Feedback**: Connections highlight on hover for better visibility
- **Keyboard Shortcuts**: Press Delete key while connection is selected

### Universal SQL Support  
- **Single Node Type**: One "SQL Source" works with all database types
- **Auto Detection**: System automatically handles database-specific syntax
- **Connection Testing**: Real-time validation before pipeline execution

### Enhanced User Experience
- **Simplified Sidebar**: Clean interface with essential components only
- **Dismissible Results**: Click "X" to close execution result notifications
- **Progress Tracking**: Real-time feedback during file uploads and execution

Happy data pipeline building! 🚀
