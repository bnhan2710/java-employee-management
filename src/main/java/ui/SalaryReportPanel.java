package ui;

import dao.AttendanceDAO;
import dao.EmployeeDAO;
import dao.SalaryDAO;
import models.Employee;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.List;

public class SalaryReportPanel extends JPanel implements EmployeePanel.EmployeeDataListener {
    
    private JTable reportTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> monthComboBox;
    private JComboBox<Integer> yearComboBox;
    private JButton generateReportButton;
    
    private EmployeeDAO employeeDAO;
    private SalaryDAO salaryDAO;
    private AttendanceDAO attendanceDAO;
    
    public SalaryReportPanel() {
        employeeDAO = new EmployeeDAO();
        salaryDAO = new SalaryDAO();
        attendanceDAO = new AttendanceDAO();
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        initComponents();
        setupLayout();
        setupEvents();
    }
    
    private void initComponents() {
        String[] columns = {"ID", "Nhân Viên", "Ngày Công", "Ngày Trong Tháng", "Tổng Lương", "Trạng Thái"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        reportTable = new JTable(tableModel);
        reportTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reportTable.getTableHeader().setReorderingAllowed(false);
        
        String[] months = {"Tất cả", "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6", 
                          "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"};
        monthComboBox = new JComboBox<>(months);
        monthComboBox.setSelectedIndex(0);
        
        Integer[] years = new Integer[10];
        int currentYear = LocalDate.now().getYear();
        for (int i = 0; i < years.length; i++) {
            years[i] = currentYear - 5 + i;
        }
        yearComboBox = new JComboBox<>(years);
        yearComboBox.setSelectedItem(currentYear);
        
        generateReportButton = new JButton("Tạo Báo Cáo");
    }
    
    private void setupLayout() {
        JScrollPane tableScrollPane = new JScrollPane(reportTable);
        tableScrollPane.setPreferredSize(new Dimension(700, 400));
        
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterPanel.add(new JLabel("Tháng:"));
        filterPanel.add(monthComboBox);
        filterPanel.add(new JLabel("Năm:"));
        filterPanel.add(yearComboBox);
        filterPanel.add(generateReportButton);
        
        add(filterPanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);
    }
    
    private void setupEvents() {
        generateReportButton.addActionListener(e -> generateReport());
    }
    
    private void generateReport() {
        tableModel.setRowCount(0);
        
        int selectedMonth = monthComboBox.getSelectedIndex();
        int year = (Integer) yearComboBox.getSelectedItem();
        
        System.out.println("Generating salary report for " + (selectedMonth == 0 ? "all months" : "month " + selectedMonth) + " of year " + year);
        
        List<Employee> employees = employeeDAO.getAllEmployees();
        System.out.println("Found " + employees.size() + " employees in the database");
        
        for (Employee employee : employees) {
            if (selectedMonth == 0) { // All months
                // Show yearly summary for each employee
                BigDecimal totalEarnings = salaryDAO.calculateTotalEarningsByEmployee(employee.getEmployeeId());
                
                Object[] row = {
                    employee.getEmployeeId(),
                    employee.getFullName(),
                    "-", // No specific attendance for all months
                    "-", // No specific days in month
                    totalEarnings,
                    "Tổng cả năm"
                };
                tableModel.addRow(row);
            } else {
                // Show specific month data
                int month = selectedMonth; // 1-12
                
                // Calculate days in the selected month
                YearMonth yearMonth = YearMonth.of(year, month);
                int daysInMonth = yearMonth.lengthOfMonth();
                
                // Get attendance days for this employee in the selected month
                int attendanceDays = 0;
                System.out.println("Checking attendance for employee: " + employee.getFullName() + 
                                   " (ID: " + employee.getEmployeeId() + ") in month: " + month + "/" + year);
                
                try {
                    attendanceDays = attendanceDAO.countAttendanceDaysByEmployeeAndMonth(
                        employee.getEmployeeId(), month, year);
                    System.out.println("Found " + attendanceDays + " attendance days with status 'present'");
                } catch (Exception e) {
                    System.err.println("Error fetching attendance: " + e.getMessage());
                    e.printStackTrace();
                    // Continue with default value of 0
                }
                
                // Calculate salary using the employee's method
                double salary = employee.calculateSalary(attendanceDays, daysInMonth);
                System.out.println("Calculated salary: " + salary + " for " + attendanceDays + 
                                   " days out of " + daysInMonth + " days in month");
                
                Object[] row = {
                    employee.getEmployeeId(),
                    employee.getFullName(),
                    attendanceDays,
                    daysInMonth,
                    BigDecimal.valueOf(salary).setScale(2, BigDecimal.ROUND_HALF_UP),
                    "Tính toán"
                };
                tableModel.addRow(row);
            }
        }
    }
    
    // Implement the employee data change listener method
    @Override
    public void employeeDataChanged() {
        // Re-generate the report with the updated employee data
        generateReport();
    }
} 