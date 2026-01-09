package com.dbms.controller;

import com.dbms.dao.EmployeeDAO;
import com.dbms.model.Employee;
import com.dbms.service.EmailService;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

@WebServlet("/api/employees/*")
public class EmployeeServlet extends HttpServlet {
    private EmployeeDAO employeeDAO;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        employeeDAO = new EmployeeDAO();
        gson = new Gson();
    }

    // GET - Retrieve employees
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String pathInfo = request.getPathInfo();
        PrintWriter out = response.getWriter();
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // Get all employees
                List<Employee> employees = employeeDAO.getAllEmployees();
                out.print(gson.toJson(employees));
            } else {
                // Get specific employee
                int id = Integer.parseInt(pathInfo.substring(1));
                Employee employee = employeeDAO.getEmployeeById(id);
                
                if (employee != null) {
                    out.print(gson.toJson(employee));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\":\"Employee not found\"}");
                }
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid employee ID\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"Internal server error\"}");
            e.printStackTrace();
        }
    }

    // POST - Add or Update employee
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            // Get all parameters
            String idStr = request.getParameter("id");
            String name = request.getParameter("name");
            String department = request.getParameter("department");
            String position = request.getParameter("position");
            String salaryStr = request.getParameter("salary");
            String email = request.getParameter("email");
            String phone = request.getParameter("phone");
            String hireDateStr = request.getParameter("hireDate");
            
            // Debug logging
            System.out.println("=================================");
            System.out.println("POST REQUEST RECEIVED");
            System.out.println("=================================");
            System.out.println("ID parameter: " + idStr);
            System.out.println("Name: " + name);
            System.out.println("Department: " + department);
            System.out.println("Position: " + position);
            System.out.println("Salary: " + salaryStr);
            System.out.println("Email: " + email);
            System.out.println("Phone: " + phone);
            System.out.println("Hire Date: " + hireDateStr);
            System.out.println("=================================");
            
            // Validate required fields
            if (name == null || name.isEmpty() || 
                department == null || department.isEmpty() || 
                position == null || position.isEmpty() || 
                salaryStr == null || salaryStr.isEmpty() || 
                email == null || email.isEmpty() || 
                phone == null || phone.isEmpty() || 
                hireDateStr == null || hireDateStr.isEmpty()) {
                
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Missing required fields\"}");
                System.err.println("ERROR: Missing required fields");
                return;
            }
            
            // Create employee object
            Employee employee = new Employee();
            employee.setName(name);
            employee.setDepartment(department);
            employee.setPosition(position);
            employee.setSalary(new BigDecimal(salaryStr));
            employee.setEmail(email);
            employee.setPhone(phone);
            employee.setHireDate(Date.valueOf(hireDateStr));
            
            boolean success;
            
            // Check if this is UPDATE (has ID) or ADD (no ID)
            if (idStr != null && !idStr.isEmpty() && !idStr.equals("null") && !idStr.equals("undefined")) {
                // UPDATE operation
                int id = Integer.parseInt(idStr);
                employee.setId(id);
                
                System.out.println(">>> UPDATING employee with ID: " + id);
                success = employeeDAO.updateEmployee(employee);
                
                if (success) {
                    System.out.println(">>> UPDATE SUCCESS for ID: " + id);
                    
                    // Send update notification email - NEW CODE
                    try {
                        boolean emailSent = EmailService.sendUpdateNotification(employee);
                        if (emailSent) {
                            System.out.println("✅ Update notification email sent to: " + email);
                        }
                    } catch (Exception e) {
                        System.err.println("⚠️ Email notification failed: " + e.getMessage());
                    }
                    
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print("{\"success\":true,\"message\":\"Employee updated successfully\"}");
                } else {
                    System.err.println(">>> UPDATE FAILED for ID: " + id);
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print("{\"success\":false,\"message\":\"Failed to update employee - no rows affected\"}");
                }
            } else {
                // ADD operation
                System.out.println(">>> ADDING new employee");
                success = employeeDAO.addEmployee(employee);
                
                if (success) {
                    System.out.println(">>> ADD SUCCESS");
                    
                    // Send welcome email - NEW CODE
                    try {
                        boolean emailSent = EmailService.sendWelcomeEmail(employee);
                        if (emailSent) {
                            System.out.println("✅ Welcome email sent to: " + email);
                        }
                    } catch (Exception e) {
                        System.err.println("⚠️ Email sending failed, but employee added: " + e.getMessage());
                    }
                    
                    response.setStatus(HttpServletResponse.SC_CREATED);
                    out.print("{\"success\":true,\"message\":\"Employee added successfully\"}");
                } else {
                    System.err.println(">>> ADD FAILED");
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print("{\"success\":false,\"message\":\"Failed to add employee\"}");
                }
            }
            
        } catch (NumberFormatException e) {
            System.err.println("NUMBER FORMAT ERROR: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid number format: " + e.getMessage() + "\"}");
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            System.err.println("ILLEGAL ARGUMENT ERROR: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid date format: " + e.getMessage() + "\"}");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("GENERAL ERROR: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"Internal server error: " + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }

    // DELETE - Delete employee
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String pathInfo = request.getPathInfo();
        PrintWriter out = response.getWriter();
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Employee ID is required\"}");
                return;
            }
            
            int id = Integer.parseInt(pathInfo.substring(1));
            System.out.println(">>> DELETING employee with ID: " + id);
            
            boolean success = employeeDAO.deleteEmployee(id);
            
            if (success) {
                System.out.println(">>> DELETE SUCCESS for ID: " + id);
                response.setStatus(HttpServletResponse.SC_OK);
                out.print("{\"success\":true,\"message\":\"Employee deleted successfully\"}");
            } else {
                System.err.println(">>> DELETE FAILED for ID: " + id);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"success\":false,\"message\":\"Employee not found\"}");
            }
            
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid employee ID\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"Internal server error\"}");
            e.printStackTrace();
        }
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
