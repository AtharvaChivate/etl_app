-- Sample database setup for SQL source demo
-- This creates a sample SQLite database with employee data

CREATE TABLE IF NOT EXISTS employees (
    id INTEGER PRIMARY KEY,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    department TEXT NOT NULL,
    salary INTEGER NOT NULL,
    hire_date TEXT NOT NULL,
    status TEXT DEFAULT 'active'
);

INSERT INTO employees (first_name, last_name, email, department, salary, hire_date, status) VALUES
('John', 'Smith', 'john.smith@company.com', 'Engineering', 75000, '2022-01-15', 'active'),
('Sarah', 'Johnson', 'sarah.johnson@company.com', 'Marketing', 65000, '2021-06-10', 'active'),
('Mike', 'Davis', 'mike.davis@company.com', 'Sales', 80000, '2020-03-22', 'active'),
('Emily', 'Brown', 'emily.brown@company.com', 'Engineering', 70000, '2023-02-08', 'active'),
('David', 'Wilson', 'david.wilson@company.com', 'Finance', 85000, '2019-11-30', 'active'),
('Lisa', 'Anderson', 'lisa.anderson@company.com', 'Marketing', 62000, '2022-08-17', 'active'),
('James', 'Miller', 'james.miller@company.com', 'Sales', 78000, '2020-09-14', 'active'),
('Maria', 'Garcia', 'maria.garcia@company.com', 'Finance', 82000, '2021-04-25', 'active'),
('Robert', 'Taylor', 'robert.taylor@company.com', 'Engineering', 77000, '2022-05-12', 'active'),
('Jennifer', 'Martinez', 'jennifer.martinez@company.com', 'HR', 68000, '2021-09-30', 'active'),
('Michael', 'Brown', 'michael.brown@company.com', 'Engineering', 90000, '2019-12-01', 'active'),
('Amanda', 'Lee', 'amanda.lee@company.com', 'Marketing', 72000, '2022-03-18', 'active'),
('Christopher', 'Wilson', 'christopher.wilson@company.com', 'Sales', 83000, '2020-07-22', 'active'),
('Jessica', 'Moore', 'jessica.moore@company.com', 'Finance', 79000, '2021-11-15', 'active'),
('Daniel', 'Clark', 'daniel.clark@company.com', 'Engineering', 88000, '2020-05-08', 'active');

-- Sample queries you can use:
-- SELECT * FROM employees;
-- SELECT * FROM employees WHERE department = 'Engineering';
-- SELECT * FROM employees WHERE salary > 75000;
-- SELECT department, COUNT(*) as employee_count, AVG(salary) as avg_salary FROM employees GROUP BY department;
