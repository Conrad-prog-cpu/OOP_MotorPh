package gui;

import model.User;
import model.WorkHoursCalculator;
import model.OvertimePolicy;

import repository.EmployeeRepository;
import repository.FileEmployeeRepository;
import repository.AttendanceRepository;
import repository.FileAttendanceRepository;

import service.PayrollService;
import service.DefaultPayrollService;

import model.DefaultContributionCalculator;
import model.DefaultEarningsCalculator;
import model.MotorPHOvertimePolicy;
import model.DefaultTaxCalculator;
import model.DefaultTaxableBenefitsPolicy;
import model.DefaultWorkHoursCalculator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.util.Vector;

public class EmployeeManagementPanel extends JPanel {

    private final Color gradientStart = new Color(255, 204, 229);
    private final Color gradientEnd   = new Color(255, 229, 180);

    private final EmployeeRepository employeeRepo;
    private final AttendanceRepository attendanceRepo;
    private final PayrollService payrollService;

    private final EmployeeTable dashboardTable;

    private JButton viewButton;
    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;

    private User currentUser;

    // ==========================================================
    // Default constructor (proper wiring, NO duplicate repos)
    // ==========================================================
    public EmployeeManagementPanel() {
        this(createDefaultEmployeeRepo(),
             createDefaultAttendanceRepo(),
             null /* will be created below */);
    }

    // ==========================================================
    // Injection constructor
    // ==========================================================
    public EmployeeManagementPanel(EmployeeRepository employeeRepo,
                         AttendanceRepository attendanceRepo,
                         PayrollService payrollService) {

        if (employeeRepo == null) throw new IllegalArgumentException("employeeRepo is null");
        if (attendanceRepo == null) throw new IllegalArgumentException("attendanceRepo is null");

        this.employeeRepo = employeeRepo;
        this.attendanceRepo = attendanceRepo;

        // Load once
        this.employeeRepo.load();
        this.attendanceRepo.load();

        // If no payrollService provided, build default one using SAME repos
        if (payrollService == null) {
            this.payrollService = createDefaultPayrollService(this.employeeRepo, this.attendanceRepo);
        } else {
            this.payrollService = payrollService;
        }

        setLayout(new BorderLayout());
        setOpaque(false);

        // table must be final -> assign here
        this.dashboardTable = new EmployeeTable(this.employeeRepo);

        initUI();
        wireEvents();
        refreshEmployeeTable();
    }

