package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

import model.Role;
import model.User;

import repository.EmployeeRepository;
import repository.FileEmployeeRepository;
import repository.CredentialRepository;
import repository.FileCredentialRepository;

public class DashboardPanel extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private JPanel contentPanel;

    // Panels
    private final EmployeeManagementPanel employeePanel = new EmployeeManagementPanel();

    private final LeaveRequestPanel leaveRequestPanel;
    private final LeaveApprovalPanel leaveApprovalPanel;
    private final EmployeeLeaveHistoryPanel leaveHistoryPanel;

    // IT-only panel
    private final UserAccountsPanel userAccountsPanel;

    // Repositories (shared)
    private final EmployeeRepository employeeRepo;
    private final CredentialRepository credentialRepo;

    public DashboardPanel(User user) {
        setTitle("MotorPH Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(800, 500));

        // Theme
        Color sidebarColor = Color.WHITE;
        Color gradientStart = new Color(255, 204, 229); 
        Color gradientEnd = new Color(255, 229, 180);
        Font boldFont = new Font("Segoe UI", Font.BOLD, 16);
        Font regularFont = new Font("Segoe UI", Font.PLAIN, 14);

        // ✅ Repositories instead of FileHandler
        employeeRepo = new FileEmployeeRepository();
        credentialRepo = new FileCredentialRepository();

        // Load employee repo once so panels can read cached headers/rows fast
        employeeRepo.load();

        // Panels that need user
        leaveRequestPanel = new LeaveRequestPanel(user);
        leaveApprovalPanel = new LeaveApprovalPanel(user);
        leaveHistoryPanel = new EmployeeLeaveHistoryPanel(user);

        // ✅ IT-only panel uses repositories
        // If your UserAccountsPanel doesn't have this constructor yet, update it.
        userAccountsPanel = new UserAccountsPanel(credentialRepo, employeeRepo);

        // Apply access rules in EmployeeManagementPanel
        employeePanel.applyAccess(user);

        // Sidebar
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(sidebarColor);
        sidebar.setPreferredSize(new Dimension(280, getHeight()));
        sidebar.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Profile panel
        JPanel profilePanel = new JPanel(new BorderLayout(10, 0));
        profilePanel.setBackground(sidebarColor);

        JLabel profileIcon = new JLabel(loadImageIcon("/assets/userprofile.png", 40, 40));
        profilePanel.add(profileIcon, BorderLayout.WEST);

        JPanel namePanel = new JPanel();
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));
        namePanel.setBackground(sidebarColor);

        JLabel userName = new JLabel(user.getFirstName());
        JLabel userRole = new JLabel(user.getPosition());

        userName.setFont(boldFont);
        userRole.setFont(regularFont);
        userRole.setForeground(Color.GRAY);

        namePanel.add(userName);
        namePanel.add(userRole);

        profilePanel.add(namePanel, BorderLayout.CENTER);

        // Nav panel
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

        // Content panel with gradient
        contentPanel = new JPanel(cardLayout) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setPaint(new GradientPaint(0, 0, gradientStart, 0, getHeight(), gradientEnd));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        // Add panels that are always available
        contentPanel.add(leaveRequestPanel, "LeaveRequest");
        contentPanel.add(leaveHistoryPanel, "LeaveHistory");

        Role role = user.getRole();

        // ============ ROLE-BASED MENU ============

        // EMPLOYEE + HRADMIN: Employee page
        if (role == Role.EMPLOYEE || role == Role.HRADMIN || role == Role.HR) {
            JButton employeeBtn = createNavButton("Employee", "employee.png");
            navPanel.add(employeeBtn);
            navPanel.add(Box.createVerticalStrut(5));

            contentPanel.add(employeePanel, "Employee");
            employeeBtn.addActionListener(e -> cardLayout.show(contentPanel, "Employee"));
        }

        // Leave Request (ALL roles)
        JButton leaveRequestBtn = createNavButton("Leave Request", "employee.png");
        navPanel.add(leaveRequestBtn);
        navPanel.add(Box.createVerticalStrut(5));
        leaveRequestBtn.addActionListener(e -> cardLayout.show(contentPanel, "LeaveRequest"));

        // HRADMIN: Leave Approvals
        if (role == Role.HRADMIN) {
            JButton leaveApprovalsBtn = createNavButton("Leave Approvals", "employee.png");
            navPanel.add(leaveApprovalsBtn);
            navPanel.add(Box.createVerticalStrut(5));

            contentPanel.add(leaveApprovalPanel, "LeaveApprovals");
            leaveApprovalsBtn.addActionListener(e -> cardLayout.show(contentPanel, "LeaveApprovals"));
        }

        // My Leaves (ALL roles)
        JButton leaveHistoryBtn = createNavButton("My Leaves", "employee.png");
        navPanel.add(leaveHistoryBtn);
        navPanel.add(Box.createVerticalStrut(5));
        leaveHistoryBtn.addActionListener(e -> cardLayout.show(contentPanel, "LeaveHistory"));

        // IT: User Accounts ONLY (plus Leave Request + My Leaves)
        if (role == Role.IT) {
            JButton userAccountsBtn = createNavButton("User Accounts", "employee.png");
            navPanel.add(userAccountsBtn);
            navPanel.add(Box.createVerticalStrut(5));

            contentPanel.add(userAccountsPanel, "UserAccounts");
            userAccountsBtn.addActionListener(e -> cardLayout.show(contentPanel, "UserAccounts"));
        }

        // Logout
        JButton logoutButton = createNavButton("Log-out", "logout.png");
        logoutButton.setForeground(Color.GRAY);
        logoutButton.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginPanel().setVisible(true));
        });

        sidebar.add(profilePanel, BorderLayout.NORTH);
        sidebar.add(navPanel, BorderLayout.CENTER);
        sidebar.add(logoutButton, BorderLayout.SOUTH);

        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        // ============ DEFAULT LANDING PAGE ============
        if (role == Role.IT) {
            cardLayout.show(contentPanel, "UserAccounts");
        } else {
            cardLayout.show(contentPanel, "Employee");
        }

        setVisible(true);
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
