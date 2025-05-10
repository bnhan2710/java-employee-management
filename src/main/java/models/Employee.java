package models;

import java.sql.Date;

public class Employee {
    private int employeeId;
    private String fullName;
    private String phoneNumber;
    private String email;
    private String position;
    private Date hireDate;
    private double basicSalary;
    
    public Employee() {
    }
    
    public Employee(String fullName, String phoneNumber, String email, String position, Date hireDate) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.position = position;
        this.hireDate = hireDate;
    }
    
    public Employee(String fullName, String phoneNumber, String email, String position, Date hireDate, double basicSalary) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.position = position;
        this.hireDate = hireDate;
        this.basicSalary = basicSalary;
    }
    
    public Employee(int employeeId, String fullName, String phoneNumber, String email, String position, Date hireDate) {
        this.employeeId = employeeId;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.position = position;
        this.hireDate = hireDate;
    }
    
    public Employee(int employeeId, String fullName, String phoneNumber, String email, String position, Date hireDate, double basicSalary) {
        this.employeeId = employeeId;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.position = position;
        this.hireDate = hireDate;
        this.basicSalary = basicSalary;
    }
    
    public int getEmployeeId() {
        return employeeId;
    }
    
    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPosition() {
        return position;
    }
    
    public void setPosition(String position) {
        this.position = position;
    }
    
    public Date getHireDate() {
        return hireDate;
    }
    
    public void setHireDate(Date hireDate) {
        this.hireDate = hireDate;
    }
    
    public double getBasicSalary() {
        return basicSalary;
    }
    
    public void setBasicSalary(double basicSalary) {
        this.basicSalary = basicSalary;
    }
    
    public double calculateSalary(int attendanceDays, int daysInMonth) {
        double dailySalary = basicSalary / daysInMonth;
        return dailySalary * attendanceDays;
    }
    
    @Override
    public String toString() {
        return fullName;
    }
} 