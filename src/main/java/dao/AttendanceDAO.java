package dao;

import database.DatabaseConnection;
import models.Attendance;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDAO {
    
    // Create a new attendance record
    public boolean addAttendance(Attendance attendance) {
        String sql = "INSERT INTO Attendance (employee_id, date, status, notes) VALUES (?, ?, ?, ?)";
        
        try {
            // Use the shared connection
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) return false;
            
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            pstmt.setInt(1, attendance.getEmployeeId());
            pstmt.setDate(2, attendance.getDate());
            pstmt.setString(3, attendance.getStatus());
            pstmt.setString(4, attendance.getNotes());
            
            int affectedRows = pstmt.executeUpdate();
            
            boolean success = false;
            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    attendance.setAttendanceId(generatedKeys.getInt(1));
                    success = true;
                }
                generatedKeys.close();
            }
            
            pstmt.close();
            return success;
        } catch (SQLException e) {
            System.err.println("Error adding attendance: " + e.getMessage());
            return false;
        }
    }
    
    // Update an existing attendance record
    public boolean updateAttendance(Attendance attendance) {
        String sql = "UPDATE Attendance SET employee_id=?, date=?, status=?, notes=? WHERE attendance_id=?";
        
        try {
            // Use the shared connection
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) return false;
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            pstmt.setInt(1, attendance.getEmployeeId());
            pstmt.setDate(2, attendance.getDate());
            pstmt.setString(3, attendance.getStatus());
            pstmt.setString(4, attendance.getNotes());
            pstmt.setInt(5, attendance.getAttendanceId());
            
            int affectedRows = pstmt.executeUpdate();
            pstmt.close();
            
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating attendance: " + e.getMessage());
            return false;
        }
    }
    
    // Delete an attendance record
    public boolean deleteAttendance(int attendanceId) {
        String sql = "DELETE FROM Attendance WHERE attendance_id=?";
        
        try {
            // Use the shared connection
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) return false;
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            pstmt.setInt(1, attendanceId);
            
            int affectedRows = pstmt.executeUpdate();
            pstmt.close();
            
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting attendance: " + e.getMessage());
            return false;
        }
    }
    
    // Get a single attendance record by ID
    public Attendance getAttendanceById(int attendanceId) {
        String sql = "SELECT * FROM Attendance WHERE attendance_id=?";
        
        try {
            // Use the shared connection
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) return null;
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            pstmt.setInt(1, attendanceId);
            
            ResultSet rs = pstmt.executeQuery();
            Attendance attendance = null;
            
            if (rs.next()) {
                attendance = extractAttendanceFromResultSet(rs);
            }
            
            rs.close();
            pstmt.close();
            
            return attendance;
        } catch (SQLException e) {
            System.err.println("Error getting attendance: " + e.getMessage());
            return null;
        }
    }
    
    // Get all attendance records for a specific employee
    public List<Attendance> getAttendanceByEmployeeId(int employeeId) {
        List<Attendance> attendanceList = new ArrayList<>();
        String sql = "SELECT * FROM Attendance WHERE employee_id=? ORDER BY date DESC";
        
        try {
            // Use the shared connection
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) return attendanceList;
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            pstmt.setInt(1, employeeId);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                attendanceList.add(extractAttendanceFromResultSet(rs));
            }
            
            rs.close();
            pstmt.close();
            
            return attendanceList;
        } catch (SQLException e) {
            System.err.println("Error getting attendance for employee: " + e.getMessage());
            return attendanceList;
        }
    }
    
    // Get all attendance records for a specific date
    public List<Attendance> getAttendanceByDate(Date date) {
        List<Attendance> attendanceList = new ArrayList<>();
        String sql = "SELECT * FROM Attendance WHERE date=?";
        
        try {
            // Use the shared connection
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) return attendanceList;
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            pstmt.setDate(1, date);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                attendanceList.add(extractAttendanceFromResultSet(rs));
            }
            
            rs.close();
            pstmt.close();
            
            return attendanceList;
        } catch (SQLException e) {
            System.err.println("Error getting attendance by date: " + e.getMessage());
            return attendanceList;
        }
    }
    
    // Manual attendance counting as a fallback method
    public int countAttendanceDaysManually(int employeeId, int month, int year) {
        System.out.println("Using manual attendance counting for employee ID: " + employeeId);
        
        // Get all attendance records for the employee
        String sql = "SELECT * FROM Attendance WHERE employee_id = ? ORDER BY date";
        int count = 0;
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) {
                System.err.println("Database connection is null in manual counting");
                return 0;
            }
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, employeeId);
            
            ResultSet rs = pstmt.executeQuery();
            
            // Output all attendance records for this employee for debugging
            System.out.println("\nAll attendance records for employee " + employeeId + ":");
            while (rs.next()) {
                Date date = rs.getDate("date");
                String status = rs.getString("status");
                
                // Extract month and year from the date
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTime(date);
                int recordMonth = cal.get(java.util.Calendar.MONTH) + 1; // Calendar months are 0-based
                int recordYear = cal.get(java.util.Calendar.YEAR);
                
                System.out.println("Record: Date=" + date + 
                                  ", Month=" + recordMonth + 
                                  ", Year=" + recordYear + 
                                  ", Status='" + status + "'");
                
                // Count if the month and year match AND status is "present"
                if (recordMonth == month && recordYear == year) {
                    if (status != null && status.equals("present")) {
                        count++;
                        System.out.println("Counted attendance record: " + date + " with status: '" + status + "'");
                    } else {
                        System.out.println("Skipped record with status: '" + status + "'");
                    }
                }
            }
            
            rs.close();
            pstmt.close();
            
            System.out.println("Manual count result: " + count + " days present in month " + month + "/" + year);
            return count;
            
        } catch (SQLException e) {
            System.err.println("Error in manual attendance counting: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    // Update the main counting method to use fallback if primary method fails
    public int countAttendanceDaysByEmployeeAndMonth(int employeeId, int month, int year) {
        int count = 0;
        
        // Changed the SQL to use English status instead of Vietnamese
        String sql = "SELECT COUNT(*) as attendance_count FROM Attendance " +
                     "WHERE employee_id = ? AND status = 'present' AND " +
                     "MONTH(date) = ? AND YEAR(date) = ?";
        
        // Debug - print the SQL query with actual values
        System.out.println("Executing attendance count SQL for employee ID: " + employeeId + 
                           ", Month: " + month + ", Year: " + year);
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) {
                System.err.println("Database connection is null");
                return countAttendanceDaysManually(employeeId, month, year);
            }
            
            pstmt = conn.prepareStatement(sql);
            
            pstmt.setInt(1, employeeId);
            pstmt.setInt(2, month);
            pstmt.setInt(3, year);
            
            System.out.println("Executing query: " + sql + " with params: " + 
                               "employeeId=" + employeeId + 
                               ", month=" + month + 
                               ", year=" + year);
            
            rs = pstmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt("attendance_count");
                System.out.println("Query returned count: " + count);
            } else {
                System.out.println("Query returned no results");
            }
            
            // If the SQL query returns 0, use manual counting as fallback
            if (count == 0) {
                int manualCount = countAttendanceDaysManually(employeeId, month, year);
                if (manualCount > 0) {
                    System.out.println("SQL query returned 0, but manual count found " + manualCount + " records. Using manual count.");
                    return manualCount;
                }
            }
            
            return count;
            
        } catch (SQLException e) {
            System.err.println("Error counting attendance days: " + e.getMessage());
            e.printStackTrace();
            return countAttendanceDaysManually(employeeId, month, year);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                // Don't close connection as it's shared
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }
    
    // Helper method to extract an Attendance object from a ResultSet
    private Attendance extractAttendanceFromResultSet(ResultSet rs) throws SQLException {
        Attendance attendance = new Attendance();
        attendance.setAttendanceId(rs.getInt("attendance_id"));
        attendance.setEmployeeId(rs.getInt("employee_id"));
        attendance.setDate(rs.getDate("date"));
        attendance.setStatus(rs.getString("status"));
        attendance.setNotes(rs.getString("notes"));
        
        return attendance;
    }
}