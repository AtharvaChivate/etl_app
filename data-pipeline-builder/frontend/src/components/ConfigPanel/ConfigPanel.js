import React, { useState, useEffect } from 'react';
import './ConfigPanel.css';

const ConfigPanel = ({ selectedNode, updateNodeData, deleteNode }) => {
  const [testingConnection, setTestingConnection] = useState(false);
  const [connectionResult, setConnectionResult] = useState(null);
  const [localFormData, setLocalFormData] = useState({});

  // Update local state when selectedNode changes
  useEffect(() => {
    if (selectedNode && selectedNode.data) {
      setLocalFormData({
        filePath: selectedNode.data.filePath || '',
        connectionString: selectedNode.data.connectionString || '',
        host: selectedNode.data.host || '',
        port: selectedNode.data.port || '',
        database: selectedNode.data.database || '',
        username: selectedNode.data.username || '',
        password: selectedNode.data.password || '',
        tableName: selectedNode.data.tableName || '',
        query: selectedNode.data.query || ''
      });
    }
  }, [selectedNode]);

  if (!selectedNode) {
    return (
      <div className="config-panel">
        <div className="config-header">
          <h3>⚙️ Configuration</h3>
        </div>
        <div className="config-content">
          <div className="no-selection">
            <p>Select a node to configure its properties</p>
            <div className="help-text">
              <h4>💡 Quick Tips:</h4>
              <ul>
                <li>Click on any node to see its configuration</li>
                <li>Use handles to connect nodes</li>
                <li>Build your pipeline from source to output</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    );
  }

  const getNodeTypeInfo = (type) => {
    const nodeInfo = {
      // Data Sources
      csvSource: { icon: '📄', name: 'CSV Source', description: 'Import data from CSV files' },
      sqlSource: { icon: '🗄️', name: 'SQL Source', description: 'Import data from SQL database' },
      mysqlSource: { icon: '�️', name: 'SQL Source', description: 'Import data from SQL database' },
      postgresqlSource: { icon: '�️', name: 'SQL Source', description: 'Import data from SQL database' },
      sqliteSource: { icon: '🗄️', name: 'SQL Source', description: 'Import data from SQL database' },
      
      // Transformations
      filter: { icon: '🔍', name: 'Filter', description: 'Filter rows based on conditions' },
      map: { icon: '🔄', name: 'Map', description: 'Transform and rename columns' },
      groupBy: { icon: '📊', name: 'Group By', description: 'Group data and apply aggregations' },
      join: { icon: '🔗', name: 'Join', description: 'Combine data from multiple sources' },
      sort: { icon: '📋', name: 'Sort', description: 'Sort data by specified columns' },
      
      // Data Outputs
      csvOutput: { icon: '📊', name: 'CSV Output', description: 'Export data to CSV file' },
      sqlOutput: { icon: '💾', name: 'SQL Output', description: 'Save results to database' },
    };
    return nodeInfo[type] || { icon: '🔧', name: type, description: 'Transformation node' };
  };

  const nodeInfo = getNodeTypeInfo(selectedNode.type);

  const handleDeleteNode = () => {
    if (window.confirm('Are you sure you want to delete this node?')) {
      deleteNode(selectedNode.id);
    }
  };

  const renderConfigurationForm = () => {
    const nodeType = selectedNode.type;
    
    // SQL Source Configuration
    if (['sqlSource', 'mysqlSource', 'postgresqlSource', 'sqliteSource'].includes(nodeType)) {
      return (
        <div className="form-group" key={`sql-config-${selectedNode.id}`}>
          <label>Database Type:</label>
          <select 
            value={selectedNode.data.databaseType || ''} 
            onChange={(e) => updateNodeData(selectedNode.id, { databaseType: e.target.value })}
          >
            <option value="">Select Database Type</option>
            <option value="mysql">MySQL</option>
            <option value="postgresql">PostgreSQL</option>
            <option value="sqlite">SQLite</option>
            <option value="mssql">SQL Server</option>
            <option value="oracle">Oracle</option>
          </select>
          
          <label>Connection String:</label>
          <input
            type="text"
            placeholder="jdbc:mysql://localhost:3306/mydb"
            value={localFormData.connectionString}
            onChange={(e) => updateFormField('connectionString', e.target.value)}
            autoComplete="off"
          />
          
          <label>Host:</label>
          <input
            type="text"
            placeholder="localhost"
            value={localFormData.host}
            onChange={(e) => updateFormField('host', e.target.value)}
            autoComplete="off"
          />
          
          <label>Port:</label>
          <input
            type="text"
            placeholder="3306"
            value={localFormData.port}
            onChange={(e) => updateFormField('port', e.target.value)}
            autoComplete="off"
          />
          
          <label>Database Name:</label>
          <input
            type="text"
            placeholder="mydb"
            value={localFormData.database}
            onChange={(e) => updateFormField('database', e.target.value)}
            autoComplete="off"
          />
          
          <label>Username:</label>
          <input
            type="text"
            placeholder="root"
            value={localFormData.username}
            onChange={(e) => updateFormField('username', e.target.value)}
            autoComplete="off"
          />
          
          <label>Password:</label>
          <input
            type="password"
            placeholder="your_password"
            value={localFormData.password}
            onChange={(e) => updateFormField('password', e.target.value)}
            autoComplete="off"
          />
          
          <label>Table Name or SQL Query:</label>
          <textarea
            placeholder="Table: employees OR Query: SELECT * FROM employees WHERE active = 1"
            value={localFormData.query}
            onChange={(e) => updateFormField('query', e.target.value)}
            rows={3}
          />
          
          <div className="connection-test">
            <button 
              className="test-connection-btn"
              onClick={testDatabaseConnection}
              disabled={testingConnection}
            >
              {testingConnection ? '🔄 Testing...' : '🔌 Test Connection'}
            </button>
            
            {connectionResult && (
              <div className={`connection-result ${connectionResult.success ? 'success' : 'error'}`}>
                {connectionResult.success ? '✅' : '❌'} {connectionResult.message}
              </div>
            )}
          </div>
        </div>
      );
    }
    
    // CSV Source Configuration
    if (nodeType === 'csvSource') {
      return (
        <div className="form-group" key={`csv-config-${selectedNode.id}`}>
          <label>CSV File Path:</label>
          <input
            type="text"
            placeholder="sample-data/employees.csv or uploads/myfile.csv"
            value={localFormData.filePath}
            onChange={(e) => updateFormField('filePath', e.target.value)}
            autoComplete="off"
          />
          
          <label>Delimiter:</label>
          <select 
            value={selectedNode.data.delimiter || ','} 
            onChange={(e) => updateNodeData(selectedNode.id, { delimiter: e.target.value })}
          >
            <option value=",">Comma (,)</option>
            <option value=";">Semicolon (;)</option>
            <option value="\t">Tab</option>
            <option value="|">Pipe (|)</option>
          </select>
          
          <label>
            <input
              type="checkbox"
              checked={selectedNode.data.hasHeaders !== false}
              onChange={(e) => updateNodeData(selectedNode.id, { hasHeaders: e.target.checked })}
            />
            First row contains headers
          </label>
          
          <div className="file-upload-section">
            <h4>Or Upload New File:</h4>
            <input
              type="file"
              accept=".csv"
              onChange={(e) => {
                const file = e.target.files[0];
                if (file) {
                  updateNodeData(selectedNode.id, { 
                    uploadedFile: file,
                    filePath: `uploads/${file.name}`,
                    fileName: file.name
                  });
                }
              }}
            />
            {selectedNode.data.fileName && (
              <p>Selected: {selectedNode.data.fileName}</p>
            )}
          </div>
        </div>
      );
    }
    
    // CSV Output Configuration
    if (nodeType === 'csvOutput') {
      return (
        <div className="form-group" key={`csv-output-config-${selectedNode.id}`}>
          <label>Output File Path:</label>
          <input
            type="text"
            placeholder="output/employees_sorted.csv"
            value={localFormData.filePath}
            onChange={(e) => updateFormField('filePath', e.target.value)}
            autoComplete="off"
          />
          <small style={{color: '#666', fontSize: '12px', display: 'block', marginTop: '4px'}}>
            💡 Specify the complete path with filename and .csv extension (e.g., output/result.csv)
          </small>
          
          <label>Delimiter:</label>
          <select 
            value={selectedNode.data.delimiter || ','} 
            onChange={(e) => updateNodeData(selectedNode.id, { delimiter: e.target.value })}
          >
            <option value=",">Comma (,)</option>
            <option value=";">Semicolon (;)</option>
            <option value="\t">Tab</option>
            <option value="|">Pipe (|)</option>
          </select>
          
          <label>
            <input
              type="checkbox"
              checked={selectedNode.data.includeHeaders !== false}
              onChange={(e) => updateNodeData(selectedNode.id, { includeHeaders: e.target.checked })}
            />
            Include Headers
          </label>
        </div>
      );
    }
    
    // SQL Output Configuration
    if (nodeType === 'sqlOutput') {
      return (
        <div className="form-group" key={`sql-output-config-${selectedNode.id}`}>
          <label>Database Type:</label>
          <select 
            value={selectedNode.data.databaseType || ''} 
            onChange={(e) => updateNodeData(selectedNode.id, { databaseType: e.target.value })}
          >
            <option value="">Select Database Type</option>
            <option value="mysql">MySQL</option>
            <option value="postgresql">PostgreSQL</option>
            <option value="sqlite">SQLite</option>
            <option value="mssql">SQL Server</option>
            <option value="oracle">Oracle</option>
          </select>
          
          <label>Connection String (Optional):</label>
          <input
            type="text"
            placeholder="jdbc:mysql://localhost:3306/mydb"
            value={localFormData.connectionString}
            onChange={(e) => updateFormField('connectionString', e.target.value)}
            autoComplete="off"
          />
          <small style={{color: '#666', fontSize: '12px', display: 'block', marginTop: '4px'}}>
            💡 Leave blank to use individual connection parameters below
          </small>
          
          <label>Host:</label>
          <input
            type="text"
            placeholder="localhost"
            value={localFormData.host}
            onChange={(e) => updateFormField('host', e.target.value)}
            autoComplete="off"
          />
          
          <label>Port:</label>
          <input
            type="text"
            placeholder="3306"
            value={localFormData.port}
            onChange={(e) => updateFormField('port', e.target.value)}
            autoComplete="off"
          />
          
          <label>Database Name:</label>
          <input
            type="text"
            placeholder="mydb"
            value={localFormData.database}
            onChange={(e) => updateFormField('database', e.target.value)}
            autoComplete="off"
          />
          
          <label>Username:</label>
          <input
            type="text"
            placeholder="root"
            value={localFormData.username}
            onChange={(e) => updateFormField('username', e.target.value)}
            autoComplete="off"
          />
          
          <label>Password:</label>
          <input
            type="password"
            placeholder="your_password"
            value={localFormData.password}
            onChange={(e) => updateFormField('password', e.target.value)}
            autoComplete="off"
          />
          
          <label>Table Name:</label>
          <input
            type="text"
            placeholder="output_table"
            value={localFormData.tableName}
            onChange={(e) => updateFormField('tableName', e.target.value)}
            autoComplete="off"
          />
          <small style={{color: '#666', fontSize: '12px', display: 'block', marginTop: '4px'}}>
            💡 Table will be created automatically if it doesn't exist
          </small>
          
          <div className="connection-test">
            <button 
              className="test-connection-btn"
              onClick={testDatabaseConnection}
              disabled={testingConnection}
            >
              {testingConnection ? '🔄 Testing...' : '🔌 Test Connection'}
            </button>
            
            {connectionResult && (
              <div className={`connection-result ${connectionResult.success ? 'success' : 'error'}`}>
                {connectionResult.success ? '✅' : '❌'} {connectionResult.message}
              </div>
            )}
          </div>
        </div>
      );
    }
    
    // Default: show existing data properties as form fields
    return (
      <div className="form-group">
        <p>Configuration options will be added here based on node type.</p>
      </div>
    );
  };

  // Helper function to update both local state and node data
  const updateFormField = (fieldName, value) => {
    setLocalFormData(prev => ({ ...prev, [fieldName]: value }));
    updateNodeData(selectedNode.id, { [fieldName]: value });
  };

  const testDatabaseConnection = async () => {
    if (!['sqlSource', 'mysqlSource', 'postgresqlSource', 'sqliteSource', 'sqlOutput'].includes(selectedNode.type)) {
      return;
    }

    setTestingConnection(true);
    setConnectionResult(null);

    try {
      const response = await fetch('http://localhost:8080/api/database/test-connection', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(selectedNode.data),
      });

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }

      const result = await response.json();
      setConnectionResult(result);
      
      if (result.success) {
        // Mark node as configured when connection is successful
        updateNodeData(selectedNode.id, { configured: true });
      }
      
    } catch (error) {
      console.error('Connection test error:', error);
      setConnectionResult({
        success: false,
        message: 'Connection test failed: ' + error.message
      });
    } finally {
      setTestingConnection(false);
    }
  };

  return (
    <div className="config-panel">
      <div className="config-header">
        <h3>⚙️ Configuration</h3>
      </div>
      
      <div className="config-content">
        <div className="node-info">
          <div className="node-type">
            <span className="node-icon">{nodeInfo.icon}</span>
            <div>
              <h4>{nodeInfo.name}</h4>
              <p>{nodeInfo.description}</p>
            </div>
          </div>
          
          <div className="node-id">
            <strong>Node ID:</strong> {selectedNode.id}
          </div>
        </div>
        
        <div className="config-section">
          <h4>Configuration Status</h4>
          <div className={`status-indicator ${selectedNode.data.configured ? 'configured' : 'pending'}`}>
            {selectedNode.data.configured ? (
              <>✅ Configured</>
            ) : (
              <>⚠️ Needs Configuration</>
            )}
          </div>
        </div>
        
        <div className="config-section">
          <h4>Properties</h4>
          <div className="properties-list">
            {Object.entries(selectedNode.data)
              .filter(([key]) => !['label', 'onUpdate', 'configured'].includes(key))
              .map(([key, value]) => (
                <div key={key} className="property">
                  <strong>{key.replace(/([A-Z])/g, ' $1').replace(/^./, str => str.toUpperCase())}:</strong>
                  <span>{Array.isArray(value) ? `${value.length} items` : String(value) || 'Not set'}</span>
                </div>
              ))}
          </div>
        </div>
        
        <div className="config-section">
          <h4>Configuration</h4>
          {renderConfigurationForm()}
        </div>
        
        <div className="config-actions">
          <button className="delete-btn" onClick={handleDeleteNode}>
            🗑️ Delete Node
          </button>
        </div>
        
        <div className="help-section">
          <h4>📖 Help</h4>
          <div className="help-content">
            {selectedNode.type === 'csvSource' && (
              <p>Upload a CSV file or specify the file path. This will be the starting point of your data pipeline.</p>
            )}
            {['sqlSource', 'mysqlSource', 'postgresqlSource', 'sqliteSource'].includes(selectedNode.type) && (
              <div>
                <p>Connect to a SQL database to import data. You can:</p>
                <ul>
                  <li>Use a connection string (e.g., jdbc:mysql://localhost:3306/mydb)</li>
                  <li>Or fill in host, port, database name individually</li>
                  <li>Specify a table name or write a custom SQL query</li>
                </ul>
              </div>
            )}
            {selectedNode.type === 'csvOutput' && (
              <div>
                <p>Export your processed data to a CSV file. Configure:</p>
                <ul>
                  <li>Output file path (will create directories if needed)</li>
                  <li>Delimiter (comma, semicolon, tab, etc.)</li>
                  <li>Whether to include column headers</li>
                </ul>
              </div>
            )}
            {selectedNode.type === 'filter' && (
              <p>Filter rows by specifying a column, condition, and value. Only rows matching the condition will pass through.</p>
            )}
            {selectedNode.type === 'map' && (
              <p>Transform columns by renaming, changing data types, or applying simple operations like uppercase or mathematical calculations.</p>
            )}
            {selectedNode.type === 'groupBy' && (
              <p>Group data by one or more columns and apply aggregation functions like sum, count, or average.</p>
            )}
            {selectedNode.type === 'join' && (
              <p>Combine data from two sources based on matching column values. Connect two nodes to this join node.</p>
            )}
            {selectedNode.type === 'sort' && (
              <p>Sort the data by one or more columns in ascending or descending order.</p>
            )}
            {selectedNode.type === 'sqlOutput' && (
              <div>
                <p>Save the final result to a database table. This should typically be the last node in your pipeline.</p>
                <ul>
                  <li>Supports MySQL, PostgreSQL, SQLite, SQL Server, and Oracle</li>
                  <li>Use connection string or individual connection parameters</li>
                  <li>Table will be created automatically if it doesn't exist</li>
                  <li>Data types are auto-detected from your data</li>
                </ul>
              </div>
            )}
          </div>
        </div>
        
        <div className="connection-test">
          <h4>🔌 Test Connection</h4>
          <button 
            className="test-connection-btn"
            onClick={testDatabaseConnection}
            disabled={testingConnection}
          >
            {testingConnection ? 'Testing...' : 'Test Connection'}
          </button>
          
          {connectionResult && (
            <div className={`connection-result ${connectionResult.success ? 'success' : 'error'}`}>
              {connectionResult.success ? '✅ Connection successful' : `❌ ${connectionResult.message}`}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default ConfigPanel;
