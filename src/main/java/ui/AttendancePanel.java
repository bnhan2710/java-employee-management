package ui;

import dao.AttendanceDAO;
import dao.EmployeeDAO;
import models.Attendance;
import models.Employee;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AttendancePanel extends JPanel implements EmployeePanel.EmployeeDataListener {
    
    private JTable attendanceTable;
    private DefaultTableModel tableModel;
    private JComboBox<Employee> employeeComboBox;
    private JComboBox<String> statusComboBox;
    private CustomJDateChooser dateChooser;
    private JTextArea notesArea;
    private JButton addButton, updateButton, deleteButton, clearButton, refreshButton;
    
    private AttendanceDAO attendanceDAO;
    private EmployeeDAO employeeDAO;
    private Attendance selectedAttendance;
    
    public AttendancePanel() {
        attendanceDAO = new AttendanceDAO();
        employeeDAO = new EmployeeDAO();
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        initComponents();
        setupLayout();
        setupEvents();
        loadEmployees();
        loadAttendanceData();
    }
    
    private void initComponents() {
        String[] columns = {"ID", "Nhân Viên", "Ngày", "Trạng Thái", "Ghi Chú"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        attendanceTable = new JTable(tableModel);
        attendanceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        attendanceTable.getTableHeader().setReorderingAllowed(false);
        
        employeeComboBox = new JComboBox<>();
        
        String[] statusOptions = {"có mặt", "vắng mặt", "đi muộn"};
        statusComboBox = new JComboBox<>(statusOptions);
        
        dateChooser = new CustomJDateChooser();
        dateChooser.setDate(java.util.Date.from(LocalDate.now().atStartOfDay().toInstant(java.time.ZoneOffset.UTC)));
        
        notesArea = new JTextArea(5, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        
        addButton = new JButton("Thêm");
        updateButton = new JButton("Cập Nhật");
        deleteButton = new JButton("Xóa");
        clearButton = new JButton("Làm Mới");
        refreshButton = new JButton("Cập Nhật DS Nhân Viên");
        
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }
    
    private void setupLayout() {
        JScrollPane tableScrollPane = new JScrollPane(attendanceTable);
        tableScrollPane.setPreferredSize(new Dimension(600, 300));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Thông Tin Chấm Công"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Nhân Viên:"), gbc);
        gbc.gridx = 1;
        formPanel.add(employeeComboBox, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Ngày:"), gbc);
        gbc.gridx = 1;
        formPanel.add(dateChooser, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Trạng Thái:"), gbc);
        gbc.gridx = 1;
        formPanel.add(statusComboBox, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Ghi Chú:"), gbc);
        gbc.gridx = 1;
        gbc.gridheight = 2;
        formPanel.add(new JScrollPane(notesArea), gbc);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(refreshButton);
        
        add(tableScrollPane, BorderLayout.CENTER);
        add(formPanel, BorderLayout.EAST);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupEvents() {
        attendanceTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = attendanceTable.getSelectedRow();
                if (selectedRow >= 0) {
                    int id = (Integer) tableModel.getValueAt(selectedRow, 0);
                    selectedAttendance = attendanceDAO.getAttendanceById(id);
                    if (selectedAttendance != null) {
                        populateForm(selectedAttendance);
                        updateButton.setEnabled(true);
                        deleteButton.setEnabled(true);
                    }
                }
            }
        });
        
        addButton.addActionListener(this::addAttendance);
        
        updateButton.addActionListener(this::updateAttendance);
        
        deleteButton.addActionListener(this::deleteAttendance);
        
        clearButton.addActionListener(e -> {
            clearForm();
            attendanceTable.clearSelection();
        });
        
        refreshButton.addActionListener(e -> loadEmployees());
    }
    
    // Implement the employee data change listener method
    @Override
    public void employeeDataChanged() {
        loadEmployees();
    }
    
    private void addAttendance(ActionEvent e) {
        if (validateForm()) {
            Attendance attendance = new Attendance();
            
            Employee selectedEmployee = (Employee) employeeComboBox.getSelectedItem();
            attendance.setEmployeeId(selectedEmployee.getEmployeeId());
            
            java.util.Date utilDate = dateChooser.getDate();
            attendance.setDate(new Date(utilDate.getTime()));
            
            attendance.setStatus((String) statusComboBox.getSelectedItem());
            attendance.setNotes(notesArea.getText());
            
            if (attendanceDAO.addAttendance(attendance)) {
                JOptionPane.showMessageDialog(this, "Thêm dữ liệu chấm công thành công!");
                clearForm();
                loadAttendanceData();
                
                // Debug log to verify attendance record was added with correct status
                System.out.println("Added attendance record for employee " + selectedEmployee.getFullName() + 
                                  " with status: " + attendance.getStatus() + 
                                  " for date: " + attendance.getDate());
                
                // Debug - count attendance records
                int month = LocalDate.now().getMonthValue();
                int year = LocalDate.now().getYear();
                int count = attendanceDAO.countAttendanceDaysByEmployeeAndMonth(selectedEmployee.getEmployeeId(), month, year);
                System.out.println("Current attendance count for " + selectedEmployee.getFullName() + 
                                  " in month " + month + "/" + year + ": " + count + " days");
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi thêm dữ liệu chấm công", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void updateAttendance(ActionEvent e) {
        if (selectedAttendance != null && validateForm()) {
            Employee selectedEmployee = (Employee) employeeComboBox.getSelectedItem();
            selectedAttendance.setEmployeeId(selectedEmployee.getEmployeeId());
            
            java.util.Date utilDate = dateChooser.getDate();
            selectedAttendance.setDate(new Date(utilDate.getTime()));
            
            selectedAttendance.setStatus((String) statusComboBox.getSelectedItem());
            selectedAttendance.setNotes(notesArea.getText());
            
            if (attendanceDAO.updateAttendance(selectedAttendance)) {
                JOptionPane.showMessageDialog(this, "Cập nhật dữ liệu chấm công thành công!");
                clearForm();
                loadAttendanceData();
                attendanceTable.clearSelection();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật dữ liệu chấm công", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void deleteAttendance(ActionEvent e) {
        if (selectedAttendance != null) {
            int choice = JOptionPane.showConfirmDialog(this, 
                "Bạn có chắc muốn xóa dữ liệu chấm công này không?",
                "Xác Nhận Xóa", JOptionPane.YES_NO_OPTION);
            
            if (choice == JOptionPane.YES_OPTION) {
                if (attendanceDAO.deleteAttendance(selectedAttendance.getAttendanceId())) {
                    JOptionPane.showMessageDialog(this, "Xóa dữ liệu chấm công thành công!");
                    clearForm();
                    loadAttendanceData();
                    attendanceTable.clearSelection();
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi khi xóa dữ liệu chấm công", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    private boolean validateForm() {
        if (employeeComboBox.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên", "Lỗi Xác Thực", JOptionPane.ERROR_MESSAGE);
            employeeComboBox.requestFocus();
            return false;
        }
        
        if (dateChooser.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Ngày không được để trống", "Lỗi Xác Thực", JOptionPane.ERROR_MESSAGE);
            dateChooser.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void populateForm(Attendance attendance) {
        for (int i = 0; i < employeeComboBox.getItemCount(); i++) {
            Employee employee = employeeComboBox.getItemAt(i);
            if (employee.getEmployeeId() == attendance.getEmployeeId()) {
                employeeComboBox.setSelectedIndex(i);
                break;
            }
        }
        
        if (attendance.getDate() != null) {
            dateChooser.setDate(new java.util.Date(attendance.getDate().getTime()));
        } else {
            dateChooser.setDate(null);
        }
        
        for (int i = 0; i < statusComboBox.getItemCount(); i++) {
            if (statusComboBox.getItemAt(i).equals(attendance.getStatus())) {
                statusComboBox.setSelectedIndex(i);
                break;
            }
        }
        
        notesArea.setText(attendance.getNotes());
    }
    
    private void clearForm() {
        if (employeeComboBox.getItemCount() > 0) {
            employeeComboBox.setSelectedIndex(0);
        }
        
        dateChooser.setDate(java.util.Date.from(LocalDate.now().atStartOfDay().toInstant(java.time.ZoneOffset.UTC)));
        statusComboBox.setSelectedIndex(0);
        notesArea.setText("");
        
        selectedAttendance = null;
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
    
    public void loadAttendanceData() {
        tableModel.setRowCount(0);
        
        List<Attendance> attendanceList = new ArrayList<>();
        
        List<Employee> employees = employeeDAO.getAllEmployees();
        
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = LocalDate.of(today.getYear(), today.getMonth(), 1);
        LocalDate lastDayOfMonth = firstDayOfMonth.plusMonths(1).minusDays(1);
        
        Date startDate = Date.valueOf(firstDayOfMonth);
        Date endDate = Date.valueOf(lastDayOfMonth);
        
        for (Employee employee : employees) {
            List<Attendance> employeeAttendance = attendanceDAO.getAttendanceByEmployeeId(employee.getEmployeeId());
            attendanceList.addAll(employeeAttendance);
        }
        
        for (Attendance attendance : attendanceList) {
            String employeeName = "Không xác định";
            for (Employee employee : employees) {
                if (employee.getEmployeeId() == attendance.getEmployeeId()) {
                    employeeName = employee.getFullName();
                    break;
                }
            }
            
            Object[] row = {
                attendance.getAttendanceId(),
                employeeName,
                attendance.getDate(),
                attendance.getStatus(),
                attendance.getNotes()
            };
            tableModel.addRow(row);
        }
    }
} 