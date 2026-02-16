package gui;

import model.Role;
import model.UserAccount;

import repository.CredentialRepository;
import repository.EmployeeRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class UserAccountsPanel extends JPanel {

    private final CredentialRepository credentialRepo;
    private final EmployeeRepository employeeRepo;

    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;

    // Gradient background colors (same as other panels)
    private final Color gradientStart = new Color(255, 204, 229);
    private final Color gradientEnd   = new Color(255, 229, 180);

    // Color gradientStart = new Color(255, 204, 229); 
//        Color gradientEnd = new Color(255, 229, 180);
    
    // Columns (nicer like Employee table)
    private final String[] columnNames = {
            "Credential ID", "Username", "Role", "Employee #", "First Name", "Position"
    };

    // Right-click row menu
    private JPopupMenu rowMenu;

    public UserAccountsPanel(CredentialRepository credentialRepo, EmployeeRepository employeeRepo) {
        this.credentialRepo = credentialRepo;
        this.employeeRepo = employeeRepo;

        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(20, 50, 20, 50));

        // Ensure employee cache is loaded (for name/position mapping)
        this.employeeRepo.load();

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);

        buildRowMenu();
        refreshTable();
    }

    // Convenience constructor if you want
    // public UserAccountsPanel() {
    //     this(new FileCredentialRepository(), new FileEmployeeRepository());
    // }

    // ==========================
    // TOP BAR (CLEAN)
    // ==========================
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

        JButton searchBtn = new JButton("Search");
        styleColoredButton(searchBtn, new Color(30, 144, 255), 90, 34);
        searchBtn.addActionListener(e -> filterTable(searchField.getText().trim()));

        JButton refreshBtn = new JButton("Refresh");
        styleColoredButton(refreshBtn, Color.BLACK, 90, 34);
        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            refreshTable();
        });

        JButton addBtn = new JButton("Add User");
        styleColoredButton(addBtn, new Color(34, 139, 34), 110, 34);
        addBtn.addActionListener(e -> showAddUserDialog());

        right.add(searchField);
        right.add(searchBtn);
        right.add(refreshBtn);
        right.add(addBtn);

        top.add(left, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);

        return top;
    }

    // ==========================
    // TABLE
    // ==========================
    private JScrollPane buildTable() {

        model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
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

        // Double-click => view details
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    showAccountDetailsPopup(table.getSelectedRow());
                }
            }
        });

        // Right-click context menu
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { maybeShowMenu(e); }
            @Override
            public void mouseReleased(MouseEvent e) { maybeShowMenu(e); }

            private void maybeShowMenu(MouseEvent e) {
                if (!e.isPopupTrigger()) return;

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

    // ==========================
    // RIGHT CLICK MENU
    // ==========================
    private void buildRowMenu() {
        rowMenu = new JPopupMenu();

        JMenuItem viewItem = new JMenuItem("View Details");
        viewItem.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) showAccountDetailsPopup(row);
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

    // ==========================
    // CRUD FUNCTIONS
    // ==========================
    private void showAddUserDialog() {

        JTextField username = new JTextField();
        JPasswordField password = new JPasswordField();
        JTextField employeeNo = new JTextField();

        JComboBox<String> roleBox = new JComboBox<>(new String[]{
                "EMPLOYEE", "HRADMIN", "IT"
        });

        JPanel panel = new JPanel(new GridLayout(0, 1, 8, 8));
        panel.add(new JLabel("Username:"));
        panel.add(username);
        panel.add(new JLabel("Password:"));
        panel.add(password);
        panel.add(new JLabel("Role:"));
        panel.add(roleBox);
        panel.add(new JLabel("Employee #:"));
        panel.add(employeeNo);

        int result = JOptionPane.showConfirmDialog(
                this, panel, "Add User Account",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) return;

        String u = username.getText().trim();
        String p = new String(password.getPassword()).trim();
        String emp = employeeNo.getText().trim();
        Role r = Role.from(roleBox.getSelectedItem().toString());

        if (u.isEmpty() || p.isEmpty() || emp.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "❌ Username, password, and Employee # are required.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Optional: validate employee exists
        String[] empRow = employeeRepo.findRowByEmployeeNo(emp);
        if (empRow == null) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Employee # not found in employee.txt.\nDo you still want to create this account?",
                    "Employee Not Found",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (confirm != JOptionPane.YES_OPTION) return;
        }

        if (credentialRepo.findByUsername(u) != null) {
            JOptionPane.showMessageDialog(this,
                    "❌ Username already exists.",
                    "Duplicate Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean ok = credentialRepo.add(u, p, r, emp);

        if (ok) {
            JOptionPane.showMessageDialog(this, "✅ User added successfully!");
            refreshTable();
        } else {
            JOptionPane.showMessageDialog(this, "❌ Failed to add user.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

        private void showUpdateDialog() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user first.");
            return;
        }

        row = table.convertRowIndexToModel(row);

        String username = model.getValueAt(row, 1).toString();
        String oldRole = model.getValueAt(row, 2).toString();
        String oldEmpNo = model.getValueAt(row, 3).toString();

        JPasswordField newPassword = new JPasswordField();
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"EMPLOYEE", "HRADMIN", "IT"});
        roleBox.setSelectedItem(oldRole);
        JTextField employeeNoField = new JTextField(oldEmpNo);

        JPanel panel = new JPanel(new GridLayout(0, 1, 8, 8));
        panel.add(new JLabel("Username: " + username));
        panel.add(new JLabel("New Password (leave blank to keep current):"));
        panel.add(newPassword);
        panel.add(new JLabel("Role:"));
        panel.add(roleBox);
        panel.add(new JLabel("Employee # (optional update):"));
        panel.add(employeeNoField);

        int result = JOptionPane.showConfirmDialog(
                this, panel, "Update User Account",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (result != JOptionPane.OK_OPTION) return;

        String pw = new String(newPassword.getPassword()).trim();
        Role newRole = Role.from(roleBox.getSelectedItem().toString());
        String newEmpNo = employeeNoField.getText().trim();

        boolean changed = false;
        boolean ok = true;

        // role
        if (!newRole.name().equalsIgnoreCase(oldRole)) {
            ok &= credentialRepo.updateRole(username, newRole);
            changed = true;
        }

        // password
        if (!pw.isEmpty()) {
            ok &= credentialRepo.updatePassword(username, pw);
            changed = true;
        }

        // employee #
        if (!newEmpNo.isEmpty() && !newEmpNo.equals(oldEmpNo)) {
            ok &= credentialRepo.updateEmployeeNo(username, newEmpNo);
            changed = true;
        }

        if (!changed) {
            JOptionPane.showMessageDialog(this, "No changes were made.");
            return;
        }

        if (ok) {
            JOptionPane.showMessageDialog(this, "✅ Account updated successfully!");
            refreshTable();
        } else {
            JOptionPane.showMessageDialog(this, "❌ Failed to update account.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedAccount() {

        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user first.");
            return;
        }

        row = table.convertRowIndexToModel(row);
        String username = model.getValueAt(row, 1).toString();

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete account:\n" + username + " ?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        boolean ok = credentialRepo.delete(username);

        if (ok) {
            JOptionPane.showMessageDialog(this, "✅ Account deleted.");
            refreshTable();
        } else {
            JOptionPane.showMessageDialog(this, "❌ Failed to delete account.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==========================
    // DATA LOADING
    // ==========================
    private void refreshTable() {
        model.setRowCount(0);

        // reload employee cache in case new employees were added
        employeeRepo.load();

        List<UserAccount> accounts = credentialRepo.findAll();

        for (UserAccount acc : accounts) {
            String empNo = acc.getEmployeeNumber();

            String firstName = "(Unknown)";
            String position  = "(Unknown)";

            String[] row = employeeRepo.findRowByEmployeeNo(empNo);
            if (row != null) {
                // your headers include First Name and Position
                List<String> headers = employeeRepo.getHeaders();
                int fnIdx = headers.indexOf("First Name");
                int posIdx = headers.indexOf("Position");

                if (fnIdx >= 0 && fnIdx < row.length) firstName = row[fnIdx];
                if (posIdx >= 0 && posIdx < row.length) position = row[posIdx];
            }

            model.addRow(new Object[]{
                    acc.getCredentialId(),
                    acc.getUsername(),
                    acc.getRole().name(),
                    empNo,
                    firstName,
                    position
            });
        }
    }

    private void filterTable(String query) {
        if (query == null || query.isBlank()) {
            refreshTable();
            return;
        }

        String q = query.toLowerCase();
        model.setRowCount(0);

        employeeRepo.load();
        List<UserAccount> accounts = credentialRepo.findAll();

        for (UserAccount acc : accounts) {

            String empNo = acc.getEmployeeNumber();
            String firstName = "(Unknown)";
            String position  = "(Unknown)";

            String[] row = employeeRepo.findRowByEmployeeNo(empNo);
            if (row != null) {
                List<String> headers = employeeRepo.getHeaders();
                int fnIdx = headers.indexOf("First Name");
                int posIdx = headers.indexOf("Position");

                if (fnIdx >= 0 && fnIdx < row.length) firstName = row[fnIdx];
                if (posIdx >= 0 && posIdx < row.length) position = row[posIdx];
            }

            boolean match =
                    acc.getUsername().toLowerCase().contains(q) ||
                    acc.getRole().name().toLowerCase().contains(q) ||
                    (empNo != null && empNo.toLowerCase().contains(q)) ||
                    firstName.toLowerCase().contains(q) ||
                    position.toLowerCase().contains(q);

            if (match) {
                model.addRow(new Object[]{
                        acc.getCredentialId(),
                        acc.getUsername(),
                        acc.getRole().name(),
                        empNo,
                        firstName,
                        position
                });
            }
        }
    }

    private void showAccountDetailsPopup(int selectedRow) {

        int row = table.convertRowIndexToModel(selectedRow);

        String credentialId = model.getValueAt(row, 0).toString();
        String username     = model.getValueAt(row, 1).toString();
        String role         = model.getValueAt(row, 2).toString();
        String employeeNo   = model.getValueAt(row, 3).toString();
        String firstName    = model.getValueAt(row, 4).toString();
        String position     = model.getValueAt(row, 5).toString();

        JPanel panel = new JPanel(new GridLayout(0, 1, 6, 6));
        panel.add(new JLabel("Credential ID: " + credentialId));
        panel.add(new JLabel("Username: " + username));
        panel.add(new JLabel("Role: " + role));
        panel.add(new JLabel("Employee #: " + employeeNo));
        panel.add(new JLabel("First Name: " + firstName));
        panel.add(new JLabel("Position: " + position));
        panel.add(new JLabel(" "));
        panel.add(new JLabel("Tip: Right-click a row for Update/Delete"));

        JOptionPane.showMessageDialog(this, panel, "User Account Details", JOptionPane.INFORMATION_MESSAGE);
    }

    // ==========================
    // UI HELPERS
    // ==========================
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
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(230, 240, 255));
            }

            int width = UserAccountsPanel.this.getWidth();
            int fontSize = width < 700 ? 11 : (width < 900 ? 12 : 14);
            c.setFont(new Font("SansSerif", Font.PLAIN, fontSize));

            return c;
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
        g2d.setPaint(new GradientPaint(0, 0, gradientStart, 0, getHeight(), gradientEnd));
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }
}
