import React, { useState, useEffect } from 'react';
import './DagManager.css';

const DagManager = ({ 
  nodes, 
  edges, 
  onLoadDag, 
  onClose 
}) => {
  const [activeTab, setActiveTab] = useState('save');
  const [savedDags, setSavedDags] = useState([]);
  const [loading, setLoading] = useState(false);
  const [dagName, setDagName] = useState('');
  const [dagDescription, setDagDescription] = useState('');
  const [uploadFile, setUploadFile] = useState(null);

  useEffect(() => {
    if (activeTab === 'load') {
      fetchSavedDags();
    }
  }, [activeTab]);

  const fetchSavedDags = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/dags/list');
      const data = await response.json();
      setSavedDags(data.dags || []);
    } catch (error) {
      console.error('Error fetching DAGs:', error);
      alert('Failed to fetch saved DAGs');
    }
  };

  const handleSaveDag = async () => {
    if (!dagName.trim()) {
      alert('Please enter a DAG name');
      return;
    }

    if (nodes.length === 0) {
      alert('Cannot save an empty DAG. Please add some nodes first.');
      return;
    }

    setLoading(true);
    
    const dagData = {
      name: dagName,
      description: dagDescription,
      nodes: nodes,
      edges: edges,
      nodeCount: nodes.length,
      edgeCount: edges.length
    };

    try {
      const response = await fetch('http://localhost:8080/api/dags/save', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(dagData),
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`HTTP ${response.status}: ${response.statusText}. ${errorText}`);
      }

      const result = await response.json();
      
      if (result.success) {
        alert(`✅ DAG saved successfully as: ${result.filename}`);
        setDagName('');
        setDagDescription('');
        onClose();
      } else {
        throw new Error(result.error || 'Unknown error occurred while saving DAG');
      }
    } catch (error) {
      console.error('Error saving DAG:', error);
      alert(`❌ Failed to save DAG: ${error.message}`);
    } finally {
      setLoading(false);
    }
  };

  const handleLoadDag = async (filename) => {
    setLoading(true);
    
    try {
      const response = await fetch(`http://localhost:8080/api/dags/load/${filename}`);
      
      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`HTTP ${response.status}: ${response.statusText}. ${errorText}`);
      }
      
      const result = await response.json();
      
      if (result.success) {
        onLoadDag(result.dag);
        alert(`✅ DAG "${result.dag.name}" loaded successfully!`);
        onClose();
      } else {
        throw new Error(result.error || 'Unknown error occurred while loading DAG');
      }
    } catch (error) {
      console.error('Error loading DAG:', error);
      alert(`❌ Failed to load DAG: ${error.message}`);
    } finally {
      setLoading(false);
    }
  };

  const handleUploadDag = async () => {
    if (!uploadFile) {
      alert('Please select a JSON file');
      return;
    }

    setLoading(true);
    
    const formData = new FormData();
    formData.append('file', uploadFile);

    try {
      const response = await fetch('http://localhost:8080/api/dags/upload', {
        method: 'POST',
        body: formData,
      });

      const result = await response.json();
      
      if (result.success) {
        onLoadDag(result.dag);
        alert('DAG uploaded and loaded successfully!');
        setUploadFile(null);
        onClose();
      } else {
        alert('Failed to upload DAG: ' + (result.error || 'Unknown error'));
      }
    } catch (error) {
      console.error('Error uploading DAG:', error);
      alert('Failed to upload DAG: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteDag = async (filename) => {
    if (!window.confirm('Are you sure you want to delete this DAG?')) {
      return;
    }

    try {
      const response = await fetch(`http://localhost:8080/api/dags/delete/${filename}`, {
        method: 'DELETE',
      });

      if (response.ok) {
        alert('DAG deleted successfully');
        fetchSavedDags(); // Refresh list
      } else {
        alert('Failed to delete DAG');
      }
    } catch (error) {
      console.error('Error deleting DAG:', error);
      alert('Failed to delete DAG: ' + error.message);
    }
  };

  const handleExportMetadata = async () => {
    if (!dagName.trim()) {
      alert('Please enter a DAG name');
      return;
    }

    if (nodes.length === 0) {
      alert('Cannot export an empty DAG. Please add some nodes first.');
      return;
    }

    setLoading(true);
    
    const dagData = {
      name: dagName,
      description: dagDescription,
      nodes: nodes,
      edges: edges,
      nodeCount: nodes.length,
      edgeCount: edges.length,
      version: "1.0"
    };

    try {
      const response = await fetch('http://localhost:8080/api/dags/export-metadata', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(dagData),
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`HTTP ${response.status}: ${response.statusText}. ${errorText}`);
      }

      const result = await response.json();
      
      if (result.success) {
        // Download the metadata as JSON file
        const metadataJson = JSON.stringify(result.metadata, null, 2);
        const blob = new Blob([metadataJson], { type: 'application/json' });
        const url = URL.createObjectURL(blob);
        
        const link = document.createElement('a');
        link.href = url;
        link.download = `${dagName}_metadata.json`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        URL.revokeObjectURL(url);
        
        alert(`✅ DAG metadata exported successfully as: ${dagName}_metadata.json`);
      } else {
        throw new Error(result.error || 'Unknown error occurred while exporting metadata');
      }
    } catch (error) {
      console.error('Error exporting DAG metadata:', error);
      alert(`❌ Failed to export DAG metadata: ${error.message}`);
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'Unknown';
    try {
      return new Date(dateString).toLocaleString();
    } catch {
      return dateString;
    }
  };

  return (
    <div className="dag-manager-overlay">
      <div className="dag-manager-modal">
        <div className="dag-manager-header">
          <h3>🗂️ DAG Manager</h3>
          <button className="close-btn" onClick={onClose}>✕</button>
        </div>
        
        <div className="dag-manager-tabs">
          <button 
            className={`tab ${activeTab === 'save' ? 'active' : ''}`}
            onClick={() => setActiveTab('save')}
          >
            💾 Save DAG
          </button>
          <button 
            className={`tab ${activeTab === 'load' ? 'active' : ''}`}
            onClick={() => setActiveTab('load')}
          >
            📂 Load DAG
          </button>
          <button 
            className={`tab ${activeTab === 'upload' ? 'active' : ''}`}
            onClick={() => setActiveTab('upload')}
          >
            📁 Upload DAG
          </button>
        </div>

        <div className="dag-manager-content">
          {activeTab === 'save' && (
            <div className="save-dag-form">
              <h4>Save Current Pipeline</h4>
              <div className="current-dag-info">
                <p><strong>Nodes:</strong> {nodes.length}</p>
                <p><strong>Connections:</strong> {edges.length}</p>
              </div>
              
              <div className="form-group">
                <label>DAG Name:</label>
                <input
                  type="text"
                  value={dagName}
                  onChange={(e) => setDagName(e.target.value)}
                  placeholder="My ETL Pipeline"
                  required
                />
              </div>
              
              <div className="form-group">
                <label>Description (Optional):</label>
                <textarea
                  value={dagDescription}
                  onChange={(e) => setDagDescription(e.target.value)}
                  placeholder="Describe what this pipeline does..."
                  rows={3}
                />
              </div>
              
              <button 
                className="save-btn"
                onClick={handleSaveDag}
                disabled={loading || !dagName.trim()}
              >
                {loading ? '💾 Saving...' : '💾 Save DAG'}
              </button>
              
              <button 
                className="export-btn"
                onClick={handleExportMetadata}
                disabled={loading || !dagName.trim()}
                style={{marginLeft: '10px', backgroundColor: '#28a745'}}
              >
                {loading ? '📤 Exporting...' : '📤 Export Metadata'}
              </button>
            </div>
          )}

          {activeTab === 'load' && (
            <div className="load-dag-list">
              <h4>Saved Pipelines</h4>
              {savedDags.length === 0 ? (
                <p className="no-dags">No saved DAGs found</p>
              ) : (
                <div className="dag-list">
                  {savedDags.map((dag, index) => (
                    <div key={index} className="dag-item">
                      <div className="dag-info">
                        <h5>{dag.name || 'Unnamed Pipeline'}</h5>
                        <p className="dag-description">{dag.description || 'No description'}</p>
                        <div className="dag-meta">
                          <span>📊 {dag.nodeCount} nodes</span>
                          <span>📅 {formatDate(dag.savedAt)}</span>
                        </div>
                      </div>
                      <div className="dag-actions">
                        <button 
                          className="load-btn"
                          onClick={() => handleLoadDag(dag.filename)}
                          disabled={loading}
                        >
                          📂 Load
                        </button>
                        <button 
                          className="delete-btn"
                          onClick={() => handleDeleteDag(dag.filename)}
                          disabled={loading}
                        >
                          🗑️
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {activeTab === 'upload' && (
            <div className="upload-dag-form">
              <h4>Upload DAG File</h4>
              <p>Upload a previously saved DAG JSON file</p>
              
              <div className="form-group">
                <label>Select JSON File:</label>
                <input
                  type="file"
                  accept=".json"
                  onChange={(e) => setUploadFile(e.target.files[0])}
                />
              </div>
              
              {uploadFile && (
                <div className="file-preview">
                  <p><strong>Selected:</strong> {uploadFile.name}</p>
                  <p><strong>Size:</strong> {(uploadFile.size / 1024).toFixed(1)} KB</p>
                </div>
              )}
              
              <button 
                className="upload-btn"
                onClick={handleUploadDag}
                disabled={loading || !uploadFile}
              >
                {loading ? '📁 Uploading...' : '📁 Upload & Load DAG'}
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default DagManager;
