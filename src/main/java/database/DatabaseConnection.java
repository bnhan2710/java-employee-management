package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/employee_management";
    private static final String USER = "root";
    private static final String PASSWORD = "12345678"; // thay bằng mk db 
    private static Connection connection;
    
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Kết nối đến cơ sở dữ liệu thành công!");
            } else {
                if (!connection.isValid(2)) {
                    connection.close();
                    connection = DriverManager.getConnection(URL, USER, PASSWORD);
                    System.out.println("Kết nối đã được thiết lập lại thành công!");
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi kết nối cơ sở dữ liệu: " + e.getMessage());
        }
        return connection;
    }
    
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("Đã đóng kết nối cơ sở dữ liệu.");
            } catch (SQLException e) {
                System.err.println("Lỗi khi đóng kết nối cơ sở dữ liệu: " + e.getMessage());
            }
        }
    }
    
    public static void initializeDatabase() {
        try {
            Connection conn = getConnection();
            if (conn == null) {
                System.err.println("Không thể thiết lập kết nối cơ sở dữ liệu.");
                return;
            }
            
            Statement stmt = conn.createStatement();
            
            stmt.execute("CREATE DATABASE IF NOT EXISTS employee_management");
            stmt.execute("USE employee_management");
            
            String createEmployeesTable = "CREATE TABLE IF NOT EXISTS Employees (" +
                    "employee_id INT PRIMARY KEY AUTO_INCREMENT," +
                    "full_name VARCHAR(100) NOT NULL," +
                    "phone_number VARCHAR(20)," +
                    "email VARCHAR(100)," +
                    "position VARCHAR(50)," +
                    "hire_date DATE," +
                    "basic_salary DOUBLE DEFAULT 0" +
                    ")";
            stmt.execute(createEmployeesTable);
            
            // Check if basic_salary column exists, if not add it
            try {
                stmt.executeQuery("SELECT basic_salary FROM Employees LIMIT 1");
            } catch (SQLException e) {
                // Column doesn't exist, add it
                stmt.execute("ALTER TABLE Employees ADD COLUMN basic_salary DOUBLE DEFAULT 0");
                System.out.println("Added basic_salary column to Employees table");
            }
            
            String createAttendanceTable = "CREATE TABLE IF NOT EXISTS Attendance (" +
                    "attendance_id INT PRIMARY KEY AUTO_INCREMENT," +
                    "employee_id INT," +
                    "date DATE NOT NULL," +
                    "status VARCHAR(20) NOT NULL," +
                    "notes TEXT," +
                    "FOREIGN KEY (employee_id) REFERENCES Employees(employee_id)" +
                    "ON DELETE CASCADE" +
                    ")";
            stmt.execute(createAttendanceTable);
            
            String createSalaryTable = "CREATE TABLE IF NOT EXISTS Salary (" +
                    "salary_id INT PRIMARY KEY AUTO_INCREMENT," +
                    "employee_id INT," +
                    "month INT NOT NULL," +
                    "year INT NOT NULL," +
                    "working_days INT," +
                    "total_salary DECIMAL(10,2)," +
                    "pay_date DATE," +
                    "status VARCHAR(20) DEFAULT 'chờ thanh toán'," +
                    "notes TEXT," +
                    "FOREIGN KEY (employee_id) REFERENCES Employees(employee_id)" +
                    "ON DELETE CASCADE" +
                    ")";
            stmt.execute(createSalaryTable);
            
            // Check if basic_salary column exists in Salary table, if yes, remove it
            try {
                stmt.executeQuery("SELECT basic_salary FROM Salary LIMIT 1");
                // If we get here, the column exists and we need to remove it
                try {
                    stmt.execute("ALTER TABLE Salary DROP COLUMN basic_salary");
                    System.out.println("Removed basic_salary column from Salary table");
                } catch (SQLException ex) {
                    System.err.println("Error removing basic_salary column: " + ex.getMessage());
                }
            } catch (SQLException e) {
                // Column doesn't exist, which is what we want
                System.out.println("basic_salary column does not exist in Salary table");
            }
            
            System.out.println("Tạo bảng cơ sở dữ liệu thành công!");
            
            stmt.close();
            
        } catch (SQLException e) {
            System.err.println("Lỗi khởi tạo cơ sở dữ liệu: " + e.getMessage());
        }
    }

} 