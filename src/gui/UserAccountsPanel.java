package gui;

import service.EmployeeService;
import service.UserAccountCreateRequest;
import service.UserAccountDto;
import service.UserAccountService;
import service.UserAccountUpdateRequest;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class UserAccountsPanel extends JPanel {

    private static final Color GRADIENT_START = new Color(255, 204, 229);
    private static final Color GRADIENT_END = new Color(255, 229, 180);

    private static final String[] COLUMN_NAMES = {
            "Credential ID", "Username", "Role", "Employee #", "First Name", "Position"
    };

    private final UserAccountService userAccountService;
    private final EmployeeService employeeService;

    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;
    private JPopupMenu rowMenu;

    private List<UserAccountDto> currentRows = new ArrayList<>();

    public UserAccountsPanel(UserAccountService userAccountService, EmployeeService employeeService) {
        this.userAccountService = userAccountService;
        this.employeeService = employeeService;

        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(20, 50, 20, 50));

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);

        buildRowMenu();
        refreshTable();
    }

    private JPanel buildTopBar() {
        JPanel top = new JPanel(new BorderLayout(10, 10));
        top.setOpaque(false);
        top.setBorder(new EmptyBorder(10, 0, 10, 0));

        JLabel title = new JLabel("User");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.DARK_GRAY);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        left.add(title);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        searchField = new JTextField(16);
        searchField.setPreferredSize(new Dimension(220, 34));
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        searchField.setToolTipText("Search username, role, employee #, name, position");

        JButton searchButton = new JButton("Search");
        styleColoredButton(searchButton, new Color(30, 144, 255), 90, 34);
        searchButton.addActionListener(e -> filterTable(searchField.getText().trim()));

        JButton refreshButton = new JButton("Refresh");
        styleColoredButton(refreshButton, Color.BLACK, 90, 34);
        refreshButton.addActionListener(e -> {
            searchField.setText("");
            refreshTable();
        });

        JButton addButton = new JButton("Add User");
        styleColoredButton(addButton, new Color(34, 139, 34), 110, 34);
        addButton.addActionListener(e -> showAddUserDialog());

        right.add(searchField);
        right.add(searchButton);
        right.add(refreshButton);
        right.add(addButton);

        top.add(left, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);

        return top;
    }

    private JScrollPane buildTable() {
        model = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(28);
        table.setShowGrid(true);
        table.setFillsViewportHeight(true);
        table.setOpaque(false);
        table.setBorder(BorderFactory.createEmptyBorder());
        table.setDefaultRenderer(Object.class, new ResponsiveCellRenderer());

        JTableHeaderRenderer headerRenderer = new JTableHeaderRenderer();
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    showAccountDetailsPopup(table.getSelectedRow());
                }
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                maybeShowMenu(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                maybeShowMenu(e);
            }

            private void maybeShowMenu(MouseEvent e) {
                if (!e.isPopupTrigger()) {
                    return;
                }

                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    table.setRowSelectionInterval(row, row);
                    rowMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getHorizontalScrollBar().setUI(new ModernScrollBarUI());

        return scrollPane;
    }

    private void buildRowMenu() {
        rowMenu = new JPopupMenu();

        JMenuItem viewItem = new JMenuItem("View Details");
        viewItem.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                showAccountDetailsPopup(row);
            }
        });

        JMenuItem updateItem = new JMenuItem("Update...");
        updateItem.addActionListener(e -> showUpdateDialog());

        JMenuItem deleteItem = new JMenuItem("Delete...");
        deleteItem.addActionListener(e -> deleteSelectedAccount());

        rowMenu.add(viewItem);
        rowMenu.addSeparator();
        rowMenu.add(updateItem);
        rowMenu.add(deleteItem);
    }

    private void showAddUserDialog() {
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JTextField employeeNoField = new JTextField();

        JComboBox<String> roleBox = new JComboBox<>(new String[]{
                "EMPLOYEE", "HRADMIN", "IT"
        });

        JPanel panel = new JPanel(new GridLayout(0, 1, 8, 8));
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel("Role:"));
        panel.add(roleBox);
        panel.add(new JLabel("Employee #:"));
        panel.add(employeeNoField);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Add User Account",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String username = safe(usernameField.getText());
        String password = safe(new String(passwordField.getPassword()));
        String employeeNo = safe(employeeNoField.getText());
        String role = safe(roleBox.getSelectedItem());

        if (username.isEmpty() || password.isEmpty() || employeeNo.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Username, password, and Employee # are required.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        if (!employeeService.existsByEmployeeId(employeeNo)) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Employee # not found in employee records.\nDo you still want to create this account?",
                    "Employee Not Found",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }

        if (userAccountService.findByUsername(username) != null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Username already exists.",
                    "Duplicate Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        boolean added = userAccountService.add(new UserAccountCreateRequest(
                username,
                password,
                role,
                employeeNo
        ));

        if (added) {
            JOptionPane.showMessageDialog(this, "User added successfully.");
            refreshTable();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to add user.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showUpdateDialog() {
        UserAccountDto selected = getSelectedAccount();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a user first.");
            return;
        }

        JPasswordField newPasswordField = new JPasswordField();
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"EMPLOYEE", "HRADMIN", "IT"});
        roleBox.setSelectedItem(safe(selected.getRole()));

        JTextField employeeNoField = new JTextField(safe(selected.getEmployeeNumber()));

        JPanel panel = new JPanel(new GridLayout(0, 1, 8, 8));
        panel.add(new JLabel("Username: " + safe(selected.getUsername())));
        panel.add(new JLabel("New Password (leave blank to keep current):"));
        panel.add(newPasswordField);
        panel.add(new JLabel("Role:"));
        panel.add(roleBox);
        panel.add(new JLabel("Employee # (optional update):"));
        panel.add(employeeNoField);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Update User Account",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String newPassword = safe(new String(newPasswordField.getPassword()));
        String newRole = safe(roleBox.getSelectedItem());
        String newEmployeeNo = safe(employeeNoField.getText());

        boolean changed = false;
        boolean ok = true;

        if (!newRole.equalsIgnoreCase(safe(selected.getRole()))) {
            ok &= userAccountService.update(new UserAccountUpdateRequest(
                    safe(selected.getUsername()),
                    null,
                    newRole,
                    null
            ));
            changed = true;
        }

        if (!newPassword.isEmpty()) {
            ok &= userAccountService.update(new UserAccountUpdateRequest(
                    safe(selected.getUsername()),
                    newPassword,
                    null,
                    null
            ));
            changed = true;
        }

        if (!newEmployeeNo.isEmpty() && !newEmployeeNo.equals(safe(selected.getEmployeeNumber()))) {
            ok &= userAccountService.update(new UserAccountUpdateRequest(
                    safe(selected.getUsername()),
                    null,
                    null,
                    newEmployeeNo
            ));
            changed = true;
        }

        if (!changed) {
            JOptionPane.showMessageDialog(this, "No changes were made.");
            return;
        }

        if (ok) {
            JOptionPane.showMessageDialog(this, "Account updated successfully.");
            refreshTable();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update account.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedAccount() {
        UserAccountDto selected = getSelectedAccount();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a user first.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete account:\n" + safe(selected.getUsername()) + " ?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        boolean deleted = userAccountService.deleteByUsername(safe(selected.getUsername()));

        if (deleted) {
            JOptionPane.showMessageDialog(this, "Account deleted.");
            refreshTable();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to delete account.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshTable() {
        currentRows = userAccountService.findAll();
        if (currentRows == null) {
            currentRows = new ArrayList<>();
        }

        model.setRowCount(0);

        for (UserAccountDto account : currentRows) {
            model.addRow(new Object[]{
                    safe(account.getCredentialId()),
                    safe(account.getUsername()),
                    safe(account.getRole()),
                    safe(account.getEmployeeNumber()),
                    safe(account.getFirstName()),
                    safe(account.getPosition())
            });
        }
    }

    private void filterTable(String query) {
        if (query == null || query.isBlank()) {
            refreshTable();
            return;
        }

        String q = query.toLowerCase().trim();
        model.setRowCount(0);

        for (UserAccountDto account : currentRows) {
            boolean match =
                    safe(account.getUsername()).toLowerCase().contains(q)
                            || safe(account.getRole()).toLowerCase().contains(q)
                            || safe(account.getEmployeeNumber()).toLowerCase().contains(q)
                            || safe(account.getFirstName()).toLowerCase().contains(q)
                            || safe(account.getPosition()).toLowerCase().contains(q);

            if (match) {
                model.addRow(new Object[]{
                        safe(account.getCredentialId()),
                        safe(account.getUsername()),
                        safe(account.getRole()),
                        safe(account.getEmployeeNumber()),
                        safe(account.getFirstName()),
                        safe(account.getPosition())
                });
            }
        }
    }

    private void showAccountDetailsPopup(int selectedRow) {
        int row = table.convertRowIndexToModel(selectedRow);
        if (row < 0 || row >= currentRows.size()) {
            return;
        }

        UserAccountDto account = currentRows.get(row);

        JPanel panel = new JPanel(new GridLayout(0, 1, 6, 6));
        panel.add(new JLabel("Credential ID: " + safe(account.getCredentialId())));
        panel.add(new JLabel("Username: " + safe(account.getUsername())));
        panel.add(new JLabel("Role: " + safe(account.getRole())));
        panel.add(new JLabel("Employee #: " + safe(account.getEmployeeNumber())));
        panel.add(new JLabel("First Name: " + safe(account.getFirstName())));
        panel.add(new JLabel("Position: " + safe(account.getPosition())));
        panel.add(new JLabel(" "));
        panel.add(new JLabel("Tip: Right-click a row for Update/Delete"));

        JOptionPane.showMessageDialog(this, panel, "User Account Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private UserAccountDto getSelectedAccount() {
        int row = table.getSelectedRow();
        if (row == -1) {
            return null;
        }

        int modelRow = table.convertRowIndexToModel(row);
        if (modelRow < 0 || modelRow >= currentRows.size()) {
            return null;
        }

        return currentRows.get(modelRow);
    }

    private String safe(Object value) {
        return value == null ? "" : value.toString().trim();
    }

    private static class JTableHeaderRenderer extends DefaultTableCellRenderer {
        public JTableHeaderRenderer() {
            setOpaque(true);
            setBackground(new Color(33, 150, 243));
            setForeground(Color.WHITE);
            setFont(new Font("SansSerif", Font.BOLD, 14));
            setHorizontalAlignment(SwingConstants.CENTER);
        }
    }

    private class ResponsiveCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            Component component = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column
            );

            if (!isSelected) {
                component.setBackground(row % 2 == 0 ? Color.WHITE : new Color(230, 240, 255));
            }

            int width = UserAccountsPanel.this.getWidth();
            int fontSize = width < 700 ? 11 : (width < 900 ? 12 : 14);
            component.setFont(new Font("SansSerif", Font.PLAIN, fontSize));

            return component;
        }
    }

    private static class ModernScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            thumbColor = Color.WHITE;
            trackColor = new Color(0, 0, 0, 0);
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createInvisibleButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createInvisibleButton();
        }

        private JButton createInvisibleButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }
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
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 18, 18);
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
}