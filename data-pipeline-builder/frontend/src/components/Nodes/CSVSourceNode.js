import React, { useCallback, useState } from 'react';
import { Handle, Position } from 'reactflow';
import { useDropzone } from 'react-dropzone';
import FileUpload from '../FileUpload/FileUpload';
import './NodeStyles.css';

const CSVSourceNode = ({ data, id }) => {
  const [showUpload, setShowUpload] = useState(false);

  const onDrop = useCallback((acceptedFiles) => {
    const file = acceptedFiles[0];
    if (file) {
      // Upload file to backend
      uploadFileToBackend(file);
    }
  }, [data, id]);

  const uploadFileToBackend = async (file) => {
    const formData = new FormData();
    formData.append('file', file);

    try {
      const response = await fetch('http://localhost:8080/api/files/upload/csv', {
        method: 'POST',
        body: formData,
      });

      if (response.ok) {
        const result = await response.json();
        data.onUpdate(id, { 
          fileName: result.originalName,
          filePath: result.filePath,
          fileSize: result.size,
          configured: true
        });
      } else {
        console.error('Upload failed');
        alert('File upload failed');
      }
    } catch (error) {
      console.error('Upload error:', error);
      alert('File upload error: ' + error.message);
    }
  };

  const handleFileUploaded = (uploadResult) => {
    data.onUpdate(id, { 
      fileName: uploadResult.originalName,
      filePath: uploadResult.filePath,
      fileSize: uploadResult.size,
      configured: true
    });
  };

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: {
      'text/csv': ['.csv'],
      'application/vnd.ms-excel': ['.csv']
    },
    multiple: false
  });

  const handleFilePathChange = (e) => {
    const filePath = e.target.value;
    data.onUpdate(id, { 
      filePath: filePath,
      fileName: filePath.split('/').pop() || filePath,
      configured: !!filePath
    });
  };

  return (
    <div className="transformation-node csv-source-node">
      <div className="node-header">
        📄 CSV Source
      </div>
      <div className="node-content">
        <div className="form-group">
          <label>File Input Method:</label>
          
          {/* Upload Button */}
          <button 
            className="upload-btn"
            onClick={() => setShowUpload(true)}
            type="button"
          >
            📁 Upload CSV File
          </button>
          
          {/* Drag and Drop Area */}
          <div 
            {...getRootProps()} 
            className={`dropzone ${isDragActive ? 'active' : ''}`}
          >
            <input {...getInputProps()} />
            {data.fileName ? (
              <div className="file-info">
                📄 {data.fileName}
                {data.fileSize && (
                  <div style={{ fontSize: '0.75rem', opacity: 0.8 }}>
                    Size: {(data.fileSize / 1024).toFixed(1)} KB
                  </div>
                )}
              </div>
            ) : (
              <p>
                {isDragActive 
                  ? "Drop the CSV file here..." 
                  : "Drag & drop CSV file here"
                }
              </p>
            )}
          </div>
        </div>
        
        <div className="form-group">
          <label>Or enter file path:</label>
          <input
            type="text"
            value={data.filePath || ''}
            onChange={handleFilePathChange}
            placeholder="sample-data/employees.csv"
          />
          <small style={{ color: '#7f8c8d', fontSize: '0.75rem', display: 'block', marginTop: '0.25rem' }}>
            For sample data, use: sample-data/employees.csv
          </small>
        </div>
        
        {data.configured && (
          <div className="node-status status-success">
            ✅ CSV source configured
          </div>
        )}
      </div>
      
      {/* Output handle - data flows out from here */}
      <Handle 
        type="source" 
        position={Position.Right} 
        style={{ background: '#27ae60' }}
      />
      
      {/* File Upload Modal */}
      {showUpload && (
        <FileUpload 
          onFileUploaded={handleFileUploaded}
          onClose={() => setShowUpload(false)}
        />
      )}
    </div>
  );
};

export default CSVSourceNode;
