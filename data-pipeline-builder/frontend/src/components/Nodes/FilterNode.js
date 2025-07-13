import React from 'react';
import { Handle, Position } from 'reactflow';
import './NodeStyles.css';

const FilterNode = ({ data, id }) => {
  const handleColumnChange = (e) => {
    data.onUpdate(id, { 
      ...data, 
      column: e.target.value,
      configured: !!(e.target.value && data.operator && data.value)
    });
  };

  const handleOperatorChange = (e) => {
    data.onUpdate(id, { 
      ...data, 
      operator: e.target.value,
      configured: !!(data.column && e.target.value && data.value)
    });
  };

  const handleValueChange = (e) => {
    data.onUpdate(id, { 
      ...data, 
      value: e.target.value,
      configured: !!(data.column && data.operator && e.target.value)
    });
  };

  return (
    <div className="transformation-node filter-node">
      {/* Input handle - data flows in from here */}
      <Handle 
        type="target" 
        position={Position.Left} 
        style={{ background: '#e74c3c' }}
      />
      
      <div className="node-header">
        🔍 Filter Rows
      </div>
      
      <div className="node-content">
        <div className="form-group">
          <label>Column to filter:</label>
          <input
            type="text"
            value={data.column || ''}
            onChange={handleColumnChange}
            placeholder="e.g., age, price, status"
          />
        </div>
        
        <div className="form-group">
          <label>Condition:</label>
          <select 
            value={data.operator || '=='} 
            onChange={handleOperatorChange}
          >
            <option value="==">Equals (==)</option>
            <option value="!=">Not Equals (!=)</option>
            <option value=">">Greater Than (&gt;)</option>
            <option value="<">Less Than (&lt;)</option>
            <option value=">=">Greater or Equal (&gt;=)</option>
            <option value="<=">Less or Equal (&lt;=)</option>
            <option value="contains">Contains</option>
            <option value="startswith">Starts With</option>
            <option value="endswith">Ends With</option>
          </select>
        </div>
        
        <div className="form-group">
          <label>Value:</label>
          <input
            type="text"
            value={data.value || ''}
            onChange={handleValueChange}
            placeholder="e.g., 25, 'Active', 100.5"
          />
        </div>
        
        {data.configured && (
          <div className="node-status status-success">
            ✅ Filter: {data.column} {data.operator} {data.value}
          </div>
        )}
        
        {!data.configured && (data.column || data.operator || data.value) && (
          <div className="node-status status-warning">
            ⚠️ Please fill all filter fields
          </div>
        )}
      </div>
      
      {/* Output handle - filtered data flows out */}
      <Handle 
        type="source" 
        position={Position.Right} 
        style={{ background: '#27ae60' }}
      />
    </div>
  );
};

export default FilterNode;
