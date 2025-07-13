import React from 'react';
import { Handle, Position } from 'reactflow';

const CsvOutputNode = ({ data, selected }) => {
  return (
    <div className={`node csv-output-node ${selected ? 'selected' : ''}`}>
      <div className="node-header">
        <span className="node-icon">📊</span>
        <span className="node-title">CSV Output</span>
      </div>
      <div className="node-content">
        <div className="node-info">
          <strong>File:</strong> {data.filePath || 'output/result.csv'}
        </div>
        <div className="node-info">
          <strong>Delimiter:</strong> {data.delimiter || ','}
        </div>
        <div className="node-info">
          <strong>Headers:</strong> {data.includeHeaders ? 'Yes' : 'No'}
        </div>
      </div>
      <Handle
        type="target"
        position={Position.Left}
        className="node-handle"
      />
    </div>
  );
};

export default CsvOutputNode;
