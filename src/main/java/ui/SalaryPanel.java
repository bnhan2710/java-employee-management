package ui;

import dao.AttendanceDAO;
import dao.EmployeeDAO;
import dao.SalaryDAO;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import models.Employee;
import models.Salary;

public class SalaryPanel extends JPanel implements EmployeePanel.EmployeeDataListener {
    
    private JTable salaryTable;
    private DefaultTableModel tableModel;
    private JComboBox<Employee> employeeComboBox;
    private JComboBox<String> monthComboBox;
    private JComboBox<Integer> yearComboBox;
    private JTextField basicSalaryField, workingDaysField, totalSalaryField;
    private CustomJDateChooser payDateChooser;
    private JComboBox<String> statusComboBox;
    private JTextArea notesArea;
    private JButton addButton, updateButton, deleteButton, clearButton, calculateButton, fetchAttendanceButton;
    
    private SalaryDAO salaryDAO;
    private EmployeeDAO employeeDAO;
    private AttendanceDAO attendanceDAO;
    private Salary selectedSalary;
    
    // Phương thức định dạng tiền tệ
    private String formatCurrency(BigDecimal value) {
        if (value == null) return "";
        // Định dạng với dấu phân cách hàng nghìn và không hiển thị thập phân
        java.text.NumberFormat formatter = java.text.NumberFormat.getInstance(java.util.Locale.getDefault());
        formatter.setMaximumFractionDigits(0);
        formatter.setGroupingUsed(true);
        return formatter.format(value);
    }
    
    public SalaryPanel() {
        salaryDAO = new SalaryDAO();
        employeeDAO = new EmployeeDAO();
        attendanceDAO = new AttendanceDAO();
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        initComponents();
        setupLayout();
        setupEvents();
        loadEmployees();
        loadSalaryData();
    }
    
