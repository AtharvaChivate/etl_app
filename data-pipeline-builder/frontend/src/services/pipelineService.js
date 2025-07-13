import axios from 'axios';

// Base URL for the backend API
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

// Create axios instance with default config
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000, // 30 seconds timeout
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add response interceptor for error handling
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('API Error:', error);
    
    if (error.response) {
      // Server responded with error status
      const message = error.response.data?.message || error.response.data?.error || 'Server error occurred';
      throw new Error(`${error.response.status}: ${message}`);
    } else if (error.request) {
      // Request was made but no response received
      throw new Error('No response from server. Please check if the backend is running.');
    } else {
      // Something else happened
      throw new Error(error.message || 'An unexpected error occurred');
    }
  }
);

const pipelineService = {
  // Execute a pipeline
  async executePipeline(pipeline) {
    try {
      console.log('Executing pipeline:', pipeline);
      const response = await apiClient.post('/pipeline/execute', pipeline);
      return response.data;
    } catch (error) {
      console.error('Pipeline execution failed:', error);
      throw error;
    }
  },

  // Validate a pipeline
  async validatePipeline(pipeline) {
    try {
      const response = await apiClient.post('/pipeline/validate', pipeline);
      return response.data;
    } catch (error) {
      console.error('Pipeline validation failed:', error);
      throw error;
    }
  },

  // Get pipeline execution status
  async getExecutionStatus(executionId) {
    try {
      const response = await apiClient.get(`/pipeline/status/${executionId}`);
      return response.data;
    } catch (error) {
      console.error('Failed to get execution status:', error);
      throw error;
    }
  },

  // Save pipeline configuration
  async savePipeline(pipelineData) {
    try {
      const response = await apiClient.post('/pipeline/save', pipelineData);
      return response.data;
    } catch (error) {
      console.error('Failed to save pipeline:', error);
      throw error;
    }
  },

  // Load saved pipeline
  async loadPipeline(pipelineId) {
    try {
      const response = await apiClient.get(`/pipeline/load/${pipelineId}`);
      return response.data;
    } catch (error) {
      console.error('Failed to load pipeline:', error);
      throw error;
    }
  },

  // Get list of saved pipelines
  async getPipelineList() {
    try {
      const response = await apiClient.get('/pipeline/list');
      return response.data;
    } catch (error) {
      console.error('Failed to get pipeline list:', error);
      throw error;
    }
  },

  // Upload CSV file
  async uploadFile(file) {
    try {
      const formData = new FormData();
      formData.append('file', file);
      
      const response = await apiClient.post('/files/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });
      
      return response.data;
    } catch (error) {
      console.error('File upload failed:', error);
      throw error;
    }
  },

  // Get file preview
  async getFilePreview(filePath, rows = 10) {
    try {
      const response = await apiClient.get(`/files/preview`, {
        params: { filePath, rows }
      });
      return response.data;
    } catch (error) {
      console.error('Failed to get file preview:', error);
      throw error;
    }
  },

  // Test database connection
  async testDatabaseConnection(connectionConfig) {
    try {
      const response = await apiClient.post('/database/test-connection', connectionConfig);
      return response.data;
    } catch (error) {
      console.error('Database connection test failed:', error);
      throw error;
    }
  },

  // Get available databases
  async getDatabases() {
    try {
      const response = await apiClient.get('/database/list');
      return response.data;
    } catch (error) {
      console.error('Failed to get databases:', error);
      throw error;
    }
  },

  // Health check
  async healthCheck() {
    try {
      const response = await apiClient.get('/health');
      return response.data;
    } catch (error) {
      console.error('Health check failed:', error);
      throw error;
    }
  }
};

export default pipelineService;
