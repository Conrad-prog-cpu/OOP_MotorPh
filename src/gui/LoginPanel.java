package gui;

import service.AuthService;
import service.AuthenticatedUser;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class LoginPanel extends JFrame {

    private static final int MAX_ATTEMPTS = 3;
    private static final int LOCKOUT_MILLIS = 60_000;
    private static final int SUCCESS_DELAY_MILLIS = 700;

    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JCheckBox showPassword = new JCheckBox("Show Password");
    private final JButton loginButton = new JButton("Login");
    private final JLabel feedbackLabel = new JLabel(" ", SwingConstants.CENTER);

    private final Color originalButtonColor = new Color(0, 191, 255);
    private final ImageIcon backgroundImage =
            new ImageIcon(getClass().getResource("/assets/loginpanel_bg.png"));

    private final AuthService authService;

    private int attempts = 0;
    private Timer lockoutTimer;

    public LoginPanel(AuthService authService) {
        this.authService = authService;

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
        JPanel backgroundPanel = buildBackgroundPanel();
        JPanel card = buildCardPanel();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 20, 2, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        JLabel titleLabel = new JLabel("Sign In", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(Color.DARK_GRAY);
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 20, 10, 20);
        card.add(titleLabel, gbc);
        gbc.gridwidth = 1;

        gbc.gridy++;
        gbc.insets = new Insets(5, 20, 2, 20);
        JLabel userLabel = new JLabel("Username");
        userLabel.setForeground(Color.GRAY);
        card.add(userLabel, gbc);

        gbc.gridy++;
        styleInput(usernameField);
        card.add(usernameField, gbc);

        gbc.gridy++;
        JLabel passLabel = new JLabel("Password");
        passLabel.setForeground(Color.GRAY);
        card.add(passLabel, gbc);

        gbc.gridy++;
        styleInput(passwordField);
        card.add(passwordField, gbc);

        gbc.gridy++;
        showPassword.setFocusPainted(false);
        showPassword.setOpaque(false);
        showPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        showPassword.setForeground(Color.DARK_GRAY);
        showPassword.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        card.add(showPassword, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(10, 20, 10, 20);
        styleLoginButton(loginButton);
        card.add(loginButton, gbc);

        gbc.gridy++;
        feedbackLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        feedbackLabel.setForeground(Color.RED);
        card.add(feedbackLabel, gbc);

        backgroundPanel.add(card);
        setContentPane(backgroundPanel);
    }

    private JPanel buildBackgroundPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Image img = backgroundImage.getImage();
                g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
            }
        };
        panel.setLayout(new GridBagLayout());
        return panel;
    }

    private JPanel buildCardPanel() {
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
        return card;
    }

    private void wireEvents() {
        KeyAdapter enterKeyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    doLogin();
                }
            }
        };

        usernameField.addKeyListener(enterKeyListener);
        passwordField.addKeyListener(enterKeyListener);

        showPassword.addActionListener(e ->
                passwordField.setEchoChar(showPassword.isSelected() ? (char) 0 : '•')
        );

        loginButton.addActionListener(e -> doLogin());
    }

    private void doLogin() {
        if (!loginButton.isEnabled()) {
            return;
        }

        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            setFeedback("Username and password are required.", Color.RED);
            return;
        }

        try {
            AuthenticatedUser authenticatedUser = authService.login(username, password);

            if (authenticatedUser == null) {
                onLoginFailed();
                return;
            }

            onLoginSuccess(authenticatedUser);

        } catch (IllegalArgumentException ex) {
            setFeedback(ex.getMessage(), Color.RED);
        } catch (Exception ex) {
            setFeedback("Login failed. Please try again.", Color.RED);
        }
    }

    private void onLoginSuccess(AuthenticatedUser authenticatedUser) {
        setFeedback("Login Successful!", new Color(34, 139, 34));
        attempts = 0;

        Timer timer = new Timer(SUCCESS_DELAY_MILLIS, e -> {
            dispose();
            SwingUtilities.invokeLater(() ->
                    new DashboardPanel(authenticatedUser).setVisible(true)
            );
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void onLoginFailed() {
        attempts++;
        setFeedback("Incorrect username or password. Attempt " + attempts + " of " + MAX_ATTEMPTS + ".", Color.RED);
        passwordField.setText("");
        usernameField.requestFocus();

        if (attempts >= MAX_ATTEMPTS) {
            lockOut();
        }
    }

    private void lockOut() {
        setFeedback("Too many attempts. Try again after 1 minute.", Color.RED);

        loginButton.setEnabled(false);
        usernameField.setEnabled(false);
        passwordField.setEnabled(false);
        showPassword.setEnabled(false);

        lockoutTimer = new Timer(LOCKOUT_MILLIS, e -> {
            unlockLogin();
            ((Timer) e.getSource()).stop();
        });
        lockoutTimer.setRepeats(false);
        lockoutTimer.start();
    }

    private void unlockLogin() {
        loginButton.setEnabled(true);
        usernameField.setEnabled(true);
        passwordField.setEnabled(true);
        showPassword.setEnabled(true);

        attempts = 0;
        setFeedback(" ", Color.RED);
        usernameField.requestFocus();
    }

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

                if (!button.isEnabled()) {
                    g2.setColor(Color.GRAY);
                } else if (button.getModel().isRollover()) {
                    g2.setColor(new Color(30, 144, 255));
                } else {
                    g2.setColor(originalButtonColor);
                }

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
}