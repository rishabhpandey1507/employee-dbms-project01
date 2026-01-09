package com.dbms.dao;

import com.dbms.model.Employee;
//import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO {
    // Database credentials
    private static final String URL = "jdbc:mysql://localhost:3306/employee_db";
    private static final String USER = "root";
    private static final String PASSWORD = "pandeyji"; 
    
    // Get database connection
    private Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL Driver not found", e);
        }
    }
    
    // Get all employees
    public List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT * FROM employees ORDER BY id DESC";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Employee emp = new Employee();
                emp.setId(rs.getInt("id"));
                emp.setName(rs.getString("name"));
                emp.setDepartment(rs.getString("department"));
                emp.setPosition(rs.getString("position"));
                emp.setSalary(rs.getBigDecimal("salary"));
                emp.setEmail(rs.getString("email"));
                emp.setPhone(rs.getString("phone"));
                emp.setHireDate(rs.getDate("hire_date"));
                employees.add(emp);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting all employees: " + e.getMessage());
            e.printStackTrace();
        }
        
        return employees;
    }
    
    // Get employee by ID
    public Employee getEmployeeById(int id) {
        String sql = "SELECT * FROM employees WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Employee emp = new Employee();
                emp.setId(rs.getInt("id"));
                emp.setName(rs.getString("name"));
                emp.setDepartment(rs.getString("department"));
                emp.setPosition(rs.getString("position"));
                emp.setSalary(rs.getBigDecimal("salary"));
                emp.setEmail(rs.getString("email"));
                emp.setPhone(rs.getString("phone"));
                emp.setHireDate(rs.getDate("hire_date"));
                return emp;
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting employee by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    // Add new employee
    public boolean addEmployee(Employee employee) {
        String sql = "INSERT INTO employees (name, department, position, salary, email, phone, hire_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, employee.getName());
            pstmt.setString(2, employee.getDepartment());
            pstmt.setString(3, employee.getPosition());
            pstmt.setBigDecimal(4, employee.getSalary());
            pstmt.setString(5, employee.getEmail());
            pstmt.setString(6, employee.getPhone());
            pstmt.setDate(7, employee.getHireDate());
            
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Insert rows affected: " + rowsAffected);
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error adding employee: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Update employee - FIXED VERSION
    public boolean updateEmployee(Employee employee) {
        String sql = "UPDATE employees SET name = ?, department = ?, position = ?, " +
                     "salary = ?, email = ?, phone = ?, hire_date = ? WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            System.out.println("Updating employee ID: " + employee.getId());
            System.out.println("New name: " + employee.getName());
            System.out.println("New salary: " + employee.getSalary());
            
            pstmt.setString(1, employee.getName());
            pstmt.setString(2, employee.getDepartment());
            pstmt.setString(3, employee.getPosition());
            pstmt.setBigDecimal(4, employee.getSalary());
            pstmt.setString(5, employee.getEmail());
            pstmt.setString(6, employee.getPhone());
            pstmt.setDate(7, employee.getHireDate());
            pstmt.setInt(8, employee.getId());
            
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Update rows affected: " + rowsAffected);
            
            if (rowsAffected > 0) {
                System.out.println("Employee updated successfully!");
                return true;
            } else {
                System.err.println("No rows updated - employee ID " + employee.getId() + " might not exist");
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("SQL Error updating employee: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Delete employee
    public boolean deleteEmployee(int id) {
        String sql = "DELETE FROM employees WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Delete rows affected: " + rowsAffected + " for ID: " + id);
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting employee: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
