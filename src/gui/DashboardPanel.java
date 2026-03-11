package gui;

import service.AuthService;
import service.AuthenticatedUser;
import service.EmployeeService;
import service.LeaveService;
import service.PayrollService;
import service.UserAccountService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

public class DashboardPanel extends JFrame {

    private static final Color SIDEBAR_COLOR = Color.WHITE;
    private static final Color GRADIENT_START = new Color(255, 204, 229);
    private static final Color GRADIENT_END = new Color(255, 229, 180);
    private static final Color NAV_HOVER_COLOR = new Color(240, 240, 240);

    private static final Font BOLD_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font REGULAR_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    private static final String CARD_EMPLOYEE = "Employee";
    private static final String CARD_LEAVE_REQUEST = "LeaveRequest";
    private static final String CARD_LEAVE_APPROVALS = "LeaveApprovals";
    private static final String CARD_LEAVE_HISTORY = "LeaveHistory";
    private static final String CARD_USER_ACCOUNTS = "UserAccounts";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new GradientPanel();

    private final AuthenticatedUser currentUser;
    private final AuthService authService;
    private final EmployeeService employeeService;
    private final UserAccountService userAccountService;
    private final LeaveService leaveService;
    private final PayrollService payrollService;

    private final EmployeeManagementPanel employeePanel;
    private final LeaveRequestPanel leaveRequestPanel;
    private final LeaveApprovalPanel leaveApprovalPanel;
    private final EmployeeLeaveHistoryPanel leaveHistoryPanel;
    private final UserAccountsPanel userAccountsPanel;

    public DashboardPanel(
            AuthenticatedUser currentUser,
            AuthService authService,
            EmployeeService employeeService,
            UserAccountService userAccountService,
            LeaveService leaveService,
            PayrollService payrollService
    ) {
        this.currentUser = currentUser;
        this.authService = authService;
        this.employeeService = employeeService;
        this.userAccountService = userAccountService;
        this.leaveService = leaveService;
        this.payrollService = payrollService;

        this.employeePanel = new EmployeeManagementPanel(employeeService, payrollService, currentUser);
        this.leaveRequestPanel = new LeaveRequestPanel(leaveService, currentUser);
        this.leaveApprovalPanel = new LeaveApprovalPanel(leaveService, currentUser);
        this.leaveHistoryPanel = new EmployeeLeaveHistoryPanel(leaveService, currentUser);
        this.userAccountsPanel = new UserAccountsPanel(userAccountService, employeeService);

        setupFrame();
        buildUI();
    }

    private void setupFrame() {
        setTitle("MotorPH Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    private void buildUI() {
        String role = getCurrentRoleName();

        JPanel sidebar = buildSidebar(role);
        configureContentPanel(role);

        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        showDefaultPage(role);
    }

    private JPanel buildSidebar(String role) {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(SIDEBAR_COLOR);
        sidebar.setPreferredSize(new Dimension(280, getHeight()));
        sidebar.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel profilePanel = buildProfilePanel();
        JPanel navigationPanel = buildNavigationPanel(role);
        JButton logoutButton = buildLogoutButton();

        sidebar.add(profilePanel, BorderLayout.NORTH);
        sidebar.add(navigationPanel, BorderLayout.CENTER);
        sidebar.add(logoutButton, BorderLayout.SOUTH);

        return sidebar;
    }

    private JPanel buildProfilePanel() {
        JPanel profilePanel = new JPanel(new BorderLayout(10, 0));
        profilePanel.setBackground(SIDEBAR_COLOR);

        JLabel profileIcon = new JLabel(loadImageIcon("/assets/userprofile.png", 40, 40));
        profilePanel.add(profileIcon, BorderLayout.WEST);

        JPanel namePanel = new JPanel();
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));
        namePanel.setBackground(SIDEBAR_COLOR);

        JLabel userName = new JLabel(safe(currentUser.getFirstName(), "(Unknown)"));
        JLabel userRole = new JLabel(safe(currentUser.getPosition(), "(Unknown)"));

        userName.setFont(BOLD_FONT);
        userRole.setFont(REGULAR_FONT);
        userRole.setForeground(Color.GRAY);

        namePanel.add(userName);
        namePanel.add(userRole);

        profilePanel.add(namePanel, BorderLayout.CENTER);

