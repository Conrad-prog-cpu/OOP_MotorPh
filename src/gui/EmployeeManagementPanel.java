package gui;

import service.AuthenticatedUser;
import service.EmployeeDetailsDto;
import service.EmployeeRowDto;
import service.EmployeeService;
import service.EmployeeUpdateRequest;
import service.EmployeeValidationResult;
import service.EmployeeValidator;
import service.PayrollService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EmployeeManagementPanel extends JPanel {

    private static final Color GRADIENT_START = new Color(255, 204, 229);
    private static final Color GRADIENT_END = new Color(255, 229, 180);

    private static final String[] EDITABLE_FIELDS = {
            "Last Name", "First Name", "Birthday", "Address", "Phone Number",
            "SSS #", "Philhealth #", "TIN #", "Pag-ibig #", "Status", "Position",
            "Immediate Supervisor", "Basic Salary", "Rice Subsidy", "Phone Allowance",
            "Clothing Allowance", "Gross Semi-monthly Rate", "Hourly Rate"
    };

    private static final Set<String> NUMERIC_UPDATE_FIELDS = Set.of(
            "Phone Number",
            "SSS #",
            "Philhealth #",
            "TIN #",
            "Pag-ibig #"
    );

    private static final Set<String> LOCKED_REGULAR_FIELDS = Set.of(
            "Basic Salary",
            "Rice Subsidy",
            "Phone Allowance",
            "Clothing Allowance",
            "Gross Semi-monthly Rate",
            "Hourly Rate"
    );

    private final EmployeeService employeeService;
    private final PayrollService payrollService;
    private final AuthenticatedUser currentUser;

    private final EmployeeTable dashboardTable;

    private JButton viewButton;
    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;

    public EmployeeManagementPanel(
            EmployeeService employeeService,
            PayrollService payrollService,
            AuthenticatedUser currentUser
    ) {
        this.employeeService = employeeService;
        this.payrollService = payrollService;
        this.currentUser = currentUser;

        setLayout(new BorderLayout());
        setOpaque(false);

        this.dashboardTable = new EmployeeTable();

        initUI();
        wireEvents();
        applyAccess();
        refreshEmployeeTable();
    }

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
            boolean selected = dashboardTable.getTable().getSelectedRow() != -1;
            updateButton.setEnabled(selected && canManage());
            deleteButton.setEnabled(selected && canManage());
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
            showUpdateDialog();
        });

        deleteButton.addActionListener(e -> {
            if (!canManage()) {
                showCustomMessage("Access denied.", "Permission");
                return;
            }
            showDeleteDialog();
        });
    }

    private void applyAccess() {
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
        return currentUser != null
                && currentUser.getRole() != null
                && (
                "HRADMIN".equalsIgnoreCase(currentUser.getRole().name())
                        || "HR".equalsIgnoreCase(currentUser.getRole().name())
        );
    }

    private void refreshEmployeeTable() {
        List<EmployeeRowDto> employees = employeeService.findAllRows();
        dashboardTable.refreshTable(employees);
    }

    private void showSelectedEmployeeDetails() {
        String employeeId = dashboardTable.getSelectedEmployeeId();
        if (employeeId == null || employeeId.isBlank()) {
            showCustomMessage("Please select an employee first.", "Message");
            return;
        }

        new ViewEmployeePanel(employeeId, employeeService, payrollService, currentUser).setVisible(true);
    }

    private void showAddEmployeeDialog() {
        JFrame frame = new JFrame("Add Employee");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(550, 600);
        frame.setLocationRelativeTo(null);

        frame.add(new AddEmployeePanel(employeeService, () -> {
            refreshEmployeeTable();
            frame.dispose();
        }));

        frame.setVisible(true);
    }

    private void showUpdateDialog() {
        String employeeId = dashboardTable.getSelectedEmployeeId();
        if (employeeId == null || employeeId.isBlank()) {
            JOptionPane.showMessageDialog(this, "Please select a row to update.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        EmployeeDetailsDto employee = employeeService.findDetailsById(employeeId);
        if (employee == null) {
            JOptionPane.showMessageDialog(this, "Employee record not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JPanel panel = new JPanel(new SpringLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setPaint(new GradientPaint(0, 0, GRADIENT_START, 0, getHeight(), GRADIENT_END));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        JLabel lblId = new JLabel("Employee ID *:");
        lblId.setForeground(Color.RED);
        JTextField txtId = new JTextField(employee.getEmployeeId(), 20);
        txtId.setEditable(false);
        txtId.setBackground(new Color(240, 240, 240));
        panel.add(lblId);
        panel.add(txtId);

        boolean isRegular = "Regular".equalsIgnoreCase(employee.getStatus());

        JTextField[] textFields = new JTextField[EDITABLE_FIELDS.length];

        for (int i = 0; i < EDITABLE_FIELDS.length; i++) {
            String field = EDITABLE_FIELDS[i];
            String value = employee.getFieldValue(field);

            boolean locked = isLockedField(isRegular, field);

            JLabel label = new JLabel(locked ? field + " *" : field + ":");
            if (locked) {
                label.setForeground(Color.RED);
            }

            JTextField tf = new JTextField(20);
            tf.setEditable(!locked);
            tf.setText(value == null ? "" : value);

            if (locked) {
                tf.setBackground(new Color(240, 240, 240));
                tf.setToolTipText("Locked for Regular employees.");
            }

            if (!locked && isNumericField(field)) {
                restrictNumeric(tf);
                tf.setToolTipText("Numbers only");
            }

            panel.add(label);
            panel.add(tf);
            textFields[i] = tf;
        }

        JLabel note = new JLabel("* Locked field — cannot be edited.");
        note.setForeground(Color.RED);
        panel.add(note);
        panel.add(new JLabel());

        SpringUtilities.makeCompactGrid(
                panel,
                EDITABLE_FIELDS.length + 2,
                2,
                10,
                10,
                10,
                10
        );

        JDialog dialog = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "Update Employee",
                true
        );

        dialog.setSize(650, 750);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.add(new JScrollPane(panel), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);

        JButton saveBtn = new JButton("Save");
        styleMinimalButton(saveBtn, 120, 36);

        saveBtn.addActionListener(evt -> {
            EmployeeUpdateRequest request = buildUpdateRequest(employeeId, textFields);

            EmployeeValidationResult validationResult = EmployeeValidator.validateForUpdate(request);
            if (!validationResult.isValid()) {
                JOptionPane.showMessageDialog(
                        dialog,
                        "Please fix the following:\n- " + String.join("\n- ", validationResult.getMessages()),
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            boolean updated = employeeService.updateEmployee(request);

            if (updated) {
                showCustomMessage("Employee record updated successfully.", "Updated");
                refreshEmployeeTable();
                dialog.dispose();
            } else {
                showCustomMessage("Failed to update employee record.", "Error");
            }
        });

        JButton cancelBtn = new JButton("Cancel");
        styleColoredButton(cancelBtn, Color.GRAY, 100, 36);
        cancelBtn.addActionListener(evt -> dialog.dispose());

        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private EmployeeUpdateRequest buildUpdateRequest(String employeeId, JTextField[] textFields) {
        Map<String, String> values = new LinkedHashMap<>();

        for (int i = 0; i < EDITABLE_FIELDS.length; i++) {
            values.put(EDITABLE_FIELDS[i], textFields[i].getText().trim());
        }

        return new EmployeeUpdateRequest(
                employeeId,
                values.getOrDefault("Last Name", ""),
                values.getOrDefault("First Name", ""),
                parseDate(values.get("Birthday")),
                values.getOrDefault("Address", ""),
                values.getOrDefault("Phone Number", ""),
                values.getOrDefault("SSS #", ""),
                values.getOrDefault("Philhealth #", ""),
                values.getOrDefault("TIN #", ""),
                values.getOrDefault("Pag-ibig #", ""),
                values.getOrDefault("Status", ""),
                values.getOrDefault("Position", ""),
                values.getOrDefault("Immediate Supervisor", ""),
                money(values.get("Basic Salary")),
                money(values.get("Rice Subsidy")),
                money(values.get("Phone Allowance")),
                money(values.get("Clothing Allowance")),
                money(values.get("Gross Semi-monthly Rate")),
                money(values.get("Hourly Rate"))
        );
    }

    private boolean isLockedField(boolean isRegular, String field) {
        return isRegular && LOCKED_REGULAR_FIELDS.contains(field);
    }

    private boolean isNumericField(String field) {
        return NUMERIC_UPDATE_FIELDS.contains(field);
    }

    private void showDeleteDialog() {
        String employeeId = dashboardTable.getSelectedEmployeeId();
        if (employeeId == null || employeeId.isBlank()) {
            showCustomMessage("Please select an employee to delete.", "Message");
            return;
        }

        JDialog dialog = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "Warning",
                true
        );

        dialog.setSize(400, 180);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel content = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setPaint(new GradientPaint(0, 0, GRADIENT_START, 0, getHeight(), GRADIENT_END));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        content.setBorder(new EmptyBorder(20, 20, 20, 20));
        content.setLayout(new BorderLayout(10, 10));

        JLabel message = new JLabel(
                "<html><center>Are you sure you want to delete<br>employee ID <b>" + employeeId + "</b>?</center></html>",
                JLabel.CENTER
        );
        message.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        content.add(message, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setOpaque(false);

        JButton yesButton = new JButton("Yes");
        styleColoredButton(yesButton, new Color(220, 20, 60), 80, 36);
        yesButton.addActionListener(ev -> {
            boolean deleted = employeeService.deleteByEmployeeId(employeeId);
            if (deleted) {
                refreshEmployeeTable();
                showCustomMessage("Record deleted successfully.", "Deleted");
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
                g2d.setPaint(new GradientPaint(0, 0, GRADIENT_START, 0, getHeight(), GRADIENT_END));
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

    private void restrictNumeric(JTextField textField) {
        ((AbstractDocument) textField.getDocument()).setDocumentFilter(new NumericDocumentFilter());
    }

    private BigDecimal money(String value) {
        if (value == null || value.isBlank()) {
            return BigDecimal.ZERO;
        }

        String clean = value.replace("\"", "").replace(",", "").trim();

        try {
            return new BigDecimal(clean);
        } catch (Exception ex) {
            return BigDecimal.ZERO;
        }
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String text = value.trim();

        try {
            return LocalDate.parse(text);
        } catch (Exception ex) {
            try {
                if (text.contains("/")) {
                    String[] parts = text.split("/");
                    return LocalDate.of(
                            Integer.parseInt(parts[0]),
                            Integer.parseInt(parts[1]),
                            Integer.parseInt(parts[2])
                    );
                }
            } catch (Exception ignored) {
            }
        }

        return null;
    }

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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setPaint(new GradientPaint(0, 0, GRADIENT_START, 0, getHeight(), GRADIENT_END));
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    private static class NumericDocumentFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            if (string == null) {
                return;
            }

            if (string.matches("\\d+")) {
                super.insertString(fb, offset, string, attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            if (text == null || text.isEmpty()) {
                super.replace(fb, offset, length, text, attrs);
                return;
            }

            if (text.matches("\\d+")) {
                super.replace(fb, offset, length, text, attrs);
            }
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            super.remove(fb, offset, length);
        }
    }
}