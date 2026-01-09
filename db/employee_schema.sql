-- Create database
CREATE DATABASE IF NOT EXISTS employee_db;

USE employee_db;

-- Create employees table
CREATE TABLE IF NOT EXISTS employees (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    department VARCHAR(50) NOT NULL,
    position VARCHAR(50) NOT NULL,
    salary DECIMAL(10, 2) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(15),
    hire_date DATE NOT NULL
);

-- Insert sample data
INSERT INTO
    employees (
        name,
        department,
        position,
        salary,
        email,
        phone,
        hire_date
    )
VALUES (
        'Jane Smith',
        'HR',
        'HR Manager',
        65000.00,
        'jane.smith@company.com',
        '1234567891',
        '2022-06-10'
    ),
    (
        'Mike Johnson',
        'Finance',
        'Accountant',
        55000.00,
        'mike.j@company.com',
        '1234567892',
        '2021-03-20'
    ),
    (
        'Sarah Williams',
        'IT',
        'Senior Developer',
        85000.00,
        'sarah.w@company.com',
        '1234567893',
        '2020-11-05'
    );