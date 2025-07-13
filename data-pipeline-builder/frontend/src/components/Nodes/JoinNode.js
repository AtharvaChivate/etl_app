import React from 'react';
import { Handle, Position } from 'reactflow';
import './NodeStyles.css';

const JoinNode = ({ data, id }) => {
  const handleChange = (field, value) => {
    data.onUpdate(id, { 
      ...data, 
      [field]: value,
      configured: !!(data.leftColumn && data.rightColumn && data.joinType && value)
    });
  };

  return (
    <div className="transformation-node join-node">
      <Handle type="target" position={Position.Left} style={{ background: '#e74c3c', top: '30%' }} id="left" />
      <Handle type="target" position={Position.Left} style={{ background: '#e74c3c', top: '70%' }} id="right" />
      
      <div className="node-header">
        🔗 Join Tables
      </div>
      
      <div className="node-content">
        <div className="form-group">
          <label>Join Type:</label>
          <select 
            value={data.joinType || 'inner'} 
            onChange={(e) => handleChange('joinType', e.target.value)}
          >
            <option value="inner">Inner Join</option>
            <option value="left">Left Join</option>
            <option value="right">Right Join</option>
            <option value="outer">Full Outer Join</option>
          </select>
        </div>
        
        <div className="form-group">
          <label>Left Column:</label>
          <input
            type="text"
            value={data.leftColumn || ''}
            onChange={(e) => handleChange('leftColumn', e.target.value)}
            placeholder="column from left table"
          />
        </div>
        
        <div className="form-group">
          <label>Right Column:</label>
          <input
            type="text"
            value={data.rightColumn || ''}
            onChange={(e) => handleChange('rightColumn', e.target.value)}
            placeholder="column from right table"
          />
        </div>
        
        {data.configured && (
          <div className="node-status status-success">
            ✅ {data.joinType} join on {data.leftColumn} = {data.rightColumn}
          </div>
        )}
      </div>
      
      <Handle type="source" position={Position.Right} style={{ background: '#27ae60' }} />
    </div>
  );
};

export default JoinNode;