    // ---------------- UI setup ----------------
    private void initUI() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(20, 50, 0, 50));

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);

        viewButton = new JButton("View Employee");
        styleMinimalButton(viewButton, 140, 36);
        rightPanel.add(viewButton);

        addButton = new JButton("Add Employee");
        styleMinimalButton(addButton, 140, 36);
        rightPanel.add(addButton);

        topPanel.add(rightPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        dashboardTable.setBorder(new EmptyBorder(20, 50, 10, 50));
        add(dashboardTable, BorderLayout.CENTER);

        JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomButtonPanel.setOpaque(false);
        bottomButtonPanel.setBorder(new EmptyBorder(10, 50, 20, 50));

        updateButton = new JButton("Update");
        styleColoredButton(updateButton, Color.BLACK, 120, 36);
        updateButton.setEnabled(false);
        bottomButtonPanel.add(updateButton);

        deleteButton = new JButton("Delete");
        styleColoredButton(deleteButton, new Color(220, 20, 60), 120, 36);
        deleteButton.setEnabled(false);
        bottomButtonPanel.add(deleteButton);

        add(bottomButtonPanel, BorderLayout.SOUTH);
    }

    private void wireEvents() {
        dashboardTable.getTable().getSelectionModel().addListSelectionListener(e -> {
            boolean isSelected = dashboardTable.getTable().getSelectedRow() != -1;
            updateButton.setEnabled(isSelected && canManage());
            deleteButton.setEnabled(isSelected && canManage());
        });

        viewButton.addActionListener(e -> showSelectedEmployeeDetails());

        addButton.addActionListener(e -> {
            if (!canManage()) {
                showCustomMessage("Access denied.", "Permission");
                return;
            }
            showAddEmployeeDialog();
        });

        updateButton.addActionListener(e -> {
            if (!canManage()) {
                showCustomMessage("Access denied.", "Permission");
                return;
            }
            showUpdateDialog(); // ✅ must exist below
        });

        deleteButton.addActionListener(e -> {
            if (!canManage()) {
                showCustomMessage("Access denied.", "Permission");
                return;
            }
            showDeleteDialog(); // ✅ must exist below
        });
    }

    // ---------------- Access ----------------
    public void applyAccess(User user) {
        this.currentUser = user;
        boolean canManage = canManage();

        viewButton.setVisible(true);
        viewButton.setEnabled(true);

        addButton.setVisible(canManage);
        addButton.setEnabled(canManage);

        updateButton.setVisible(canManage);
        deleteButton.setVisible(canManage);

        if (!canManage) {
            updateButton.setEnabled(false);
            deleteButton.setEnabled(false);
        }
    }

    private boolean canManage() {
        return currentUser != null && currentUser.canManageEmployees();
    }

    // ---------------- Table refresh ----------------
    private void refreshEmployeeTable() {
        employeeRepo.load();
        dashboardTable.refreshTable(employeeRepo.getRows());
    }

    // ---------------- Actions ----------------
    private void showSelectedEmployeeDetails() {
        Vector<Object> selected = dashboardTable.getSelectedEmployeeFullDetails();
        if (selected == null || selected.isEmpty()) {
            showCustomMessage("Please select an employee first.", "Message");
            return;
        }

        new ViewEmployeePanel(selected, employeeRepo, attendanceRepo, payrollService);
    }

    private void showAddEmployeeDialog() {
        JFrame frame = new JFrame("Add Employee");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(550, 600);
        frame.setLocationRelativeTo(null);

        frame.add(new AddEmployeePanel(employeeRepo, () -> {
            refreshEmployeeTable();
            frame.dispose();
        }));

        frame.setVisible(true);
    }

    // ==========================================================
    // ✅ PUT YOUR EXISTING METHODS HERE (from your original file)
    // ==========================================================
    private void showUpdateDialog() {
        // paste your existing showUpdateDialog() here (employee update version)
    }

    private void showDeleteDialog() {
        // paste your existing showDeleteDialog() here (employee delete version)
    }

    // ---------------- Message UI ----------------
    private void showCustomMessage(String message, String title) {
        JDialog customDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        customDialog.setSize(400, 260);
        customDialog.setLocationRelativeTo(this);
        customDialog.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setPaint(new GradientPaint(0, 0, gradientStart, 0, getHeight(), gradientEnd));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        contentPanel.setBorder(new EmptyBorder(20, 20, 10, 20));
        contentPanel.setLayout(new BorderLayout(10, 10));

        JLabel messageLabel = new JLabel(message, JLabel.CENTER);
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        contentPanel.add(messageLabel, BorderLayout.CENTER);

        JPanel dialogButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        dialogButtonPanel.setOpaque(false);

        JButton okButton = new JButton("OK");
        styleMinimalButton(okButton, 80, 32);
        okButton.addActionListener(e -> customDialog.dispose());
        dialogButtonPanel.add(okButton);

        contentPanel.add(dialogButtonPanel, BorderLayout.SOUTH);

        customDialog.setContentPane(contentPanel);
        customDialog.setVisible(true);
    }

    // ---------------- Button styles ----------------
    private void styleMinimalButton(JButton button, int width, int height) {
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setOpaque(false);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setPreferredSize(new Dimension(width, height));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.BLACK);
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 20, 20);
                super.paint(g2, c);
                g2.dispose();
            }
        });

        button.setMargin(new Insets(0, 15, 0, 15));
    }

    private void styleColoredButton(JButton button, Color bgColor, int width, int height) {
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setOpaque(false);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setPreferredSize(new Dimension(width, height));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 20, 20);
                super.paint(g2, c);
                g2.dispose();
            }
        });

        button.setMargin(new Insets(0, 15, 0, 15));
    }

    // ---------------- Gradient bg ----------------
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setPaint(new GradientPaint(0, 0, gradientStart, 0, getHeight(), gradientEnd));
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    // ==========================================================
    // Dependency builders
    // ==========================================================
    private static EmployeeRepository createDefaultEmployeeRepo() {
        return new FileEmployeeRepository();
    }

    private static AttendanceRepository createDefaultAttendanceRepo() {
        return new FileAttendanceRepository();
    }

    private static PayrollService createDefaultPayrollService(EmployeeRepository er, AttendanceRepository ar) {
       OvertimePolicy overtimePolicy = new MotorPHOvertimePolicy();
        WorkHoursCalculator hoursCalc = new DefaultWorkHoursCalculator(overtimePolicy);

        return new DefaultPayrollService(
                er,
                ar,
                hoursCalc,
                new DefaultEarningsCalculator(),
                new DefaultContributionCalculator(),
                new DefaultTaxCalculator(),
                overtimePolicy,
                new DefaultTaxableBenefitsPolicy()
        );
    }
}
