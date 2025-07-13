import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class CsvTest {
    public static void main(String[] args) {
        try {
            String filePath = "sample-data/employees.csv";
            
            // Test path resolution logic
            String resolvedPath = filePath;
            if (filePath.startsWith("sample-data/")) {
                resolvedPath = "../" + filePath;
            }
            
            System.out.println("Original path: " + filePath);
            System.out.println("Resolved path: " + resolvedPath);
            System.out.println("File exists: " + Files.exists(Paths.get(resolvedPath)));
            
            // Test CSV reading
            List<Map<String, Object>> data = readCsvFile(resolvedPath);
            System.out.println("Records read: " + data.size());
            
            if (!data.isEmpty()) {
                System.out.println("First record: " + data.get(0));
                
                // Test filtering
                int highSalaryCount = 0;
                for (Map<String, Object> row : data) {
                    String salaryStr = (String) row.get("salary");
                    if (salaryStr != null) {
                        try {
                            double salary = Double.parseDouble(salaryStr);
                            if (salary > 70000) {
                                highSalaryCount++;
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid salary format: " + salaryStr);
                        }
                    }
                }
                System.out.println("Records with salary > 70000: " + highSalaryCount);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static List<Map<String, Object>> readCsvFile(String filePath) throws IOException {
        List<Map<String, Object>> data = new ArrayList<>();
        
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                System.out.println("CSV file is empty: " + filePath);
                return data;
            }
            
            String[] headers = parseCsvLine(headerLine);
            System.out.println("CSV headers: " + Arrays.toString(headers));
            String line;
            int lineNumber = 1;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) {
                    continue; // Skip empty lines
                }
                
                String[] values = parseCsvLine(line);
                Map<String, Object> row = new HashMap<>();
                
                for (int i = 0; i < headers.length && i < values.length; i++) {
                    row.put(headers[i].trim(), values[i].trim());
                }
                
                data.add(row);
            }
            
            System.out.println("Successfully read " + data.size() + " records from CSV file: " + filePath);
        } catch (IOException e) {
            System.out.println("Error reading CSV file: " + filePath);
            throw e;
        }
        
        return data;
    }
    
    private static String[] parseCsvLine(String line) {
        // Simple CSV parser that handles basic quoted fields
        List<String> fields = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentField = new StringBuilder();
        
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            
            if (ch == '"' && (i == 0 || line.charAt(i-1) != '\\')) {
                inQuotes = !inQuotes;
            } else if (ch == ',' && !inQuotes) {
                fields.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(ch);
            }
        }
        
        fields.add(currentField.toString());
        return fields.toArray(new String[0]);
    }
}
