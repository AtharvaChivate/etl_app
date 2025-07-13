import React from 'react';
import { Handle, Position } from 'reactflow';
import './NodeStyles.css';

const SortNode = ({ data, id }) => {
  const handleSortColumnsChange = (index, field, value) => {
    const sortColumns = [...(data.sortColumns || [])];
    sortColumns[index] = { ...sortColumns[index], [field]: value };
    
    data.onUpdate(id, { 
      ...data, 
      sortColumns,
      configured: sortColumns.some(s => s.column)
    });
  };

  const addSortColumn = () => {
    const sortColumns = [...(data.sortColumns || []), { column: '', direction: 'asc' }];
    data.onUpdate(id, { ...data, sortColumns });
  };

  const removeSortColumn = (index) => {
    const sortColumns = (data.sortColumns || []).filter((_, i) => i !== index);
    data.onUpdate(id, { 
      ...data, 
      sortColumns,
      configured: sortColumns.some(s => s.column)
    });
  };

  return (
    <div className="transformation-node sort-node">
      <Handle type="target" position={Position.Left} style={{ background: '#e74c3c' }} />
      
      <div className="node-header">
        📋 Sort Data
      </div>
      
      <div className="node-content">
        {(data.sortColumns || []).map((sort, index) => (
          <div key={index} style={{ marginBottom: '0.75rem', border: '1px solid #eee', padding: '0.5rem', borderRadius: '4px' }}>
            <div className="form-group">
              <label>Column:</label>
              <input
                type="text"
                value={sort.column || ''}
                onChange={(e) => handleSortColumnsChange(index, 'column', e.target.value)}
                placeholder="column_to_sort"
              />
            </div>
            
            <div className="form-group">
              <label>Direction:</label>
              <select 
                value={sort.direction || 'asc'} 
                onChange={(e) => handleSortColumnsChange(index, 'direction', e.target.value)}
              >
                <option value="asc">Ascending</option>
                <option value="desc">Descending</option>
              </select>
            </div>
            
            <button 
              onClick={() => removeSortColumn(index)} 
              style={{ background: '#e74c3c', color: 'white', border: 'none', padding: '0.25rem 0.5rem', borderRadius: '3px', fontSize: '0.8rem' }}
            >
              Remove
            </button>
          </div>
        ))}
        
        <button 
          onClick={addSortColumn}
          style={{ background: '#3498db', color: 'white', border: 'none', padding: '0.5rem 1rem', borderRadius: '4px', width: '100%' }}
        >
          + Add Sort Column
        </button>
        
        {data.configured && (
          <div className="node-status status-success">
            ✅ Sort by {(data.sortColumns || []).length} column(s)
          </div>
        )}
      </div>
      
      <Handle type="source" position={Position.Right} style={{ background: '#27ae60' }} />
    </div>
  );
};

export default SortNode;
