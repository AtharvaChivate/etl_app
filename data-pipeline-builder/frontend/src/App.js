import React, { useState, useCallback, useEffect } from 'react';
import ReactFlow, {
  ReactFlowProvider,
  addEdge,
  useNodesState,
  useEdgesState,
  Controls,
  Background,
} from 'reactflow';
import 'reactflow/dist/style.css';

import Sidebar from './components/Sidebar/Sidebar';
import ConfigPanel from './components/ConfigPanel/ConfigPanel';
import DagManager from './components/DagManager/DagManager';
import { nodeTypes } from './components/Nodes/nodeTypes';
import { edgeTypes } from './components/Edges/edgeTypes';
import { serializePipeline } from './utils/pipelineSerializer';
import pipelineService from './services/pipelineService';
import './App.css';

const initialNodes = [];
const initialEdges = [];

function App() {
  const [nodes, setNodes, onNodesChange] = useNodesState(initialNodes);
  const [edges, setEdges, onEdgesChange] = useEdgesState(initialEdges);
  const [selectedNode, setSelectedNode] = useState(null);
  const [isExecuting, setIsExecuting] = useState(false);
  const [executionResult, setExecutionResult] = useState(null);
  const [showDagManager, setShowDagManager] = useState(false);

  const onConnect = useCallback(
    (params) => setEdges((eds) => addEdge({ ...params, type: 'simple' }, eds)),
    [setEdges]
  );

  const onNodeClick = useCallback((event, node) => {
    setSelectedNode(node);
  }, []);

  const onPaneClick = useCallback(() => {
    setSelectedNode(null);
  }, []);

  // Function to add a new node
  const addNode = useCallback((type, position) => {
    const newNode = {
      id: `${type}-${Date.now()}`,
      type,
      position,
      data: {
        label: type.charAt(0).toUpperCase() + type.slice(1),
        onUpdate: updateNodeData,
      },
    };
    setNodes((nds) => [...nds, newNode]);
  }, [setNodes]);

  // Function to update node data
  const updateNodeData = useCallback((nodeId, newData) => {
    console.log('updateNodeData called:', { nodeId, newData });
    setNodes((nds) =>
      nds.map((node) => {
        if (node.id === nodeId) {
          const updatedNode = {
            ...node,
            data: { 
              ...node.data, 
              ...newData
            },
          };
          console.log('Updated node:', updatedNode);
          return updatedNode;
        }
        return node;
      })
    );
  }, [setNodes]);

  // Helper function to ensure nodes have the onUpdate function
  const ensureNodeUpdate = useCallback((node) => ({
    ...node,
    data: {
      ...node.data,
      onUpdate: node.data.onUpdate || updateNodeData
    }
  }), [updateNodeData]);

  // Function to delete a node
  const deleteNode = useCallback((nodeId) => {
    setNodes((nds) => nds.filter((node) => node.id !== nodeId));
    setEdges((eds) => eds.filter((edge) => edge.source !== nodeId && edge.target !== nodeId));
    setSelectedNode(null);
  }, [setNodes, setEdges]);

  // Function to execute pipeline
  const executePipeline = async () => {
    if (nodes.length === 0) {
      alert('Please add some nodes to the pipeline first!');
      return;
    }

    setIsExecuting(true);
    setExecutionResult(null);

    try {
      const pipeline = serializePipeline(nodes, edges);
      console.log('Sending pipeline:', pipeline);
      
      const result = await pipelineService.executePipeline(pipeline);
      setExecutionResult(result);
      alert(`Pipeline executed successfully! ${result.message}`);
    } catch (error) {
      console.error('Pipeline execution error:', error);
      alert(`Error executing pipeline: ${error.message}`);
    } finally {
      setIsExecuting(false);
    }
  };

  // Function to handle DAG loading
  const handleLoadDag = (dagData) => {
    try {
      // Add onUpdate function to each node when loading
      const nodesWithUpdate = (dagData.nodes || []).map(ensureNodeUpdate);
      
      // Ensure edges have the deletable type
      const edgesWithType = (dagData.edges || []).map(edge => ({
        ...edge,
        type: edge.type || 'simple'
      }));
      
      // Replace current pipeline with loaded DAG
      setNodes(nodesWithUpdate);
      setEdges(edgesWithType);
      setSelectedNode(null);
      setShowDagManager(false);
      alert(`Successfully loaded DAG: ${dagData.name}`);
    } catch (error) {
      console.error('Error loading DAG:', error);
      alert('Failed to load DAG');
    }
  };

  // Function to clear current pipeline
  const clearPipeline = () => {
    if (nodes.length > 0 || edges.length > 0) {
      if (window.confirm('Are you sure you want to clear the current pipeline?')) {
        setNodes([]);
        setEdges([]);
        setSelectedNode(null);
        setExecutionResult(null);
      }
    }
  };

  // Keyboard event listener for deleting selected edges
  useEffect(() => {
    const handleKeyDown = (event) => {
      if (event.key === 'Delete' || event.key === 'Backspace') {
        // Find selected edges
        const selectedEdges = edges.filter(edge => edge.selected);
        if (selectedEdges.length > 0) {
          event.preventDefault();
          setEdges((eds) => eds.filter(edge => !edge.selected));
        }
      }
    };

    document.addEventListener('keydown', handleKeyDown);
    return () => {
      document.removeEventListener('keydown', handleKeyDown);
    };
  }, [edges, setEdges]);

  return (
    <div className="app">
      <div className="app-header">
        <h1>🔧 Data Pipeline Builder</h1>
        <div className="header-controls">
          <button 
            onClick={() => setShowDagManager(true)}
            className="dag-manager-btn"
            title="Save, Load, or Upload DAGs"
          >
            📁 Manage DAGs
          </button>
          <button 
            onClick={clearPipeline}
            className="clear-btn"
            disabled={nodes.length === 0 && edges.length === 0}
            title="Clear current pipeline"
          >
            🗑️ Clear
          </button>
          <button 
            onClick={executePipeline} 
            disabled={isExecuting || nodes.length === 0}
            className={`execute-btn ${isExecuting ? 'executing' : ''}`}
          >
            {isExecuting ? '⏳ Executing...' : '▶️ Execute Pipeline'}
          </button>
        </div>
      </div>
      
      <div className="app-content">
        <ReactFlowProvider>
          <Sidebar addNode={addNode} />
          
          <div className="canvas-container">
            <ReactFlow
              nodes={nodes}
              edges={edges}
              onNodesChange={onNodesChange}
              onEdgesChange={onEdgesChange}
              onConnect={onConnect}
              onNodeClick={onNodeClick}
              onPaneClick={onPaneClick}
              nodeTypes={nodeTypes}
              edgeTypes={edgeTypes}
              fitView
            >
              <Controls />
              <Background color="#aaa" gap={16} />
            </ReactFlow>
          </div>
          
          <ConfigPanel
            selectedNode={selectedNode}
            updateNodeData={updateNodeData}
            deleteNode={deleteNode}
          />
        </ReactFlowProvider>
      </div>
      
      {executionResult && (
        <div className="execution-result">
          <div className="execution-result-header">
            <h3>Execution Result:</h3>
            <button 
              className="close-result-btn"
              onClick={() => setExecutionResult(null)}
              title="Close execution result"
            >
              ×
            </button>
          </div>
          <pre>{JSON.stringify(executionResult, null, 2)}</pre>
        </div>
      )}

      {showDagManager && (
        <DagManager
          nodes={nodes}
          edges={edges}
          onLoadDag={handleLoadDag}
          onClose={() => setShowDagManager(false)}
        />
      )}
    </div>
  );
}

export default App;
