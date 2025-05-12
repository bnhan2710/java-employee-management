package ui;

import dao.EmployeeDAO;
import models.Employee;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Date;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EmployeePanel extends JPanel {
    
    private JTable employeeTable;
    private DefaultTableModel tableModel;
    private JTextField nameField, phoneField, emailField, positionField, basicSalaryField;
    private CustomJDateChooser hireDateChooser;
    private JButton addButton, updateButton, deleteButton, clearButton;
    
    private EmployeeDAO employeeDAO;
    private Employee selectedEmployee;
    
    // List of panels to notify when employee data changes
    private List<EmployeeDataListener> dataListeners = new ArrayList<>();
    
    // Format for currency in VND
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private DecimalFormat decimalFormat = new DecimalFormat("#,###");
    
    // Interface for panels that need to be notified of employee data changes
    public interface EmployeeDataListener {
        void employeeDataChanged();
    }
    
    // Method to add a listener
    public void addEmployeeDataListener(EmployeeDataListener listener) {
        if (!dataListeners.contains(listener)) {
            dataListeners.add(listener);
        }
    }
    
    // Method to notify all listeners
    private void notifyEmployeeDataChanged() {
        for (EmployeeDataListener listener : dataListeners) {
            listener.employeeDataChanged();
        }
    }
    
    public EmployeePanel() {
        employeeDAO = new EmployeeDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        initComponents();
        setupLayout();
        setupEvents();
        loadEmployeeData();
    }
    
    private void initComponents() {
        String[] columns = {"ID", "Họ Tên", "Số Điện Thoại", "Email", "Chức Vụ", "Ngày Tuyển Dụng", "Lương Cơ Bản"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        employeeTable = new JTable(tableModel);
        employeeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        employeeTable.getTableHeader().setReorderingAllowed(false);
        
        nameField = new JTextField(20);
        phoneField = new JTextField(20);
        emailField = new JTextField(20);
        positionField = new JTextField(20);
        basicSalaryField = new JTextField(20);
        hireDateChooser = new CustomJDateChooser();
        hireDateChooser.setDate(java.util.Date.from(LocalDate.now().atStartOfDay().toInstant(java.time.ZoneOffset.UTC)));
        
        addButton = new JButton("Thêm");
        updateButton = new JButton("Cập Nhật");
        deleteButton = new JButton("Xóa");
        clearButton = new JButton("Làm Mới");
        
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }
    
    private void setupLayout() {
        JScrollPane tableScrollPane = new JScrollPane(employeeTable);
        tableScrollPane.setPreferredSize(new Dimension(600, 300));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Thông Tin Nhân Viên"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Họ Tên:"), gbc);
        gbc.gridx = 1;
        formPanel.add(nameField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Số Điện Thoại:"), gbc);
        gbc.gridx = 1;
        formPanel.add(phoneField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        formPanel.add(emailField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Chức Vụ:"), gbc);
        gbc.gridx = 1;
        formPanel.add(positionField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("Ngày Tuyển Dụng:"), gbc);
        gbc.gridx = 1;
        formPanel.add(hireDateChooser, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(new JLabel("Lương Cơ Bản:"), gbc);
        gbc.gridx = 1;
        formPanel.add(basicSalaryField, gbc);
        
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
        employeeTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = employeeTable.getSelectedRow();
                if (selectedRow >= 0) {
                    int id = (Integer) tableModel.getValueAt(selectedRow, 0);
                    selectedEmployee = employeeDAO.getEmployeeById(id);
                    if (selectedEmployee != null) {
                        populateForm(selectedEmployee);
                        updateButton.setEnabled(true);
                        deleteButton.setEnabled(true);
                    }
                }
            }
        });
        
        addButton.addActionListener(this::addEmployee);
        
        updateButton.addActionListener(this::updateEmployee);
        
        deleteButton.addActionListener(this::deleteEmployee);
        
        clearButton.addActionListener(e -> {
            clearForm();
            employeeTable.clearSelection();
        });
    }
    
    private void addEmployee(ActionEvent e) {
        if (validateForm()) {
            Employee employee = new Employee();
            employee.setFullName(nameField.getText());
            employee.setPhoneNumber(phoneField.getText());
            employee.setEmail(emailField.getText());
            employee.setPosition(positionField.getText());
            
            java.util.Date utilDate = hireDateChooser.getDate();
            employee.setHireDate(new Date(utilDate.getTime()));
            
            try {
                // Parse salary from the input field, removing any non-digit characters
                String salaryText = basicSalaryField.getText().trim().replaceAll("[^0-9]", "");
                double basicSalary = Double.parseDouble(salaryText);
                employee.setBasicSalary(basicSalary);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Lương cơ bản phải là một số hợp lệ", "Lỗi", JOptionPane.ERROR_MESSAGE);
                basicSalaryField.requestFocus();
                return;
            }
            
            if (employeeDAO.addEmployee(employee)) {
                JOptionPane.showMessageDialog(this, "Thêm nhân viên thành công!");
                clearForm();
                loadEmployeeData();
                // Notify listeners that employee data has changed
                notifyEmployeeDataChanged();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi thêm nhân viên", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void updateEmployee(ActionEvent e) {
        if (selectedEmployee != null && validateForm()) {
            selectedEmployee.setFullName(nameField.getText());
            selectedEmployee.setPhoneNumber(phoneField.getText());
            selectedEmployee.setEmail(emailField.getText());
            selectedEmployee.setPosition(positionField.getText());
            
            java.util.Date utilDate = hireDateChooser.getDate();
            selectedEmployee.setHireDate(new Date(utilDate.getTime()));
            
            try {
                // Parse salary from the input field, removing any non-digit characters
                String salaryText = basicSalaryField.getText().trim().replaceAll("[^0-9]", "");
                double basicSalary = Double.parseDouble(salaryText);
                selectedEmployee.setBasicSalary(basicSalary);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Lương cơ bản phải là một số hợp lệ", "Lỗi", JOptionPane.ERROR_MESSAGE);
                basicSalaryField.requestFocus();
                return;
            }
            
            if (employeeDAO.updateEmployee(selectedEmployee)) {
                JOptionPane.showMessageDialog(this, "Cập nhật nhân viên thành công!");
                clearForm();
                loadEmployeeData();
                employeeTable.clearSelection();
                // Notify listeners that employee data has changed
                notifyEmployeeDataChanged();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật nhân viên", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void deleteEmployee(ActionEvent e) {
        if (selectedEmployee != null) {
            int choice = JOptionPane.showConfirmDialog(this, 
                "Bạn có chắc muốn xóa nhân viên này không?\nĐiều này cũng sẽ xóa tất cả dữ liệu chấm công và lương liên quan.",
                "Xác Nhận Xóa", JOptionPane.YES_NO_OPTION);
            
            if (choice == JOptionPane.YES_OPTION) {
                if (employeeDAO.deleteEmployee(selectedEmployee.getEmployeeId())) {
                    JOptionPane.showMessageDialog(this, "Xóa nhân viên thành công!");
                    clearForm();
                    loadEmployeeData();
                    employeeTable.clearSelection();
                    // Notify listeners that employee data has changed
                    notifyEmployeeDataChanged();
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi khi xóa nhân viên", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    private boolean validateForm() {
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Họ tên không được để trống", "Lỗi Xác Thực", JOptionPane.ERROR_MESSAGE);
            nameField.requestFocus();
            return false;
        }
        
        if (positionField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chức vụ không được để trống", "Lỗi Xác Thực", JOptionPane.ERROR_MESSAGE);
            positionField.requestFocus();
            return false;
        }
        
        if (hireDateChooser.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Ngày tuyển dụng không được để trống", "Lỗi Xác Thực", JOptionPane.ERROR_MESSAGE);
            hireDateChooser.requestFocus();
            return false;
        }
        
        if (basicSalaryField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập lương cơ bản", "Lỗi Xác Thực", JOptionPane.ERROR_MESSAGE);
            basicSalaryField.requestFocus();
            return false;
        }
        
        try {
            // Parse salary from the input field, removing any non-digit characters
            String salaryText = basicSalaryField.getText().trim().replaceAll("[^0-9]", "");
            double basicSalary = Double.parseDouble(salaryText);
            if (basicSalary < 0) {
                JOptionPane.showMessageDialog(this, "Lương cơ bản không thể âm", "Lỗi Xác Thực", JOptionPane.ERROR_MESSAGE);
                basicSalaryField.requestFocus();
                return false;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Lương cơ bản phải là một số hợp lệ", "Lỗi Xác Thực", JOptionPane.ERROR_MESSAGE);
            basicSalaryField.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void populateForm(Employee employee) {
        nameField.setText(employee.getFullName());
        phoneField.setText(employee.getPhoneNumber());
        emailField.setText(employee.getEmail());
        positionField.setText(employee.getPosition());
        basicSalaryField.setText(decimalFormat.format(employee.getBasicSalary()) + " VNĐ");
        
        if (employee.getHireDate() != null) {
            hireDateChooser.setDate(new java.util.Date(employee.getHireDate().getTime()));
        } else {
            hireDateChooser.setDate(null);
        }
    }
    
    private void clearForm() {
        nameField.setText("");
        phoneField.setText("");
        emailField.setText("");
        positionField.setText("");
        basicSalaryField.setText("0 VNĐ");
        hireDateChooser.setDate(java.util.Date.from(LocalDate.now().atStartOfDay().toInstant(java.time.ZoneOffset.UTC)));
        
        selectedEmployee = null;
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }
    
    public void loadEmployeeData() {
        tableModel.setRowCount(0);
        
        List<Employee> employees = employeeDAO.getAllEmployees();
        
        for (Employee employee : employees) {
            Object[] row = {
                employee.getEmployeeId(),
                employee.getFullName(),
                employee.getPhoneNumber(),
                employee.getEmail(),
                employee.getPosition(),
                employee.getHireDate(),
                decimalFormat.format(employee.getBasicSalary()) + " VNĐ"
            };
            tableModel.addRow(row);
        }
    }
} 