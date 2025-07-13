import React, { useState } from 'react';
import './FileUpload.css';

const FileUpload = ({ onFileUploaded, onClose }) => {
  const [dragActive, setDragActive] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);

  const handleDrag = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === 'dragenter' || e.type === 'dragover') {
      setDragActive(true);
    } else if (e.type === 'dragleave') {
      setDragActive(false);
    }
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);

    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      handleFileUpload(e.dataTransfer.files[0]);
    }
  };

  const handleFileSelect = (e) => {
    if (e.target.files && e.target.files[0]) {
      handleFileUpload(e.target.files[0]);
    }
  };

  const handleFileUpload = async (file) => {
    if (!file.name.toLowerCase().endsWith('.csv')) {
      alert('Please select a CSV file');
      return;
    }

    setUploading(true);
    setUploadProgress(0);

    const formData = new FormData();
    formData.append('file', file);

    try {
      const xhr = new XMLHttpRequest();
      
      xhr.upload.addEventListener('progress', (e) => {
        if (e.lengthComputable) {
          const progress = (e.loaded / e.total) * 100;
          setUploadProgress(progress);
        }
      });

      xhr.addEventListener('load', () => {
        if (xhr.status === 200) {
          const response = JSON.parse(xhr.responseText);
          console.log('File uploaded successfully:', response);
          
          if (onFileUploaded) {
            onFileUploaded(response);
          }
          
          setUploading(false);
          setUploadProgress(0);
          onClose();
        } else {
          throw new Error('Upload failed');
        }
      });

      xhr.addEventListener('error', () => {
        throw new Error('Upload failed');
      });

      xhr.open('POST', 'http://localhost:8080/api/files/upload/csv');
      xhr.send(formData);

    } catch (error) {
      console.error('Upload error:', error);
      alert('Upload failed: ' + error.message);
      setUploading(false);
      setUploadProgress(0);
    }
  };

  return (
    <div className="file-upload-overlay">
      <div className="file-upload-modal">
        <div className="upload-header">
          <h3>📁 Upload CSV File</h3>
          <button className="close-btn" onClick={onClose}>✕</button>
        </div>
        
        <div
          className={`upload-area ${dragActive ? 'drag-active' : ''} ${uploading ? 'uploading' : ''}`}
          onDragEnter={handleDrag}
          onDragLeave={handleDrag}
          onDragOver={handleDrag}
          onDrop={handleDrop}
        >
          {uploading ? (
            <div className="upload-progress">
              <div className="progress-circle">
                <div className="progress-text">{Math.round(uploadProgress)}%</div>
              </div>
              <p>Uploading...</p>
            </div>
          ) : (
            <>
              <div className="upload-icon">📁</div>
              <p className="upload-text">
                <strong>Drag and drop your CSV file here</strong><br />
                or click to browse
              </p>
              <input
                type="file"
                accept=".csv"
                onChange={handleFileSelect}
                style={{ display: 'none' }}
                id="file-input"
              />
              <label htmlFor="file-input" className="browse-btn">
                Browse Files
              </label>
            </>
          )}
        </div>
        
        <div className="upload-info">
          <h4>📋 Requirements:</h4>
          <ul>
            <li>File must be in CSV format (.csv)</li>
            <li>First row should contain column headers</li>
            <li>Maximum file size: 50MB</li>
            <li>Supported encodings: UTF-8, ASCII</li>
          </ul>
        </div>
      </div>
    </div>
  );
};

export default FileUpload;
