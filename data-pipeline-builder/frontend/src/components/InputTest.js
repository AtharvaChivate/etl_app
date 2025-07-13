// Test component to verify input functionality
import React, { useState } from 'react';

const InputTest = () => {
  const [testValue, setTestValue] = useState('');

  return (
    <div style={{ padding: '20px', border: '1px solid #ccc', margin: '10px' }}>
      <h4>Input Test Component</h4>
      <label>Test Input:</label>
      <input
        type="text"
        placeholder="Type something here..."
        value={testValue}
        onChange={(e) => {
          console.log('Input changed:', e.target.value);
          setTestValue(e.target.value);
        }}
      />
      <p>Current value: "{testValue}"</p>
    </div>
  );
};

export default InputTest;
