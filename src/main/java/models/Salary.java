package models;

import java.math.BigDecimal;
import java.sql.Date;

public class Salary {
    private int salaryId;
    private int employeeId;
    private int month;
    private int year;
    private int workingDays;
    private BigDecimal totalSalary;
    private Date payDate;
    private String status; // "pending", "paid", "cancelled"
    private String notes;
    
    // Default constructor
    public Salary() {
    }
    
    // Constructor for new salary records (without ID)
    public Salary(int employeeId, int month, int year, int workingDays, 
                 BigDecimal totalSalary, Date payDate, String status, String notes) {
        this.employeeId = employeeId;
        this.month = month;
        this.year = year;
        this.workingDays = workingDays;
        this.totalSalary = totalSalary;
        this.payDate = payDate;
        this.status = status;
        this.notes = notes;
    }
    
    // Constructor with all fields
    public Salary(int salaryId, int employeeId, int month, int year, 
                 int workingDays, BigDecimal totalSalary, Date payDate, String status, String notes) {
        this.salaryId = salaryId;
        this.employeeId = employeeId;
        this.month = month;
        this.year = year;
        this.workingDays = workingDays;
        this.totalSalary = totalSalary;
        this.payDate = payDate;
        this.status = status;
        this.notes = notes;
    }
    
    // Getters and Setters
    public int getSalaryId() {
        return salaryId;
    }
    
    public void setSalaryId(int salaryId) {
        this.salaryId = salaryId;
    }
    
    public int getEmployeeId() {
        return employeeId;
    }
    
    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }
    
    public int getMonth() {
        return month;
    }
    
    public void setMonth(int month) {
        this.month = month;
    }
    
    public int getYear() {
        return year;
    }
    
    public void setYear(int year) {
        this.year = year;
    }
    
    public int getWorkingDays() {
        return workingDays;
    }
    
    public void setWorkingDays(int workingDays) {
        this.workingDays = workingDays;
    }
    
    public BigDecimal getTotalSalary() {
        return totalSalary;
    }
    
    public void setTotalSalary(BigDecimal totalSalary) {
        this.totalSalary = totalSalary;
    }
    
    public Date getPayDate() {
        return payDate;
    }
    
    public void setPayDate(Date payDate) {
        this.payDate = payDate;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
} 