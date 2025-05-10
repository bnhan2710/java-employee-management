package dao;

import database.DatabaseConnection;
import models.Employee;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO {
    
    // Create a new employee record
    public boolean addEmployee(Employee employee) {
        String sql = "INSERT INTO Employees (full_name, phone_number, email, position, hire_date, basic_salary) VALUES (?, ?, ?, ?, ?, ?)";
        
        try {
            // Use the shared connection
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) return false;
            
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            pstmt.setString(1, employee.getFullName());
            pstmt.setString(2, employee.getPhoneNumber());
            pstmt.setString(3, employee.getEmail());
            pstmt.setString(4, employee.getPosition());
            pstmt.setDate(5, employee.getHireDate());
            pstmt.setDouble(6, employee.getBasicSalary());
            
            int affectedRows = pstmt.executeUpdate();
            
            boolean success = false;
            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    employee.setEmployeeId(generatedKeys.getInt(1));
                    success = true;
                }
                generatedKeys.close();
            }
            
            pstmt.close();
            return success;
        } catch (SQLException e) {
            System.err.println("Error adding employee: " + e.getMessage());
            return false;
        }
    }
    
    // Update an existing employee record
    public boolean updateEmployee(Employee employee) {
        String sql = "UPDATE Employees SET full_name=?, phone_number=?, email=?, position=?, hire_date=?, basic_salary=? WHERE employee_id=?";
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) return false;
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            pstmt.setString(1, employee.getFullName());
            pstmt.setString(2, employee.getPhoneNumber());
            pstmt.setString(3, employee.getEmail());
            pstmt.setString(4, employee.getPosition());
            pstmt.setDate(5, employee.getHireDate());
            pstmt.setDouble(6, employee.getBasicSalary());
            pstmt.setInt(7, employee.getEmployeeId());
            
            int affectedRows = pstmt.executeUpdate();
            pstmt.close();
            
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating employee: " + e.getMessage());
            return false;
        }
    }
    
    // Delete an employee record
    public boolean deleteEmployee(int employeeId) {
        String sql = "DELETE FROM Employees WHERE employee_id=?";
        
        try {
            // Use the shared connection
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) return false;
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            pstmt.setInt(1, employeeId);
            
            int affectedRows = pstmt.executeUpdate();
            pstmt.close();
            
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting employee: " + e.getMessage());
            return false;
        }
    }
    
    // Get a single employee by ID
    public Employee getEmployeeById(int employeeId) {
        String sql = "SELECT * FROM Employees WHERE employee_id=?";
        
        try {
            // Use the shared connection
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) return null;
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            pstmt.setInt(1, employeeId);
            
            ResultSet rs = pstmt.executeQuery();
            Employee employee = null;
            
            if (rs.next()) {
                employee = extractEmployeeFromResultSet(rs);
            }
            
            rs.close();
            pstmt.close();
            
            return employee;
        } catch (SQLException e) {
            System.err.println("Error getting employee: " + e.getMessage());
            return null;
        }
    }
    
    // Get all employees
    public List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT * FROM Employees ORDER BY full_name";
        
        try {
            // Use the shared connection
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) return employees;
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                employees.add(extractEmployeeFromResultSet(rs));
            }
            
            rs.close();
            stmt.close();
            
            return employees;
        } catch (SQLException e) {
            System.err.println("Error getting all employees: " + e.getMessage());
            return employees;
        }
    }
    
    // Helper method to extract an Employee object from a ResultSet
    private Employee extractEmployeeFromResultSet(ResultSet rs) throws SQLException {
        Employee employee = new Employee();
        employee.setEmployeeId(rs.getInt("employee_id"));
        employee.setFullName(rs.getString("full_name"));
        employee.setPhoneNumber(rs.getString("phone_number"));
        employee.setEmail(rs.getString("email"));
        employee.setPosition(rs.getString("position"));
        employee.setHireDate(rs.getDate("hire_date"));
        
        try {
            employee.setBasicSalary(rs.getDouble("basic_salary"));
        } catch (SQLException ex) {
            // In case the basic_salary column doesn't exist yet in existing databases
            System.err.println("Warning: Could not retrieve basic_salary: " + ex.getMessage());
            employee.setBasicSalary(0.0);
        }
        
        return employee;
    }
}