import React from 'react';
import { Handle, Position } from 'reactflow';
import './NodeStyles.css';

const MapNode = ({ data, id }) => {
  const handleMappingChange = (index, field, value) => {
    const mappings = [...(data.mappings || [])];
    mappings[index] = { ...mappings[index], [field]: value };
    
    data.onUpdate(id, { 
      ...data, 
      mappings,
      configured: mappings.some(m => m.sourceColumn && m.targetColumn)
    });
  };

  const addMapping = () => {
    const mappings = [...(data.mappings || []), { sourceColumn: '', targetColumn: '', operation: 'copy' }];
    data.onUpdate(id, { ...data, mappings });
  };

  const removeMapping = (index) => {
    const mappings = (data.mappings || []).filter((_, i) => i !== index);
    data.onUpdate(id, { 
      ...data, 
      mappings,
      configured: mappings.some(m => m.sourceColumn && m.targetColumn)
    });
  };

  return (
    <div className="transformation-node map-node">
      <Handle type="target" position={Position.Left} style={{ background: '#e74c3c' }} />
      
      <div className="node-header">
        🔄 Map Columns
      </div>
      
      <div className="node-content">
        {(data.mappings || []).map((mapping, index) => (
          <div key={index} style={{ marginBottom: '1rem', border: '1px solid #eee', padding: '0.5rem', borderRadius: '4px' }}>
            <div className="form-group">
              <label>Source Column:</label>
              <input
                type="text"
                value={mapping.sourceColumn || ''}
                onChange={(e) => handleMappingChange(index, 'sourceColumn', e.target.value)}
                placeholder="original_column_name"
              />
            </div>
            
            <div className="form-group">
              <label>Target Column:</label>
              <input
                type="text"
                value={mapping.targetColumn || ''}
                onChange={(e) => handleMappingChange(index, 'targetColumn', e.target.value)}
                placeholder="new_column_name"
              />
            </div>
            
            <div className="form-group">
              <label>Operation:</label>
              <select 
                value={mapping.operation || 'copy'} 
                onChange={(e) => handleMappingChange(index, 'operation', e.target.value)}
              >
                <option value="copy">Copy</option>
                <option value="uppercase">Uppercase</option>
                <option value="lowercase">Lowercase</option>
                <option value="multiply">Multiply by factor</option>
                <option value="add">Add value</option>
              </select>
            </div>
            
            {(mapping.operation === 'multiply' || mapping.operation === 'add') && (
              <div className="form-group">
                <label>Value:</label>
                <input
                  type="number"
                  value={mapping.value || ''}
                  onChange={(e) => handleMappingChange(index, 'value', e.target.value)}
                  placeholder="Enter number"
                />
              </div>
            )}
            
            <button 
              onClick={() => removeMapping(index)} 
              style={{ background: '#e74c3c', color: 'white', border: 'none', padding: '0.25rem 0.5rem', borderRadius: '3px', fontSize: '0.8rem' }}
            >
              Remove
            </button>
          </div>
        ))}
        
        <button 
          onClick={addMapping}
          style={{ background: '#3498db', color: 'white', border: 'none', padding: '0.5rem 1rem', borderRadius: '4px', width: '100%' }}
        >
          + Add Column Mapping
        </button>
        
        {data.configured && (
          <div className="node-status status-success">
            ✅ {(data.mappings || []).length} column mapping(s) configured
          </div>
        )}
      </div>
      
      <Handle type="source" position={Position.Right} style={{ background: '#27ae60' }} />
    </div>
  );
};

export default MapNode;
