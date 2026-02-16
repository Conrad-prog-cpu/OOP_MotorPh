package gui;

import repository.CredentialRepository;
import repository.EmployeeRepository;

import model.Role;
import model.UserAccount;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;

public class UserManagementPanel extends JFrame {

    private final CredentialRepository credentialRepo;
    private final EmployeeRepository employeeRepo;

    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JComboBox<String> roleComboBox;
    private final JTextField employeeNumberField;

    private final JButton addButton;
    private final JButton updatePasswordButton;
    private final JButton updateRoleButton;
    private final JButton updateEmployeeNoButton;

    private final JLabel feedbackLabel;

    public UserManagementPanel(CredentialRepository credentialRepo, EmployeeRepository employeeRepo) {
        this.credentialRepo = credentialRepo;
        this.employeeRepo = employeeRepo;

        setTitle("User Management");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(520, 520);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());

        // Load employee cache (for employee# validation)
        this.employeeRepo.load();

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.dispose();
            }
        };

        card.setOpaque(false);
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(420, 440));
        card.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 20, 2, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridwidth = 1;

        // Title
        JLabel titleLabel = new JLabel("Manage Users", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(Color.DARK_GRAY);
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        card.add(titleLabel, gbc);
        gbc.gridwidth = 1;

        // Username
        gbc.gridy++;
        JLabel userLabel = new JLabel("Username");
        userLabel.setForeground(Color.GRAY);
        card.add(userLabel, gbc);

        gbc.gridy++;
        usernameField = new JTextField();
        styleField(usernameField);
        card.add(usernameField, gbc);

        // Password
        gbc.gridy++;
        JLabel passLabel = new JLabel("Password");
        passLabel.setForeground(Color.GRAY);
        card.add(passLabel, gbc);

        gbc.gridy++;
        passwordField = new JPasswordField();
        styleField(passwordField);
        card.add(passwordField, gbc);

        // Role
        gbc.gridy++;
        JLabel roleLabel = new JLabel("Role");
        roleLabel.setForeground(Color.GRAY);
        card.add(roleLabel, gbc);

        gbc.gridy++;
        roleComboBox = new JComboBox<>(new String[]{"EMPLOYEE", "HRADMIN", "IT"});
        roleComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        card.add(roleComboBox, gbc);

        // Employee #
        gbc.gridy++;
        JLabel empNoLabel = new JLabel("Employee # (must exist in employee.txt)");
        empNoLabel.setForeground(Color.GRAY);
        card.add(empNoLabel, gbc);

        gbc.gridy++;
        employeeNumberField = new JTextField();
        styleField(employeeNumberField);
        employeeNumberField.setToolTipText("Example: 10001");
        card.add(employeeNumberField, gbc);

        // Feedback label
        gbc.gridy++;
        feedbackLabel = new JLabel(" ", SwingConstants.CENTER);
        feedbackLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        feedbackLabel.setForeground(Color.RED);
        card.add(feedbackLabel, gbc);

        // Buttons
        gbc.gridy++;
        addButton = createStyledButton("Add User");
        card.add(addButton, gbc);

        gbc.gridy++;
        updatePasswordButton = createStyledButton("Update Password");
        card.add(updatePasswordButton, gbc);

        gbc.gridy++;
        updateRoleButton = createStyledButton("Update Role");
        card.add(updateRoleButton, gbc);

        gbc.gridy++;
        updateEmployeeNoButton = createStyledButton("Update Employee #");
        card.add(updateEmployeeNoButton, gbc);

        add(card);
        setVisible(true);

        wireEvents();
    }

    // Convenience constructor if you want (uncomment and adjust if you have these repo classes)
    // public UserManagementPanel() {
    //     this(new FileCredentialRepository(), new FileEmployeeRepository());
    // }

    // ==========================
    // EVENTS
    // ==========================
    private void wireEvents() {

        addButton.addActionListener(e -> {
            String user = usernameField.getText().trim();
            String pass = new String(passwordField.getPassword()).trim();
            String roleStr = roleComboBox.getSelectedItem().toString();
            String employeeNo = employeeNumberField.getText().trim();

            if (user.isEmpty() || pass.isEmpty() || employeeNo.isEmpty()) {
                showError("Username, password, and Employee # are required.");
                return;
            }

            // Ensure employee exists
            if (employeeRepo.findRowByEmployeeNo(employeeNo) == null) {
                showError("Employee # not found in employee.txt.");
                return;
            }

            // Ensure username doesn't exist
            if (credentialRepo.findByUsername(user) != null) {
                showError("Username already exists.");
                return;
            }

            Role role = Role.from(roleStr);

            boolean ok = credentialRepo.add(user, pass, role, employeeNo);
            if (ok) {
                showSuccess("User added successfully.");
                clearInputs();
            } else {
                showError("Failed to add user.");
            }
        });

        updatePasswordButton.addActionListener(e -> {
            String user = usernameField.getText().trim();
            String pass = new String(passwordField.getPassword()).trim();

            if (user.isEmpty() || pass.isEmpty()) {
                showError("Username and new password are required.");
                return;
            }

            UserAccount acc = credentialRepo.findByUsername(user);
            if (acc == null) {
                showError("User not found.");
                return;
            }

            boolean ok = credentialRepo.updatePassword(user, pass);
            if (ok) {
                showSuccess("Password updated.");
                passwordField.setText("");
            } else {
                showError("Failed to update password.");
            }
        });

        updateRoleButton.addActionListener(e -> {
            String user = usernameField.getText().trim();
            if (user.isEmpty()) {
                showError("Username is required.");
                return;
            }

            UserAccount acc = credentialRepo.findByUsername(user);
            if (acc == null) {
                showError("User not found.");
                return;
            }

            Role newRole = Role.from(roleComboBox.getSelectedItem().toString());
            boolean ok = credentialRepo.updateRole(user, newRole);

            if (ok) {
                showSuccess("Role updated.");
            } else {
                showError("Failed to update role.");
            }
        });

        updateEmployeeNoButton.addActionListener(e -> {
            String user = usernameField.getText().trim();
            String newEmpNo = employeeNumberField.getText().trim();

            if (user.isEmpty() || newEmpNo.isEmpty()) {
                showError("Username and Employee # are required.");
                return;
            }

            UserAccount acc = credentialRepo.findByUsername(user);
            if (acc == null) {
                showError("User not found.");
                return;
            }

            // Ensure employee exists
            if (employeeRepo.findRowByEmployeeNo(newEmpNo) == null) {
                showError("Employee # not found in employee.txt.");
                return;
            }

            boolean ok = credentialRepo.updateEmployeeNo(user, newEmpNo);
            if (ok) {
                showSuccess("Employee # updated.");
            } else {
                showError("Failed to update Employee #.");
            }
        });
    }

    // ==========================
    // UI HELPERS
    // ==========================
    private void styleField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setOpaque(false);
        field.setBorder(new MatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
    }

    private void showError(String msg) {
        feedbackLabel.setForeground(Color.RED);
        feedbackLabel.setText(msg);
    }

    private void showSuccess(String msg) {
        feedbackLabel.setForeground(new Color(34, 139, 34));
        feedbackLabel.setText(msg);
    }

    private void clearInputs() {
        usernameField.setText("");
        passwordField.setText("");
        employeeNumberField.setText("");
        roleComboBox.setSelectedIndex(0);
    }

    // Button design copied from LoginPanel
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (!isEnabled()) {
                    g2.setColor(Color.GRAY);
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(30, 144, 255));
                } else {
                    g2.setColor(new Color(0, 191, 255));
                }

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {}
        };

        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
}
