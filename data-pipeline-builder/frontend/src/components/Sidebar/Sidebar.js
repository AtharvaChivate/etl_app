import React from 'react';
import './Sidebar.css';

const Sidebar = ({ addNode }) => {
  const nodeTypes = [
    // Data Sources
    { type: 'csvSource', label: '📄 CSV Source', icon: '📊', description: 'Import data from CSV file', category: 'sources' },
    { type: 'sqlSource', label: '🗄️ SQL Source', icon: '🗄️', description: 'Import data from SQL database', category: 'sources' },
    
    // Transformations
    { type: 'filter', label: '🔍 Filter', icon: '⚡', description: 'Filter rows based on conditions', category: 'transforms' },
    { type: 'map', label: '🔄 Map', icon: '🔧', description: 'Transform columns', category: 'transforms' },
    { type: 'groupBy', label: '📊 Group By', icon: '📈', description: 'Group and aggregate data', category: 'transforms' },
    { type: 'join', label: '🔗 Join', icon: '🤝', description: 'Join with another dataset', category: 'transforms' },
    { type: 'sort', label: '📋 Sort', icon: '⬆️', description: 'Sort data by columns', category: 'transforms' },
    
    // Data Outputs
    { type: 'csvOutput', label: '📊 CSV Output', icon: '📊', description: 'Export data to CSV file', category: 'outputs' },
    { type: 'sqlOutput', label: '💾 SQL Output', icon: '🗃️', description: 'Save to database table', category: 'outputs' },
  ];

  const handleDragStart = (event, nodeType) => {
    event.dataTransfer.setData('application/reactflow', nodeType);
    event.dataTransfer.effectAllowed = 'move';
  };

  const handleClick = (nodeType) => {
    // Add node at center of canvas
    const position = { x: 250, y: 250 };
    addNode(nodeType, position);
  };

  const groupedNodes = {
    sources: nodeTypes.filter(n => n.category === 'sources'),
    transforms: nodeTypes.filter(n => n.category === 'transforms'),
    outputs: nodeTypes.filter(n => n.category === 'outputs')
  };

  const renderNodeGroup = (title, nodes) => (
    <div className="node-group">
      <h4 className="group-title">{title}</h4>
      {nodes.map((nodeType) => (
        <div
          key={nodeType.type}
          className="palette-node"
          draggable
          onDragStart={(event) => handleDragStart(event, nodeType.type)}
          onClick={() => handleClick(nodeType.type)}
          title={nodeType.description}
        >
          <div className="node-icon">{nodeType.icon}</div>
          <div className="node-info">
            <div className="node-label">{nodeType.label}</div>
            <div className="node-description">{nodeType.description}</div>
          </div>
        </div>
      ))}
    </div>
  );

  return (
    <div className="sidebar">
      <div className="sidebar-header">
        <h3>🧰 Data Pipeline Builder</h3>
        <p>Drag or click to add nodes</p>
      </div>
      
      <div className="node-palette">
        {renderNodeGroup('📥 Data Sources', groupedNodes.sources)}
        {renderNodeGroup('🔄 Transformations', groupedNodes.transforms)}
        {renderNodeGroup('📤 Data Outputs', groupedNodes.outputs)}
      </div>
      
      <div className="sidebar-footer">
        <div className="help-section">
          <h4>💡 How to use:</h4>
          <ol>
            <li>Start with a <strong>Data Source</strong></li>
            <li>Add transformation nodes</li>
            <li>Connect nodes with arrows</li>
            <li>End with <strong>Data Output</strong></li>
            <li>Click Execute to run!</li>
          </ol>
        </div>
      </div>
    </div>
  );
};

export default Sidebar;
