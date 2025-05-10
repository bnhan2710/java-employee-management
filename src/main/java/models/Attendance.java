package models;

import java.sql.Date;

public class Attendance {
    private int attendanceId;
    private int employeeId;
    private Date date;
    private String status; // "present", "absent", "late"
    private String notes;
    
    // Default constructor
    public Attendance() {
    }
    
    // Constructor for new attendance records (without ID)
    public Attendance(int employeeId, Date date, String status, String notes) {
        this.employeeId = employeeId;
        this.date = date;
        this.status = status;
        this.notes = notes;
    }
    
    // Constructor with all fields
    public Attendance(int attendanceId, int employeeId, Date date, String status, String notes) {
        this.attendanceId = attendanceId;
        this.employeeId = employeeId;
        this.date = date;
        this.status = status;
        this.notes = notes;
    }
    
    // Getters and Setters
    public int getAttendanceId() {
        return attendanceId;
    }
    
    public void setAttendanceId(int attendanceId) {
        this.attendanceId = attendanceId;
    }
    
    public int getEmployeeId() {
        return employeeId;
    }
    
    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }
    
    public Date getDate() {
        return date;
    }
    
    public void setDate(Date date) {
        this.date = date;
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