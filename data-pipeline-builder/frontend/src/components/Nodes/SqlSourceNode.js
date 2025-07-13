import React from 'react';
import { Handle, Position } from 'reactflow';

const SqlSourceNode = ({ data, selected }) => {
  return (
    <div className={`node sql-source-node ${selected ? 'selected' : ''}`}>
      <div className="node-header">
        <span className="node-icon">🗄️</span>
        <span className="node-title">SQL Source</span>
      </div>
      <div className="node-content">
        <div className="node-info">
          <strong>Database:</strong> {data.databaseType || 'Not configured'}
        </div>
        <div className="node-info">
          <strong>Host:</strong> {data.host || 'localhost'}
        </div>
        <div className="node-info">
          <strong>Table/Query:</strong> {data.tableName || data.query || 'Not set'}
        </div>
      </div>
      <Handle
        type="source"
        position={Position.Right}
        className="node-handle"
      />
    </div>
  );
};

export default SqlSourceNode;
