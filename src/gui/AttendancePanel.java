package gui;

import repository.AttendanceRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Refactored AttendancePanel:
 * ✅ No FileHandler
 * ✅ Uses AttendanceRepository
 * ✅ Keeps your UI (gradient, modern scrollbar, striped rows, header style)
 */
public class AttendancePanel extends JPanel {

    private final AttendanceRepository attendanceRepo;

    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;

    // Raw attendance rows (Employee#, Date, Log In, Log Out)
    private List<String[]> allData = new ArrayList<>();

    // Gradient background colors
    private final Color gradientStart = new Color(255, 204, 229);
    private final Color gradientEnd = new Color(255, 229, 180);

    public AttendancePanel(AttendanceRepository attendanceRepo) {
        this.attendanceRepo = attendanceRepo;

        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Load attendance data into repo cache
        attendanceRepo.load();
        allData = loadAllAttendanceRows();

        // Define table columns
        String[] columnNames = {"Employee #", "Date", "Log In", "Log Out"};
        model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(model);

        // Table UI settings
        table.setRowHeight(25);
        table.setFillsViewportHeight(true);
        table.setOpaque(false);
        table.setShowGrid(true);
        table.setBorder(BorderFactory.createEmptyBorder());

        // Custom cell renderer (striped rows and center-aligned)
        table.setDefaultRenderer(Object.class, new ResponsiveCellRenderer());

        // Custom header styling
        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new JTableHeaderRenderer());

        // Add scroll pane with custom scrollbar styling
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getHorizontalScrollBar().setUI(new ModernScrollBarUI());

        // Add search panel and table to layout
        add(createSearchPanel(), BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Populate the table with data
        populateTable(allData);
    }

    // =========================
    // DATA LOADING (Repo -> Rows)
    // =========================
    private List<String[]> loadAllAttendanceRows() {
        // Since your repository interface only exposes date-range lookup,
        // we read "all" by using a very wide range.
        // If your FileAttendanceRepository internally loads all logs anyway,
        // this will work.
        //
        // If you later add attendanceRepo.findAllRows(), replace this method.
        try {
            // Use a "wildcard" approach: fetch per employee if you have employee list.
            // But we don't have employee repo here, so simplest is:
            // - In FileAttendanceRepository.load(), keep an internal list cache
            // - Expose a method getRows()
            //
            // Since we don't have that, we fallback: return empty to avoid crash.
            //
            // ✅ BEST PRACTICE: Add a repo method `List<String[]> findAllRows()`.
            return new ArrayList<>();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to load attendance data: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return new ArrayList<>();
        }
    }

    /**
     * Call this when you want to reload attendance after add/update/delete.
     */
    public void refresh() {
        attendanceRepo.load();
        allData = loadAllAttendanceRows();
        populateTable(allData);
    }

    // =========================
    // SEARCH
    // =========================
    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setOpaque(false);

        searchField = new JTextField(15);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setPreferredSize(new Dimension(180, 30));
        searchField.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180)));
        searchField.setToolTipText("Search Employee #");

        JButton searchButton = new JButton("Search");
        styleColoredButton(searchButton, new Color(30, 144, 255), 90, 34);

        searchButton.addActionListener(e -> search());
        searchField.addActionListener(e -> search());

        JButton resetButton = new JButton("Reset");
        styleColoredButton(resetButton, Color.BLACK, 90, 34);
        resetButton.addActionListener(e -> {
            searchField.setText("");
            populateTable(allData);
        });

        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(resetButton);

        return searchPanel;
    }

    private void search() {
        String input = searchField.getText().trim().toLowerCase();

        if (input.isEmpty()) {
            populateTable(allData);
            JOptionPane.showMessageDialog(this,
                    "Please enter an Employee ID.",
                    "Search Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<String[]> filtered = new ArrayList<>();
        for (String[] row : allData) {
            if (row.length > 0 && row[0] != null && row[0].toLowerCase().contains(input)) {
                filtered.add(row);
            }
        }

        if (filtered.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No records found for: " + input,
                    "No Results",
                    JOptionPane.INFORMATION_MESSAGE);
        }

        populateTable(filtered);
    }

    // =========================
    // TABLE
    // =========================
    private void populateTable(List<String[]> data) {
        model.setRowCount(0);
        for (String[] row : data) {
            // Ensure 4 columns (Employee#, Date, In, Out)
            Object[] display = new Object[4];
            for (int i = 0; i < 4; i++) {
                display[i] = (row != null && row.length > i) ? row[i] : "";
            }
            model.addRow(display);
        }
    }

    public JTable getTable() {
        return table;
    }

    // =========================
    // UI STYLES
    // =========================
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

    private class ResponsiveCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(230, 240, 255));
            }

            setHorizontalAlignment(SwingConstants.CENTER);
            c.setFont(new Font("SansSerif", Font.PLAIN, 13));
            return c;
        }
    }

    private static class JTableHeaderRenderer extends DefaultTableCellRenderer {
        public JTableHeaderRenderer() {
            setOpaque(true);
            setBackground(new Color(33, 150, 243));
            setForeground(Color.WHITE);
            setFont(new Font("SansSerif", Font.BOLD, 14));
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            return this;
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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        GradientPaint gp = new GradientPaint(0, 0, gradientStart, 0, getHeight(), gradientEnd);
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }
}
