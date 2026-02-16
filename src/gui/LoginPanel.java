package gui;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import model.Employee;
import model.Role;
import model.User;
import model.UserAccount;

import repository.CredentialRepository;
import repository.EmployeeRepository;
import repository.FileCredentialRepository;
import repository.FileEmployeeRepository;

import java.util.Optional;

public class LoginPanel extends JFrame {

    // UI fields
    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JCheckBox showPassword = new JCheckBox("Show Password");
    private final JButton loginButton = new JButton("Login");
    private final JLabel feedbackLabel = new JLabel(" ", SwingConstants.CENTER);

    // Lockout
    private int attempts = 0;
    private Timer lockoutTimer;

    // Styling
    private final Color originalButtonColor = new Color(0, 191, 255);
    private final ImageIcon backgroundImage =
            new ImageIcon(getClass().getResource("/assets/loginpanel_bg.png"));

    // ✅ Dependencies (repositories)
    private final CredentialRepository credentialRepo;
    private final EmployeeRepository employeeRepo;

    public LoginPanel() {
        // default repos (file-based)
        this(new FileCredentialRepository(), new FileEmployeeRepository());
    }

    public LoginPanel(CredentialRepository credentialRepo, EmployeeRepository employeeRepo) {
        this.credentialRepo = credentialRepo;
        this.employeeRepo = employeeRepo;

        setupFrame();
        buildUI();
        wireEvents();

        setVisible(true);
    }

    private void setupFrame() {
        setTitle("MotorPH Payroll System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
    }

    private void buildUI() {
        // Background panel
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Image img = backgroundImage.getImage();
                g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
            }
        };
        backgroundPanel.setLayout(new GridBagLayout());
        setContentPane(backgroundPanel);

        // Card panel with rounded edges
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(360, 340));
        card.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 20, 2, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        // Title
        JLabel titleLabel = new JLabel("Sign In", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(Color.DARK_GRAY);
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 20, 10, 20);
        card.add(titleLabel, gbc);
        gbc.gridwidth = 1;

        // Username label
        gbc.gridy++;
        gbc.insets = new Insets(5, 20, 2, 20);
        JLabel userLabel = new JLabel("Username");
        userLabel.setForeground(Color.GRAY);
        card.add(userLabel, gbc);

        // Username input
        gbc.gridy++;
        styleInput(usernameField);
        card.add(usernameField, gbc);

        // Password label
        gbc.gridy++;
        JLabel passLabel = new JLabel("Password");
        passLabel.setForeground(Color.GRAY);
        card.add(passLabel, gbc);

        // Password input
        gbc.gridy++;
        styleInput(passwordField);
        card.add(passwordField, gbc);

        // Show password checkbox
        gbc.gridy++;
        showPassword.setFocusPainted(false);
        showPassword.setOpaque(false);
        showPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        showPassword.setForeground(Color.DARK_GRAY);
        showPassword.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        card.add(showPassword, gbc);

        // Login button
        gbc.gridy++;
        gbc.insets = new Insets(10, 20, 10, 20);
        styleLoginButton(loginButton);
        card.add(loginButton, gbc);

        // Feedback label
        gbc.gridy++;
        feedbackLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        feedbackLabel.setForeground(Color.RED);
        card.add(feedbackLabel, gbc);

        backgroundPanel.add(card);
    }

    private void wireEvents() {
        // Enter triggers login
        KeyAdapter enterKeyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) doLogin();
            }
        };
        usernameField.addKeyListener(enterKeyListener);
        passwordField.addKeyListener(enterKeyListener);

        // Show/hide password
        showPassword.addActionListener(e ->
                passwordField.setEchoChar(showPassword.isSelected() ? (char) 0 : '•')
        );

        // Button click
        loginButton.addActionListener(e -> doLogin());
    }

    private void doLogin() {
        if (!loginButton.isEnabled()) return;

        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            setFeedback("Username and password are required.", Color.RED);
            return;
        }

        // ✅ load repositories (important if they cache)
        employeeRepo.load();

        // ✅ validate credentials
        UserAccount account = credentialRepo.validate(username, password);
        if (account == null) {
            onLoginFailed();
            return;
        }

        // ✅ map to employee
        Optional<Employee> empOpt = employeeRepo.findById(account.getEmployeeNumber());

        String firstName = "(Unknown)";
        String position = "(Unknown)";

        if (empOpt.isPresent()) {
            Employee emp = empOpt.get();
            firstName = safe(emp.getFirstName(), "(Unknown)");
            position = safe(emp.getPosition(), "(Unknown)");
        }

        // ✅ build logged-in user object
        User loggedInUser = new User(
                account.getCredentialId(),
                account.getUsername(),
                account.getRole(),
                account.getEmployeeNumber(),
                firstName,
                position
        );

        onLoginSuccess(loggedInUser);
    }

    private void onLoginSuccess(User user) {
        setFeedback("Login Successful!", new Color(34, 139, 34));
        attempts = 0;

        Timer t = new Timer(700, e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new DashboardPanel(user).setVisible(true));
        });
        t.setRepeats(false);
        t.start();
    }

    private void onLoginFailed() {
        attempts++;
        setFeedback("Incorrect username or password. Attempt " + attempts + " of 3.", Color.RED);
        usernameField.setText("");
        passwordField.setText("");
        usernameField.requestFocus();

        if (attempts >= 3) {
            lockOut();
        }
    }

    private void lockOut() {
        setFeedback("Too many attempts. Try again after 1 minute.", Color.RED);

        loginButton.setEnabled(false);
        usernameField.setEnabled(false);
        passwordField.setEnabled(false);
        showPassword.setEnabled(false);

        lockoutTimer = new Timer(60000, e -> {
            loginButton.setEnabled(true);
            usernameField.setEnabled(true);
            passwordField.setEnabled(true);
            showPassword.setEnabled(true);

            attempts = 0;
            setFeedback(" ", Color.RED);
            usernameField.requestFocus();
            ((Timer) e.getSource()).stop();
        });
        lockoutTimer.setRepeats(false);
        lockoutTimer.start();
    }

    // ---------- Styling helpers ----------

    private void styleInput(JTextField field) {
        field.setBorder(new MatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        field.setOpaque(false);
        field.setPreferredSize(new Dimension(200, 28));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    }

    private void styleLoginButton(JButton button) {
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (!button.isEnabled()) g2.setColor(Color.GRAY);
                else if (button.getModel().isRollover()) g2.setColor(new Color(30, 144, 255));
                else g2.setColor(originalButtonColor);

                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 20, 20);
                super.paint(g2, c);
                g2.dispose();
            }
        });
    }

    private void setFeedback(String msg, Color c) {
        feedbackLabel.setForeground(c);
        feedbackLabel.setText("<html><div align='center'>" + msg + "</div></html>");
    }

    private String safe(String v, String fallback) {
        return (v == null || v.trim().isEmpty()) ? fallback : v.trim();
    }
}
