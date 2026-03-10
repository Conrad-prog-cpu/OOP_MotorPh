package gui;

import service.AuthService;
import service.AuthenticatedUser;
import service.UserAccountService;
import service.EmployeeService;
import service.LeaveService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

public class DashboardPanel extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel;

    private final AuthenticatedUser currentUser;
    private final AuthService authService;
    private final EmployeeService employeeService;
    private final UserAccountService userAccountService;
    private final LeaveService leaveService;

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
            LeaveService leaveService
    ) {
        this.currentUser = currentUser;
        this.authService = authService;
        this.employeeService = employeeService;
        this.userAccountService = userAccountService;
        this.leaveService = leaveService;

        setTitle("MotorPH Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(800, 500));

        Color sidebarColor = Color.WHITE;
        Color gradientStart = new Color(255, 204, 229);
        Color gradientEnd = new Color(255, 229, 180);
        Font boldFont = new Font("Segoe UI", Font.BOLD, 16);
        Font regularFont = new Font("Segoe UI", Font.PLAIN, 14);

        this.employeePanel = new EmployeeManagementPanel(employeeService, currentUser);
        this.leaveRequestPanel = new LeaveRequestPanel(leaveService, currentUser);
        this.leaveApprovalPanel = new LeaveApprovalPanel(leaveService, currentUser);
        this.leaveHistoryPanel = new EmployeeLeaveHistoryPanel(leaveService, currentUser);
        this.userAccountsPanel = new UserAccountsPanel(userAccountService, employeeService);

        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(sidebarColor);
        sidebar.setPreferredSize(new Dimension(280, getHeight()));
        sidebar.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel profilePanel = buildProfilePanel(sidebarColor, boldFont, regularFont);

        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBackground(sidebarColor);
        navPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        navPanel.add(Box.createVerticalStrut(30));

        JLabel generalLabel = new JLabel("General");
        generalLabel.setFont(boldFont);
        generalLabel.setBorder(new EmptyBorder(0, 10, 10, 0));
        generalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        navPanel.add(generalLabel);

        contentPanel = new JPanel(cardLayout) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setPaint(new GradientPaint(0, 0, gradientStart, 0, getHeight(), gradientEnd));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        contentPanel.add(leaveRequestPanel, "LeaveRequest");
        contentPanel.add(leaveHistoryPanel, "LeaveHistory");

        String role = roleName(currentUser);

        if (isEmployeeModuleAllowed(role)) {
            JButton employeeBtn = createNavButton("Employee", "employee.png");
            navPanel.add(employeeBtn);
            navPanel.add(Box.createVerticalStrut(5));

            contentPanel.add(employeePanel, "Employee");
            employeeBtn.addActionListener(e -> cardLayout.show(contentPanel, "Employee"));
        }

        JButton leaveRequestBtn = createNavButton("Leave Request", "employee.png");
        navPanel.add(leaveRequestBtn);
        navPanel.add(Box.createVerticalStrut(5));
        leaveRequestBtn.addActionListener(e -> cardLayout.show(contentPanel, "LeaveRequest"));

        if (isLeaveApprovalAllowed(role)) {
            JButton leaveApprovalsBtn = createNavButton("Leave Approvals", "employee.png");
            navPanel.add(leaveApprovalsBtn);
            navPanel.add(Box.createVerticalStrut(5));

            contentPanel.add(leaveApprovalPanel, "LeaveApprovals");
            leaveApprovalsBtn.addActionListener(e -> cardLayout.show(contentPanel, "LeaveApprovals"));
        }

        JButton leaveHistoryBtn = createNavButton("My Leaves", "employee.png");
        navPanel.add(leaveHistoryBtn);
        navPanel.add(Box.createVerticalStrut(5));
        leaveHistoryBtn.addActionListener(e -> cardLayout.show(contentPanel, "LeaveHistory"));

        if (isUserAccountsAllowed(role)) {
            JButton userAccountsBtn = createNavButton("User Accounts", "employee.png");
            navPanel.add(userAccountsBtn);
            navPanel.add(Box.createVerticalStrut(5));

            contentPanel.add(userAccountsPanel, "UserAccounts");
            userAccountsBtn.addActionListener(e -> cardLayout.show(contentPanel, "UserAccounts"));
        }

        JButton logoutButton = createNavButton("Log-out", "logout.png");
        logoutButton.setForeground(Color.GRAY);
        logoutButton.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginPanel(authService).setVisible(true));
        });

        sidebar.add(profilePanel, BorderLayout.NORTH);
        sidebar.add(navPanel, BorderLayout.CENTER);
        sidebar.add(logoutButton, BorderLayout.SOUTH);

        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        showDefaultPage(role);

        setVisible(true);
    }

    private JPanel buildProfilePanel(Color sidebarColor, Font boldFont, Font regularFont) {
        JPanel profilePanel = new JPanel(new BorderLayout(10, 0));
        profilePanel.setBackground(sidebarColor);

        JLabel profileIcon = new JLabel(loadImageIcon("/assets/userprofile.png", 40, 40));
        profilePanel.add(profileIcon, BorderLayout.WEST);

        JPanel namePanel = new JPanel();
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));
        namePanel.setBackground(sidebarColor);

        JLabel userName = new JLabel(safe(currentUser.getFirstName(), "(Unknown)"));
        JLabel userRole = new JLabel(safe(currentUser.getPosition(), "(Unknown)"));

        userName.setFont(boldFont);
        userRole.setFont(regularFont);
        userRole.setForeground(Color.GRAY);

        namePanel.add(userName);
        namePanel.add(userRole);

        profilePanel.add(namePanel, BorderLayout.CENTER);
        return profilePanel;
    }

    private void showDefaultPage(String role) {
        if (isUserAccountsAllowed(role)) {
            cardLayout.show(contentPanel, "UserAccounts");
        } else if (isEmployeeModuleAllowed(role)) {
            cardLayout.show(contentPanel, "Employee");
        } else {
            cardLayout.show(contentPanel, "LeaveRequest");
        }
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

    private String roleName(AuthenticatedUser user) {
        return user == null || user.getRole() == null ? "" : user.getRole().name();
    }

    private String safe(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private JButton createNavButton(String text, String iconFileName) {
        JButton button = new JButton(text);
        button.setIcon(loadImageIcon("/assets/" + iconFileName, 20, 20));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setIconTextGap(15);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
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
                button.setBackground(new Color(240, 240, 240));
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
        if (imageUrl != null) {
            ImageIcon icon = new ImageIcon(imageUrl);
            Image scaled = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } else {
            System.err.println("Image not found: " + path);
            return null;
        }
    }
}