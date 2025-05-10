package ui;

import database.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainFrame extends JFrame {
    
    private JTabbedPane tabbedPane;
    private EmployeePanel employeePanel;
    private AttendancePanel attendancePanel;
    private SalaryPanel salaryPanel;
    private SalaryReportPanel salaryReportPanel;
    
    public MainFrame() {
        initComponents();
        setupLayout();
        registerListeners();
        addWindowListener();
        
        setTitle("Hệ Thống Quản Lý Nhân Viên");
        setSize(1000, 600);
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }
    
    private void initComponents() {
        tabbedPane = new JTabbedPane();
        employeePanel = new EmployeePanel();
        attendancePanel = new AttendancePanel();
        salaryPanel = new SalaryPanel();
        salaryReportPanel = new SalaryReportPanel();
        
        tabbedPane.addTab("Nhân Viên", new ImageIcon(), employeePanel, "Quản lý thông tin nhân viên");
        tabbedPane.addTab("Chấm Công", new ImageIcon(), attendancePanel, "Quản lý chấm công");
        tabbedPane.addTab("Lương", new ImageIcon(), salaryPanel, "Quản lý lương");
        tabbedPane.addTab("Báo Cáo Lương", new ImageIcon(), salaryReportPanel, "Báo cáo và thống kê lương");
    }
    
    private void registerListeners() {
        // Register all panels as listeners for employee data changes
        employeePanel.addEmployeeDataListener(attendancePanel);
        employeePanel.addEmployeeDataListener(salaryPanel);
        employeePanel.addEmployeeDataListener(salaryReportPanel);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        add(createTopPanel(), BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(51, 153, 255));
        panel.setPreferredSize(new Dimension(0, 60));
        
        JLabel titleLabel = new JLabel("Hệ Thống Quản Lý Nhân Viên");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 0));
        
        panel.add(titleLabel, BorderLayout.WEST);
        
        return panel;
    }
    
    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        
        JLabel statusLabel = new JLabel("Sẵn sàng");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
        statusBar.add(statusLabel, BorderLayout.WEST);
        
        return statusBar;
    }
    
    private void addWindowListener() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                DatabaseConnection.closeConnection();
            }
        });
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            DatabaseConnection.initializeDatabase();
            new MainFrame();
        });
    }
} 