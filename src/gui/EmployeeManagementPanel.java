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

        int selectedRow = dashboardTable.getTable().getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "⚠ Please select a row to update.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        selectedRow = dashboardTable.getTable().convertRowIndexToModel(selectedRow);
        String employeeId = dashboardTable.getTable().getValueAt(selectedRow, 0).toString();

        String[] fullRow = employeeRepo.findRowByEmployeeNo(employeeId);
        if (fullRow == null) {
            JOptionPane.showMessageDialog(this, "❌ Employee record not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String[] editableFields = {
                "Last Name", "First Name", "Birthday", "Address", "Phone Number",
                "SSS #", "Philhealth #", "TIN #", "Pag-ibig #", "Status", "Position",
                "Immediate Supervisor", "Basic Salary", "Rice Subsidy", "Phone Allowance",
                "Clothing Allowance", "Gross Semi-monthly Rate", "Hourly Rate"
        };

        java.util.List<String> headers = employeeRepo.getHeaders();

        JPanel panel = new JPanel(new SpringLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setPaint(new GradientPaint(0, 0, gradientStart, 0, getHeight(), gradientEnd));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        // Employee ID locked
        JLabel lblId = new JLabel("Employee ID *:");
        lblId.setForeground(Color.RED);
        JTextField txtId = new JTextField(employeeId, 20);
        txtId.setEditable(false);
        txtId.setBackground(new Color(240, 240, 240));
        panel.add(lblId);
        panel.add(txtId);

        int statusIndex = headers.indexOf("Status");
        boolean isRegular = statusIndex != -1 &&
                statusIndex < fullRow.length &&
                fullRow[statusIndex].equalsIgnoreCase("Regular");

        JTextField[] textFields = new JTextField[editableFields.length];
        String[] originalValues = new String[editableFields.length];

        for (int i = 0; i < editableFields.length; i++) {

            String field = editableFields[i];
            int idx = headers.indexOf(field);

            boolean locked = isRegular && (
                    field.equals("Basic Salary") ||
                    field.equals("Rice Subsidy") ||
                    field.equals("Phone Allowance") ||
                    field.equals("Clothing Allowance") ||
                    field.equals("Gross Semi-monthly Rate") ||
                    field.equals("Hourly Rate")
            );

            JLabel label = new JLabel(locked ? field + " *" : field + ":");
            if (locked) label.setForeground(Color.RED);

            JTextField tf = new JTextField(20);
            tf.setEditable(!locked);

            if (idx != -1 && idx < fullRow.length) {
                tf.setText(fullRow[idx]);
                originalValues[i] = fullRow[idx];
            } else {
                originalValues[i] = "";
            }

            if (locked) {
                tf.setBackground(new Color(240, 240, 240));
                tf.setToolTipText("🔒 Locked for Regular employees.");
            }

            panel.add(label);
            panel.add(tf);
            textFields[i] = tf;
            }

            JLabel note = new JLabel("* Locked field — cannot be edited.");
            note.setForeground(Color.RED);
            panel.add(note);
            panel.add(new JLabel());

            SpringUtilities.makeCompactGrid(panel,
                    editableFields.length + 2, 2,
                    10, 10, 10, 10);

            JDialog dialog = new JDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this),
                    "Update Employee", true);

            dialog.setSize(650, 750);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout());
            dialog.add(new JScrollPane(panel), BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setOpaque(false);

            JButton saveBtn = new JButton("Save");
            styleMinimalButton(saveBtn, 120, 36);

            saveBtn.addActionListener(evt -> {

                boolean anyChanged = false;

                for (int i = 0; i < editableFields.length; i++) {
                    JTextField tf = textFields[i];
                    if (!tf.isEditable()) continue;

                    String newVal = tf.getText().trim();
                    String oldVal = originalValues[i] == null ? "" : originalValues[i];

                    if (!newVal.equals(oldVal)) {
                        boolean ok = employeeRepo.updateField(employeeId, editableFields[i], newVal);
                        anyChanged |= ok;
                    }
                }

                if (anyChanged) {
                    showCustomMessage("Employee record updated successfully.", "Updated");
                    refreshEmployeeTable();
                } else {
                    showCustomMessage("No changes were made.", "Message");
                }

                dialog.dispose();
            });

            JButton cancelBtn = new JButton("Cancel");
            styleColoredButton(cancelBtn, Color.GRAY, 100, 36);
            cancelBtn.addActionListener(evt -> dialog.dispose());

            buttonPanel.add(saveBtn);
            buttonPanel.add(cancelBtn);

            dialog.add(buttonPanel, BorderLayout.SOUTH);
            dialog.setVisible(true);
        }
           private void showDeleteDialog() {

        int selectedRow = dashboardTable.getTable().getSelectedRow();
        if (selectedRow == -1) {
            showCustomMessage("Please select an employee to delete.", "Message");
            return;
        }

        selectedRow = dashboardTable.getTable().convertRowIndexToModel(selectedRow);
        String employeeId = dashboardTable.getTable().getValueAt(selectedRow, 0).toString();

        JDialog dialog = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "Warning", true);

        dialog.setSize(400, 180);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel content = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setPaint(new GradientPaint(0, 0, gradientStart, 0, getHeight(), gradientEnd));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        content.setBorder(new EmptyBorder(20, 20, 20, 20));
        content.setLayout(new BorderLayout(10, 10));

        JLabel message = new JLabel(
                "<html><center>Are you sure you want to delete<br>employee ID <b>" + employeeId + "</b>?</center></html>",
                JLabel.CENTER);

        message.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        content.add(message, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setOpaque(false);

        JButton yesButton = new JButton("Yes");
        styleColoredButton(yesButton, new Color(220, 20, 60), 80, 36);

        yesButton.addActionListener(ev -> {
            boolean ok = employeeRepo.deleteByEmployeeNo(employeeId);
            if (ok) {
                refreshEmployeeTable();
                showCustomMessage("Record Deleted Successfully", "Deleted");
            } else {
                showCustomMessage("Failed to delete record.", "Error");
            }
            dialog.dispose();
        });

        JButton noButton = new JButton("No");
        styleColoredButton(noButton, Color.GRAY, 80, 36);
        noButton.addActionListener(ev -> dialog.dispose());

        buttonPanel.add(yesButton);
        buttonPanel.add(noButton);

        content.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setContentPane(content);
        dialog.setVisible(true);
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
