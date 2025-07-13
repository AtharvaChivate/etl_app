import React from 'react';
import { Handle, Position } from 'reactflow';
import './NodeStyles.css';

const SQLOutputNode = ({ data, id }) => {
  const handleChange = (field, value) => {
    data.onUpdate(id, { 
      ...data, 
      [field]: value,
      configured: !!(data.tableName && data.databaseType && value)
    });
  };

  return (
    <div className="transformation-node sql-output-node">
      <Handle type="target" position={Position.Left} style={{ background: '#e74c3c' }} />
      
      <div className="node-header">
        💾 SQL Output
      </div>
      
      <div className="node-content">
        <div className="form-group">
          <label>Database Type:</label>
          <select 
            value={data.databaseType || 'sqlite'} 
            onChange={(e) => handleChange('databaseType', e.target.value)}
          >
            <option value="sqlite">SQLite (Local - No Setup Required)</option>
            <option value="postgresql">PostgreSQL</option>
            <option value="mysql">MySQL</option>
          </select>
          <small style={{ color: '#7f8c8d', fontSize: '0.75rem', display: 'block', marginTop: '0.25rem' }}>
            SQLite tables are auto-created with proper data types
          </small>
        </div>
        
        <div className="form-group">
          <label>Table Name:</label>
          <input
            type="text"
            value={data.tableName || ''}
            onChange={(e) => handleChange('tableName', e.target.value)}
            placeholder="my_output_table"
          />
          <small style={{ color: '#7f8c8d', fontSize: '0.75rem', display: 'block', marginTop: '0.25rem' }}>
            Table will be created automatically if it doesn't exist
          </small>
        </div>
        
        <div className="form-group">
          <label>Write Mode:</label>
          <select 
            value={data.writeMode || 'replace'} 
            onChange={(e) => handleChange('writeMode', e.target.value)}
          >
            <option value="replace">Replace Table</option>
            <option value="append">Append to Table</option>
            <option value="fail">Fail if Exists</option>
          </select>
        </div>
        
        {data.databaseType !== 'sqlite' && (
          <>
            <div className="form-group">
              <label>Connection String:</label>
              <input
                type="text"
                value={data.connectionString || ''}
                onChange={(e) => handleChange('connectionString', e.target.value)}
                placeholder="jdbc:postgresql://localhost:5432/mydb"
              />
            </div>
            
            <div className="form-group">
              <label>Username:</label>
              <input
                type="text"
                value={data.username || ''}
                onChange={(e) => handleChange('username', e.target.value)}
                placeholder="database_username"
              />
            </div>
            
            <div className="form-group">
              <label>Password:</label>
              <input
                type="password"
                value={data.password || ''}
                onChange={(e) => handleChange('password', e.target.value)}
                placeholder="database_password"
              />
            </div>
          </>
        )}
        
        {data.configured && (
          <div className="node-status status-success">
            ✅ Output to {data.databaseType}: {data.tableName}
          </div>
        )}
        
        {!data.configured && data.tableName && (
          <div className="node-status status-warning">
            ⚠️ Please complete all required fields
          </div>
        )}
      </div>
    </div>
  );
};

export default SQLOutputNode;