        return profilePanel;
    }

    private JPanel buildNavigationPanel(String role) {
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBackground(SIDEBAR_COLOR);
        navPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        navPanel.add(Box.createVerticalStrut(30));

        JLabel generalLabel = new JLabel("General");
        generalLabel.setFont(BOLD_FONT);
        generalLabel.setBorder(new EmptyBorder(0, 10, 10, 0));
        generalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        navPanel.add(generalLabel);

        if (isEmployeeModuleAllowed(role)) {
            navPanel.add(createNavigationButton("Employee", "employee.png", CARD_EMPLOYEE));
            navPanel.add(Box.createVerticalStrut(5));
        }

        navPanel.add(createNavigationButton("Leave Request", "employee.png", CARD_LEAVE_REQUEST));
        navPanel.add(Box.createVerticalStrut(5));

        if (isLeaveApprovalAllowed(role)) {
            navPanel.add(createNavigationButton("Leave Approvals", "employee.png", CARD_LEAVE_APPROVALS));
            navPanel.add(Box.createVerticalStrut(5));
        }

        navPanel.add(createNavigationButton("My Leaves", "employee.png", CARD_LEAVE_HISTORY));
        navPanel.add(Box.createVerticalStrut(5));

        if (isUserAccountsAllowed(role)) {
            navPanel.add(createNavigationButton("User Accounts", "employee.png", CARD_USER_ACCOUNTS));
            navPanel.add(Box.createVerticalStrut(5));
        }

        return navPanel;
    }

    private JButton buildLogoutButton() {
        JButton logoutButton = createNavButton("Log-out", "logout.png");
        logoutButton.setForeground(Color.GRAY);
        logoutButton.addActionListener(e -> logout());
        return logoutButton;
    }

    private void configureContentPanel(String role) {
        contentPanel.setLayout(cardLayout);

        contentPanel.add(leaveRequestPanel, CARD_LEAVE_REQUEST);
        contentPanel.add(leaveHistoryPanel, CARD_LEAVE_HISTORY);

        if (isEmployeeModuleAllowed(role)) {
            contentPanel.add(employeePanel, CARD_EMPLOYEE);
        }

        if (isLeaveApprovalAllowed(role)) {
            contentPanel.add(leaveApprovalPanel, CARD_LEAVE_APPROVALS);
        }

        if (isUserAccountsAllowed(role)) {
            contentPanel.add(userAccountsPanel, CARD_USER_ACCOUNTS);
        }
    }

    private JButton createNavigationButton(String text, String iconFileName, String cardName) {
        JButton button = createNavButton(text, iconFileName);
        button.addActionListener(e -> showCard(cardName));
        return button;
    }

    private void showCard(String cardName) {
        cardLayout.show(contentPanel, cardName);
    }

    private void showDefaultPage(String role) {
        if (isUserAccountsAllowed(role)) {
            showCard(CARD_USER_ACCOUNTS);
        } else if (isEmployeeModuleAllowed(role)) {
            showCard(CARD_EMPLOYEE);
        } else {
            showCard(CARD_LEAVE_REQUEST);
        }
    }

    private void logout() {
        dispose();

        SwingUtilities.invokeLater(() ->
                new LoginPanel(
                        authService,
                        employeeService,
                        userAccountService,
                        leaveService,
                        payrollService
                ).setVisible(true)
        );
    }

    private boolean isEmployeeModuleAllowed(String role) {
        return "EMPLOYEE".equals(role) || "HRADMIN".equals(role) || "HR".equals(role);
    }

    private boolean isLeaveApprovalAllowed(String role) {
        return "HRADMIN".equals(role);
    }

    private boolean isUserAccountsAllowed(String role) {
        return "IT".equals(role);
    }

    private String getCurrentRoleName() {
        return currentUser == null || currentUser.getRole() == null
                ? ""
                : currentUser.getRole().name();
    }

    private String safe(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private JButton createNavButton(String text, String iconFileName) {
        JButton button = new JButton(text);
        button.setIcon(loadImageIcon("/assets/" + iconFileName, 20, 20));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setIconTextGap(15);
        button.setFont(REGULAR_FONT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(NAV_HOVER_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(Color.WHITE);
            }
        });

        return button;
    }

    private ImageIcon loadImageIcon(String path, int width, int height) {
        URL imageUrl = getClass().getResource(path);
        if (imageUrl == null) {
            System.err.println("Image not found: " + path);
            return null;
        }

        ImageIcon icon = new ImageIcon(imageUrl);
        Image scaled = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private static class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g;
            g2d.setPaint(new GradientPaint(0, 0, GRADIENT_START, 0, getHeight(), GRADIENT_END));
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}