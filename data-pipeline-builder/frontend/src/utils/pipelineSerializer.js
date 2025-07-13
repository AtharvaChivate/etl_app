// Pipeline serialization utility
export const serializePipeline = (nodes, edges) => {
  // Convert React Flow nodes and edges to a format the backend can understand
  const serializedNodes = nodes.map(node => ({
    id: node.id,
    type: node.type,
    position: node.position,
    data: {
      // Remove React-specific properties like onUpdate function
      ...Object.fromEntries(
        Object.entries(node.data).filter(([key]) => key !== 'onUpdate')
      )
    }
  }));

  const serializedEdges = edges.map(edge => ({
    id: edge.id,
    source: edge.source,
    target: edge.target,
    sourceHandle: edge.sourceHandle,
    targetHandle: edge.targetHandle
  }));

  // Build execution order based on graph topology
  const executionOrder = getExecutionOrder(serializedNodes, serializedEdges);

  return {
    nodes: serializedNodes,
    edges: serializedEdges,
    executionOrder,
    metadata: {
      created: new Date().toISOString(),
      nodeCount: serializedNodes.length,
      edgeCount: serializedEdges.length
    }
  };
};

// Topological sort to determine execution order
const getExecutionOrder = (nodes, edges) => {
  const graph = new Map();
  const inDegree = new Map();
  
  // Initialize graph and in-degree count
  nodes.forEach(node => {
    graph.set(node.id, []);
    inDegree.set(node.id, 0);
  });
  
  // Build adjacency list and calculate in-degrees
  edges.forEach(edge => {
    graph.get(edge.source).push(edge.target);
    inDegree.set(edge.target, inDegree.get(edge.target) + 1);
  });
  
  // Topological sort using Kahn's algorithm
  const queue = [];
  const result = [];
  
  // Find nodes with no incoming edges
  inDegree.forEach((degree, nodeId) => {
    if (degree === 0) {
      queue.push(nodeId);
    }
  });
  
  while (queue.length > 0) {
    const nodeId = queue.shift();
    result.push(nodeId);
    
    // Remove edges from this node
    graph.get(nodeId).forEach(neighbor => {
      inDegree.set(neighbor, inDegree.get(neighbor) - 1);
      if (inDegree.get(neighbor) === 0) {
        queue.push(neighbor);
      }
    });
  }
  
  // Check for cycles
  if (result.length !== nodes.length) {
    throw new Error('Pipeline contains cycles - this is not allowed');
  }
  
  return result;
};

// Validate pipeline before execution
export const validatePipeline = (nodes, edges) => {
  const errors = [];
  
  // Check if pipeline has at least one node
  if (nodes.length === 0) {
    errors.push('Pipeline must contain at least one node');
  }
  
  // Check for source nodes (CSV input)
  const sourceNodes = nodes.filter(node => node.type === 'csvSource');
  if (sourceNodes.length === 0) {
    errors.push('Pipeline must contain at least one CSV source node');
  }
  
  // Check for output nodes
  const outputNodes = nodes.filter(node => node.type === 'sqlOutput');
  if (outputNodes.length === 0) {
    errors.push('Pipeline must contain at least one SQL output node');
  }
  
  // Check if all nodes are configured
  const unconfiguredNodes = nodes.filter(node => !node.data.configured);
  if (unconfiguredNodes.length > 0) {
    errors.push(`${unconfiguredNodes.length} node(s) are not properly configured: ${unconfiguredNodes.map(n => n.id).join(', ')}`);
  }
  
  // Check for disconnected nodes (except source and sink nodes)
  const connectedNodes = new Set();
  edges.forEach(edge => {
    connectedNodes.add(edge.source);
    connectedNodes.add(edge.target);
  });
  
  const disconnectedNodes = nodes.filter(node => 
    !connectedNodes.has(node.id) && 
    node.type !== 'csvSource' && 
    node.type !== 'sqlOutput'
  );
  
  if (disconnectedNodes.length > 0) {
    errors.push(`Disconnected nodes found: ${disconnectedNodes.map(n => n.id).join(', ')}`);
  }
  
  return {
    isValid: errors.length === 0,
    errors
  };
};

// Generate sample pipeline for testing
export const generateSamplePipeline = () => {
  return {
    nodes: [
      {
        id: 'csv-source-1',
        type: 'csvSource',
        position: { x: 100, y: 100 },
        data: {
          label: 'Sample CSV',
          filePath: '/sample-data/sales.csv',
          fileName: 'sales.csv',
          configured: true
        }
      },
      {
        id: 'filter-1',
        type: 'filter',
        position: { x: 400, y: 100 },
        data: {
          label: 'Filter Sales',
          column: 'amount',
          operator: '>',
          value: '100',
          configured: true
        }
      },
      {
        id: 'sql-output-1',
        type: 'sqlOutput',
        position: { x: 700, y: 100 },
        data: {
          label: 'Save to DB',
          tableName: 'filtered_sales',
          databaseType: 'sqlite',
          writeMode: 'replace',
          configured: true
        }
      }
    ],
    edges: [
      {
        id: 'csv-source-1-filter-1',
        source: 'csv-source-1',
        target: 'filter-1'
      },
      {
        id: 'filter-1-sql-output-1',
        source: 'filter-1',
        target: 'sql-output-1'
      }
    ]
  };
};
