package dao;

import database.DatabaseConnection;
import models.Employee;
import models.Salary;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalaryDAO {
    
    // Create a new salary record
    public boolean addSalary(Salary salary) {
        String sql = "INSERT INTO Salary (employee_id, month, year, working_days, total_salary, pay_date, status, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) return false;
            
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            pstmt.setInt(1, salary.getEmployeeId());
            pstmt.setInt(2, salary.getMonth());
            pstmt.setInt(3, salary.getYear());
            pstmt.setInt(4, salary.getWorkingDays());
            pstmt.setBigDecimal(5, salary.getTotalSalary());
            pstmt.setDate(6, salary.getPayDate());
            pstmt.setString(7, salary.getStatus());
            pstmt.setString(8, salary.getNotes());
            
            int affectedRows = pstmt.executeUpdate();
            
            boolean success = false;
            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    salary.setSalaryId(generatedKeys.getInt(1));
                    success = true;
                }
                generatedKeys.close();
            }
            
            pstmt.close();
            return success;
        } catch (SQLException e) {
            System.err.println("Error adding salary: " + e.getMessage());
            return false;
        }
    }
    
    public boolean updateSalary(Salary salary) {
        String sql = "UPDATE Salary SET employee_id=?, month=?, year=?, working_days=?, " +
                     "total_salary=?, pay_date=?, status=?, notes=? WHERE salary_id=?";
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) return false;
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            pstmt.setInt(1, salary.getEmployeeId());
            pstmt.setInt(2, salary.getMonth());
            pstmt.setInt(3, salary.getYear());
            pstmt.setInt(4, salary.getWorkingDays());
            pstmt.setBigDecimal(5, salary.getTotalSalary());
            pstmt.setDate(6, salary.getPayDate());
            pstmt.setString(7, salary.getStatus());
            pstmt.setString(8, salary.getNotes());
            pstmt.setInt(9, salary.getSalaryId());
            
            int affectedRows = pstmt.executeUpdate();
            pstmt.close();
            
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating salary: " + e.getMessage());
            return false;
        }
    }
    
    public boolean deleteSalary(int salaryId) {
        String sql = "DELETE FROM Salary WHERE salary_id=?";
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) return false;
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            pstmt.setInt(1, salaryId);
            
            int affectedRows = pstmt.executeUpdate();
            pstmt.close();
            
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting salary: " + e.getMessage());
            return false;
        }
    }
    
    public Salary getSalaryById(int salaryId) {
        String sql = "SELECT * FROM Salary WHERE salary_id=?";
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) return null;
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            pstmt.setInt(1, salaryId);
            
            ResultSet rs = pstmt.executeQuery();
            Salary salary = null;
            
            if (rs.next()) {
                salary = extractSalaryFromResultSet(rs);
            }
            
            rs.close();
            pstmt.close();
            
            return salary;
        } catch (SQLException e) {
            System.err.println("Error getting salary: " + e.getMessage());
            return null;
        }
    }
    
    public List<Salary> getSalaryByEmployeeId(int employeeId) {
        List<Salary> salaryList = new ArrayList<>();
        String sql = "SELECT * FROM Salary WHERE employee_id=? ORDER BY year DESC, month DESC";
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) return salaryList;
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            pstmt.setInt(1, employeeId);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                salaryList.add(extractSalaryFromResultSet(rs));
            }
            
            rs.close();
            pstmt.close();
            
            return salaryList;
        } catch (SQLException e) {
            System.err.println("Error getting salary for employee: " + e.getMessage());
            return salaryList;
        }
    }
    
    public List<Salary> getSalaryByMonthAndYear(int month, int year) {
        List<Salary> salaryList = new ArrayList<>();
        String sql = "SELECT * FROM Salary WHERE month=? AND year=?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, month);
            pstmt.setInt(2, year);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    salaryList.add(extractSalaryFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting salary by month and year: " + e.getMessage());
        }
        
        return salaryList;
    }
    
    public List<Salary> getSalaryByStatus(String status) {
        List<Salary> salaryList = new ArrayList<>();
        String sql = "SELECT * FROM Salary WHERE status=? ORDER BY year DESC, month DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    salaryList.add(extractSalaryFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting salary by status: " + e.getMessage());
        }
        
        return salaryList;
    }
    
    public boolean calculateTotalSalary(int salaryId) {
        String getSalarySQL = "SELECT s.*, e.basic_salary FROM Salary s " +
                             "JOIN Employees e ON s.employee_id = e.employee_id " +
                             "WHERE s.salary_id=?";
        String updateTotalSQL = "UPDATE Salary SET total_salary=? WHERE salary_id=?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement getStmt = conn.prepareStatement(getSalarySQL);
             PreparedStatement updateStmt = conn.prepareStatement(updateTotalSQL)) {
            
            getStmt.setInt(1, salaryId);
            
            try (ResultSet rs = getStmt.executeQuery()) {
                if (rs.next()) {
                    double employeeBasicSalary = rs.getDouble("basic_salary");
                    int workingDays = rs.getInt("working_days");
                    int month = rs.getInt("month");
                    int year = rs.getInt("year");
                    
                    // Determine days in month based on month and year
                    java.time.YearMonth yearMonth = java.time.YearMonth.of(year, month);
                    int daysInMonth = yearMonth.lengthOfMonth();
                    
                    // Calculate daily salary rate
                    double dailySalary = employeeBasicSalary / daysInMonth;
                    
                    // Calculate total salary
                    BigDecimal totalSalary = BigDecimal.valueOf(dailySalary * workingDays)
                                                      .setScale(2, BigDecimal.ROUND_HALF_UP);
                    
                    updateStmt.setBigDecimal(1, totalSalary);
                    updateStmt.setInt(2, salaryId);
                    
                    int affectedRows = updateStmt.executeUpdate();
                    return affectedRows > 0;
                }
            }
            
            return false;
        } catch (SQLException e) {
            System.err.println("Error calculating total salary: " + e.getMessage());
            return false;
        }
    }
    
    // Get all salary records for a specific employee in a month/year
    public List<Salary> getSalaryByEmployeeAndMonth(int employeeId, int month, int year) {
        List<Salary> salaryList = new ArrayList<>();
        String sql = "SELECT * FROM Salary WHERE employee_id=? AND month=? AND year=?";
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) return salaryList;
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            pstmt.setInt(1, employeeId);
            pstmt.setInt(2, month);
            pstmt.setInt(3, year);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                salaryList.add(extractSalaryFromResultSet(rs));
            }
            
            rs.close();
            pstmt.close();
            
            return salaryList;
        } catch (SQLException e) {
            System.err.println("Error getting salary for employee by month: " + e.getMessage());
            return salaryList;
        }
    }
    
    // Calculate total earnings for an employee across all months
    public BigDecimal calculateTotalEarningsByEmployee(int employeeId) {
        BigDecimal totalEarnings = BigDecimal.ZERO;
        String sql = "SELECT SUM(total_salary) as total_earnings FROM Salary WHERE employee_id=? AND status='đã thanh toán'";
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) return totalEarnings;
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, employeeId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                BigDecimal result = rs.getBigDecimal("total_earnings");
                if (result != null) {
                    totalEarnings = result;
                }
            }
            
            rs.close();
            pstmt.close();
            
            return totalEarnings;
        } catch (SQLException e) {
            System.err.println("Error calculating total earnings: " + e.getMessage());
            return totalEarnings;
        }
    }
    
    private Salary extractSalaryFromResultSet(ResultSet rs) throws SQLException {
        Salary salary = new Salary();
        salary.setSalaryId(rs.getInt("salary_id"));
        salary.setEmployeeId(rs.getInt("employee_id"));
        salary.setMonth(rs.getInt("month"));
        salary.setYear(rs.getInt("year"));
        salary.setWorkingDays(rs.getInt("working_days"));
        salary.setTotalSalary(rs.getBigDecimal("total_salary"));
        salary.setPayDate(rs.getDate("pay_date"));
        salary.setStatus(rs.getString("status"));
        salary.setNotes(rs.getString("notes"));
        
        return salary;
    }
} 