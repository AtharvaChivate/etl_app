import React from 'react';
import { Handle, Position } from 'reactflow';
import './NodeStyles.css';

const GroupByNode = ({ data, id }) => {
  const handleGroupColumnsChange = (e) => {
    const columns = e.target.value.split(',').map(c => c.trim()).filter(c => c);
    data.onUpdate(id, { 
      ...data, 
      groupColumns: columns,
      configured: !!(columns.length > 0 && data.aggregations && data.aggregations.length > 0)
    });
  };

  const handleAggregationChange = (index, field, value) => {
    const aggregations = [...(data.aggregations || [])];
    aggregations[index] = { ...aggregations[index], [field]: value };
    
    data.onUpdate(id, { 
      ...data, 
      aggregations,
      configured: !!(data.groupColumns && data.groupColumns.length > 0 && aggregations.some(a => a.column && a.function))
    });
  };

  const addAggregation = () => {
    const aggregations = [...(data.aggregations || []), { column: '', function: 'sum', alias: '' }];
    data.onUpdate(id, { ...data, aggregations });
  };

  const removeAggregation = (index) => {
    const aggregations = (data.aggregations || []).filter((_, i) => i !== index);
    data.onUpdate(id, { 
      ...data, 
      aggregations,
      configured: !!(data.groupColumns && data.groupColumns.length > 0 && aggregations.some(a => a.column && a.function))
    });
  };

  return (
    <div className="transformation-node groupby-node">
      <Handle type="target" position={Position.Left} style={{ background: '#e74c3c' }} />
      
      <div className="node-header">
        📊 Group By & Aggregate
      </div>
      
      <div className="node-content">
        <div className="form-group">
          <label>Group by columns (comma-separated):</label>
          <input
            type="text"
            value={(data.groupColumns || []).join(', ')}
            onChange={handleGroupColumnsChange}
            placeholder="e.g., category, region"
          />
        </div>
        
        <div style={{ marginTop: '1rem' }}>
          <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>Aggregations:</label>
          
          {(data.aggregations || []).map((agg, index) => (
            <div key={index} style={{ marginBottom: '0.75rem', border: '1px solid #eee', padding: '0.5rem', borderRadius: '4px' }}>
              <div className="form-group">
                <label>Column:</label>
                <input
                  type="text"
                  value={agg.column || ''}
                  onChange={(e) => handleAggregationChange(index, 'column', e.target.value)}
                  placeholder="column_to_aggregate"
                />
              </div>
              
              <div className="form-group">
                <label>Function:</label>
                <select 
                  value={agg.function || 'sum'} 
                  onChange={(e) => handleAggregationChange(index, 'function', e.target.value)}
                >
                  <option value="sum">Sum</option>
                  <option value="count">Count</option>
                  <option value="avg">Average</option>
                  <option value="min">Minimum</option>
                  <option value="max">Maximum</option>
                </select>
              </div>
              
              <div className="form-group">
                <label>Alias (optional):</label>
                <input
                  type="text"
                  value={agg.alias || ''}
                  onChange={(e) => handleAggregationChange(index, 'alias', e.target.value)}
                  placeholder="new_column_name"
                />
              </div>
              
              <button 
                onClick={() => removeAggregation(index)} 
                style={{ background: '#e74c3c', color: 'white', border: 'none', padding: '0.25rem 0.5rem', borderRadius: '3px', fontSize: '0.8rem' }}
              >
                Remove
              </button>
            </div>
          ))}
          
          <button 
            onClick={addAggregation}
            style={{ background: '#3498db', color: 'white', border: 'none', padding: '0.5rem 1rem', borderRadius: '4px', width: '100%' }}
          >
            + Add Aggregation
          </button>
        </div>
        
        {data.configured && (
          <div className="node-status status-success">
            ✅ Group by {(data.groupColumns || []).length} column(s), {(data.aggregations || []).length} aggregation(s)
          </div>
        )}
      </div>
      
      <Handle type="source" position={Position.Right} style={{ background: '#27ae60' }} />
    </div>
  );
};

export default GroupByNode;