    private void initComponents() {
        String[] columns = {"ID", "Nhân Viên", "Tháng/Năm", "Ngày Làm Việc", "Tổng Lương", "Trạng Thái"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        salaryTable = new JTable(tableModel);
        salaryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        salaryTable.getTableHeader().setReorderingAllowed(false);
        
        employeeComboBox = new JComboBox<>();
        
        String[] months = {"Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6", 
                           "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"};
        monthComboBox = new JComboBox<>(months);
        monthComboBox.setSelectedIndex(LocalDate.now().getMonthValue() - 1);
        
        Integer[] years = new Integer[10];
        int currentYear = LocalDate.now().getYear();
        for (int i = 0; i < years.length; i++) {
            years[i] = currentYear - 5 + i;
        }
        yearComboBox = new JComboBox<>(years);
        yearComboBox.setSelectedItem(currentYear);
        
        basicSalaryField = new JTextField(10);
        workingDaysField = new JTextField(10);
        workingDaysField.setText("22");
        
        totalSalaryField = new JTextField(10);
        totalSalaryField.setEditable(false);
        
        payDateChooser = new CustomJDateChooser();
        payDateChooser.setDate(java.util.Date.from(LocalDate.now().atStartOfDay().toInstant(java.time.ZoneOffset.UTC)));
        
        String[] statusOptions = {"chờ thanh toán", "đã thanh toán", "đã hủy"};
        statusComboBox = new JComboBox<>(statusOptions);
        
        notesArea = new JTextArea(3, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        
        addButton = new JButton("Thêm");
        updateButton = new JButton("Cập Nhật");
        deleteButton = new JButton("Xóa");
        clearButton = new JButton("Làm Mới");
        calculateButton = new JButton("Tính Lương");
        fetchAttendanceButton = new JButton("Lấy Ngày Có Mặt");
        
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }
    
    private void setupLayout() {
        JScrollPane tableScrollPane = new JScrollPane(salaryTable);
        tableScrollPane.setPreferredSize(new Dimension(600, 300));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Thông Tin Lương"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Nhân Viên:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        formPanel.add(employeeComboBox, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        formPanel.add(new JLabel("Tháng:"), gbc);
        gbc.gridx = 1;
        formPanel.add(monthComboBox, gbc);
        
        gbc.gridx = 2;
        formPanel.add(yearComboBox, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Lương Cơ Bản:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        formPanel.add(basicSalaryField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        formPanel.add(new JLabel("Ngày Làm Việc:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        formPanel.add(workingDaysField, gbc);
        gbc.gridx = 2;
        formPanel.add(fetchAttendanceButton, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        formPanel.add(calculateButton, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        formPanel.add(new JLabel("Tổng Lương:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        formPanel.add(totalSalaryField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        formPanel.add(new JLabel("Ngày Thanh Toán:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        formPanel.add(payDateChooser, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 1;
        formPanel.add(new JLabel("Trạng Thái:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        formPanel.add(statusComboBox, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 1;
        formPanel.add(new JLabel("Ghi Chú:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        formPanel.add(new JScrollPane(notesArea), gbc);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        
        add(tableScrollPane, BorderLayout.CENTER);
        add(formPanel, BorderLayout.EAST);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupEvents() {
        salaryTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = salaryTable.getSelectedRow();
                if (selectedRow >= 0) {
                    int id = (Integer) tableModel.getValueAt(selectedRow, 0);
                    selectedSalary = salaryDAO.getSalaryById(id);
                    if (selectedSalary != null) {
                        populateForm(selectedSalary);
                        updateButton.setEnabled(true);
                        deleteButton.setEnabled(true);
                    }
                }
            }
        });
        
        addButton.addActionListener(this::addSalary);
        
        updateButton.addActionListener(this::updateSalary);
        
        deleteButton.addActionListener(this::deleteSalary);
        
        clearButton.addActionListener(e -> {
            clearForm();
            salaryTable.clearSelection();
        });
        
        calculateButton.addActionListener(e -> calculateTotalSalary());
        
        fetchAttendanceButton.addActionListener(e -> fetchAttendanceDays());
        
        employeeComboBox.addActionListener(e -> {
            Employee selectedEmployee = (Employee) employeeComboBox.getSelectedItem();
            if (selectedEmployee != null) {
                // Định dạng lương cơ bản với dấu phân cách hàng nghìn
                java.text.NumberFormat formatter = java.text.NumberFormat.getInstance();
                formatter.setMaximumFractionDigits(0);
                formatter.setGroupingUsed(true);
                basicSalaryField.setText(formatter.format(selectedEmployee.getBasicSalary()));
            }
        });
        
        monthComboBox.addActionListener(e -> fetchAttendanceDays());
        yearComboBox.addActionListener(e -> fetchAttendanceDays());
    }
    
    private void addSalary(ActionEvent e) {
        if (validateForm()) {
            Salary salary = new Salary();
            
            Employee selectedEmployee = (Employee) employeeComboBox.getSelectedItem();
            salary.setEmployeeId(selectedEmployee.getEmployeeId());
            
            salary.setMonth(monthComboBox.getSelectedIndex() + 1);
            salary.setYear((Integer) yearComboBox.getSelectedItem());
            
            salary.setWorkingDays(Integer.parseInt(workingDaysField.getText()));
            
            if (!totalSalaryField.getText().isEmpty()) {
                // Xử lý chuỗi có dấu phân cách hàng nghìn
                String totalSalaryStr = totalSalaryField.getText().replaceAll("[,.]", "");
                salary.setTotalSalary(new BigDecimal(totalSalaryStr));
            } else {
                int month = monthComboBox.getSelectedIndex() + 1;
                int year = (Integer) yearComboBox.getSelectedItem();
                LocalDate date = LocalDate.of(year, month, 1);
                int daysInMonth = date.lengthOfMonth();
                
                double totalSalary = selectedEmployee.calculateSalary(salary.getWorkingDays(), daysInMonth);
                salary.setTotalSalary(new BigDecimal(totalSalary).setScale(2, BigDecimal.ROUND_HALF_UP));
            }
            
            if (payDateChooser.getDate() != null) {
                salary.setPayDate(new Date(payDateChooser.getDate().getTime()));
            }
            
            salary.setStatus((String) statusComboBox.getSelectedItem());
            salary.setNotes(notesArea.getText());
            
            if (salaryDAO.addSalary(salary)) {
                JOptionPane.showMessageDialog(this, "Thêm dữ liệu lương thành công!");
                clearForm();
                loadSalaryData();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi thêm dữ liệu lương", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void updateSalary(ActionEvent e) {
        if (selectedSalary != null && validateForm()) {
            Employee selectedEmployee = (Employee) employeeComboBox.getSelectedItem();
            selectedSalary.setEmployeeId(selectedEmployee.getEmployeeId());
            
            selectedSalary.setMonth(monthComboBox.getSelectedIndex() + 1);
            selectedSalary.setYear((Integer) yearComboBox.getSelectedItem());
            
            selectedSalary.setWorkingDays(Integer.parseInt(workingDaysField.getText()));
            
            if (!totalSalaryField.getText().isEmpty()) {
                // Xử lý chuỗi có dấu phân cách hàng nghìn
                String totalSalaryStr = totalSalaryField.getText().replaceAll("[,.]", "");
                selectedSalary.setTotalSalary(new BigDecimal(totalSalaryStr));
            } else {
                int month = monthComboBox.getSelectedIndex() + 1;
                int year = (Integer) yearComboBox.getSelectedItem();
                LocalDate date = LocalDate.of(year, month, 1);
                int daysInMonth = date.lengthOfMonth();
                
                double totalSalary = selectedEmployee.calculateSalary(selectedSalary.getWorkingDays(), daysInMonth);
                selectedSalary.setTotalSalary(new BigDecimal(totalSalary).setScale(2, BigDecimal.ROUND_HALF_UP));
            }
            
            if (payDateChooser.getDate() != null) {
                selectedSalary.setPayDate(new Date(payDateChooser.getDate().getTime()));
            }
            
            selectedSalary.setStatus((String) statusComboBox.getSelectedItem());
            selectedSalary.setNotes(notesArea.getText());
            
            if (salaryDAO.updateSalary(selectedSalary)) {
                JOptionPane.showMessageDialog(this, "Cập nhật thông tin lương thành công!");
                clearForm();
                loadSalaryData();
                salaryTable.clearSelection();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật thông tin lương", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void deleteSalary(ActionEvent e) {
        if (selectedSalary != null) {
            int choice = JOptionPane.showConfirmDialog(this, 
                "Bạn có chắc muốn xóa thông tin lương này không?",
                "Xác Nhận Xóa", JOptionPane.YES_NO_OPTION);
            
            if (choice == JOptionPane.YES_OPTION) {
                if (salaryDAO.deleteSalary(selectedSalary.getSalaryId())) {
                    JOptionPane.showMessageDialog(this, "Xóa thông tin lương thành công!");
                    clearForm();
                    loadSalaryData();
                    salaryTable.clearSelection();
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi khi xóa thông tin lương", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    private void calculateTotalSalary() {
        try {
            Employee selectedEmployee = (Employee) employeeComboBox.getSelectedItem();
            if (selectedEmployee == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên", "Lỗi Tính Toán", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int workingDays = Integer.parseInt(workingDaysField.getText());
            
            int month = monthComboBox.getSelectedIndex() + 1;
            int year = (Integer) yearComboBox.getSelectedItem();
            
            LocalDate date = LocalDate.of(year, month, 1);
            int daysInMonth = date.lengthOfMonth();
            
            double totalSalary = selectedEmployee.calculateSalary(workingDays, daysInMonth);
            
            // Định dạng tổng lương với dấu phân cách hàng nghìn
            java.text.NumberFormat formatter = java.text.NumberFormat.getInstance();
            formatter.setMaximumFractionDigits(0);
            formatter.setGroupingUsed(true);
            totalSalaryField.setText(formatter.format(totalSalary));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đúng định dạng số cho ngày làm việc",
                                         "Lỗi Tính Toán", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void fetchAttendanceDays() {
        try {
            Employee selectedEmployee = (Employee) employeeComboBox.getSelectedItem();
            if (selectedEmployee == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            int month = monthComboBox.getSelectedIndex() + 1;
            int year = (Integer) yearComboBox.getSelectedItem();
            
            // Debug information
            System.out.println("=== FETCHING ATTENDANCE ===");
            System.out.println("Employee: " + selectedEmployee.getFullName() + 
                              " (ID: " + selectedEmployee.getEmployeeId() + ")");
            System.out.println("Month: " + month + ", Year: " + year);
            int attendanceDays = 0;
            try {
                System.out.println("Calling countAttendanceDaysByEmployeeAndMonth...");
                attendanceDays = attendanceDAO.countAttendanceDaysByEmployeeAndMonth(
                    selectedEmployee.getEmployeeId(), month, year);
                System.out.println("Found " + attendanceDays + " attendance days with status 'present'");
                
                // Sanity check - if calendar days in the month is less than 31 and attendance days is more,
                // something is wrong
                java.time.YearMonth yearMonth = java.time.YearMonth.of(year, month);
                int daysInMonth = yearMonth.lengthOfMonth();
                
                if (attendanceDays > daysInMonth) {
                    System.err.println("WARNING: Attendance days (" + attendanceDays + 
                                     ") exceeds days in month (" + daysInMonth + ")!");
                    attendanceDays = daysInMonth; // Cap at maximum possible
                }
                
            } catch (Exception e) {
                System.err.println("ERROR in fetchAttendanceDays: " + e.getMessage());
                e.printStackTrace();
                // Show a popup with error details
                JOptionPane.showMessageDialog(this, 
                    "Lỗi khi lấy dữ liệu chấm công: " + e.getMessage(), 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                // Use default value of 0 in case of error
            }
            
            // Update the UI
            workingDaysField.setText(String.valueOf(attendanceDays));
            
            // Auto calculate salary after fetching attendance
            calculateTotalSalary();
            
            // Provide a summary message to the user
            JOptionPane.showMessageDialog(this, 
                "Đã tìm thấy " + attendanceDays + " ngày có mặt của nhân viên " + 
                selectedEmployee.getFullName() + " trong tháng " + month + "/" + year, 
                "Kết quả", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (Exception ex) {
            System.err.println("Unexpected error in fetchAttendanceDays: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Lỗi khi lấy dữ liệu chấm công: " + ex.getMessage(), 
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private boolean validateForm() {
        if (employeeComboBox.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên", "Lỗi Xác Thực", JOptionPane.ERROR_MESSAGE);
            employeeComboBox.requestFocus();
            return false;
        }
        
        try {
            int workingDays = Integer.parseInt(workingDaysField.getText());
            if (workingDays <= 0 || workingDays > 31) {
                JOptionPane.showMessageDialog(this, "Ngày làm việc phải từ 1 đến 31", "Lỗi Xác Thực", JOptionPane.ERROR_MESSAGE);
                workingDaysField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Ngày làm việc phải là một số hợp lệ", "Lỗi Xác Thực", JOptionPane.ERROR_MESSAGE);
            workingDaysField.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void populateForm(Salary salary) {
        for (int i = 0; i < employeeComboBox.getItemCount(); i++) {
            Employee employee = employeeComboBox.getItemAt(i);
            if (employee.getEmployeeId() == salary.getEmployeeId()) {
                employeeComboBox.setSelectedIndex(i);
                break;
            }
        }
        
        monthComboBox.setSelectedIndex(salary.getMonth() - 1);
        yearComboBox.setSelectedItem(salary.getYear());
        
        Employee selectedEmployee = (Employee) employeeComboBox.getSelectedItem();
        if (selectedEmployee != null) {
            // Định dạng lương cơ bản với dấu phân cách hàng nghìn
            java.text.NumberFormat formatter = java.text.NumberFormat.getInstance();
            formatter.setMaximumFractionDigits(0);
            formatter.setGroupingUsed(true);
            basicSalaryField.setText(formatter.format(selectedEmployee.getBasicSalary()));
        }
        
        workingDaysField.setText(String.valueOf(salary.getWorkingDays()));
        
        // Định dạng tổng lương với dấu phân cách hàng nghìn
        java.text.NumberFormat formatter = java.text.NumberFormat.getInstance();
        formatter.setMaximumFractionDigits(0);
        formatter.setGroupingUsed(true);
        totalSalaryField.setText(formatter.format(salary.getTotalSalary()));
        
        if (salary.getPayDate() != null) {
            payDateChooser.setDate(new java.util.Date(salary.getPayDate().getTime()));
        } else {
            payDateChooser.setDate(null);
        }
        
        for (int i = 0; i < statusComboBox.getItemCount(); i++) {
            if (statusComboBox.getItemAt(i).equals(salary.getStatus())) {
                statusComboBox.setSelectedIndex(i);
                break;
            }
        }
        
        notesArea.setText(salary.getNotes());
    }
    
    private void clearForm() {
        if (employeeComboBox.getItemCount() > 0) {
            employeeComboBox.setSelectedIndex(0);
        }
        
        monthComboBox.setSelectedIndex(LocalDate.now().getMonthValue() - 1);
        yearComboBox.setSelectedItem(LocalDate.now().getYear());
        
        basicSalaryField.setText("");
        workingDaysField.setText("22");
        totalSalaryField.setText("");
        
        payDateChooser.setDate(java.util.Date.from(LocalDate.now().atStartOfDay().toInstant(java.time.ZoneOffset.UTC)));
        statusComboBox.setSelectedIndex(0);
        notesArea.setText("");
        
        selectedSalary = null;
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }
    
    private void loadEmployees() {
        employeeComboBox.removeAllItems();
        List<Employee> employees = employeeDAO.getAllEmployees();
        for (Employee employee : employees) {
            employeeComboBox.addItem(employee);
        }
    }
    
    public void loadSalaryData() {
        tableModel.setRowCount(0);
        
        List<Employee> employees = employeeDAO.getAllEmployees();
        
        List<Salary> salaryList = new ArrayList<>();
        for (Employee employee : employees) {
            List<Salary> employeeSalary = salaryDAO.getSalaryByEmployeeId(employee.getEmployeeId());
            salaryList.addAll(employeeSalary);
        }
        
        for (Salary salary : salaryList) {
            String employeeName = "Unknown";
            for (Employee employee : employees) {
                if (employee.getEmployeeId() == salary.getEmployeeId()) {
                    employeeName = employee.getFullName();
                    break;
                }
            }
            
            String monthName = Month.of(salary.getMonth()).toString();
            monthName = monthName.charAt(0) + monthName.substring(1).toLowerCase();
            
            // Định dạng tiền tệ cho tổng lương
            java.text.NumberFormat formatter = java.text.NumberFormat.getInstance();
            formatter.setMaximumFractionDigits(0);
            formatter.setGroupingUsed(true);
            String formattedSalary = formatter.format(salary.getTotalSalary());
            
            Object[] row = {
                salary.getSalaryId(),
                employeeName,
                monthName + " " + salary.getYear(),
                salary.getWorkingDays(),
                formattedSalary,
                salary.getStatus()
            };
            tableModel.addRow(row);
        }
    }
    
    @Override
    public void employeeDataChanged() {
        loadEmployees();
    }
}