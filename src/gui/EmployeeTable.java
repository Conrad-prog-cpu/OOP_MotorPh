package gui;

import repository.EmployeeRepository;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class EmployeeTable extends JPanel {

    private final EmployeeRepository employeeRepo;

    private JTable table;
    private DefaultTableModel model;

    private List<String[]> employeeRows = new ArrayList<>();

    // Column names shown in the table
    private final String[] columnNames = {
            "Employee ID", "Last Name", "First Name",
            "SSS No.", "PhilHealth No.", "TIN", "Pag-IBIG No."
    };

    // Indices in employee.txt that match above columns
    private final int[] indices = {0, 1, 2, 6, 7, 8, 9};

//    private final Color gradientStart = new Color(255, 204, 229);
//    private final Color gradientEnd   = new Color(255, 229, 180);

    public EmployeeTable(EmployeeRepository employeeRepo) {
        this.employeeRepo = employeeRepo;

        setLayout(new BorderLayout());
        setOpaque(false);

        // load data
        reloadFromRepo();

        buildTable();
        buildScrollPane();

        // initial fill
        refreshTable(employeeRows);
    }

    // ---------------- public API ----------------

    public JTable getTable() {
        return table;
    }

    /** Reload from repo and refresh */
    public void reload() {
        reloadFromRepo();
        refreshTable(employeeRows);
    }

    /** Updates JTable model rows using "indices" projection
     * @param rows */
    public final void refreshTable(List<String[]> rows) {
        model.setRowCount(0);

        if (rows == null) return;

        int maxIndex = indices[indices.length - 1];

        for (String[] row : rows) {
            if (row == null || row.length <= maxIndex) continue;

            String[] displayRow = new String[indices.length];
            for (int i = 0; i < indices.length; i++) {
                displayRow[i] = safe(row[indices[i]]);
            }
            model.addRow(displayRow);
        }
    }

    /** Simple contains() filter across shown fields
     * @param query */
    public void filterTable(String query) {
        if (query == null || query.trim().isEmpty()) {
            refreshTable(employeeRows);
            return;
        }

        String q = query.trim().toLowerCase();
        List<String[]> filtered = new ArrayList<>();

        for (String[] row : employeeRows) {
            if (row == null) continue;

            for (int idx : indices) {
                if (idx < row.length && safe(row[idx]).toLowerCase().contains(q)) {
                    filtered.add(row);
                    break;
                }
            }
        }

        refreshTable(filtered);
    }

    /** Returns the FULL raw row (all columns) of the selected employee */
    public Vector<Object> getSelectedEmployeeFullDetails() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) return null;

        int modelRow = table.convertRowIndexToModel(selectedRow);
        String employeeId = safe(table.getModel().getValueAt(modelRow, 0));

        String[] full = employeeRepo.findRowByEmployeeNo(employeeId);
        if (full == null) return null;

        Vector<Object> v = new Vector<>();
        for (String s : full) v.add(s);
        return v;
    }

    // ---------------- internals ----------------

    private void reloadFromRepo() {
        employeeRepo.load();
        employeeRows = new ArrayList<>(employeeRepo.getRows());
    }

    private void buildTable() {
        model = new DefaultTableModel(columnNames, 0) {
            // make table read-only
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);

        table.setFillsViewportHeight(true);
        table.setOpaque(false);
        table.setShowGrid(true);
        table.setRowHeight(28);
        table.setBorder(BorderFactory.createEmptyBorder());
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Custom cell renderer for alternating row colors and responsive font
        table.setDefaultRenderer(Object.class, new ResponsiveCellRenderer());

        // Header renderer
        JTableHeaderRenderer headerRenderer = new JTableHeaderRenderer();
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }

        // Optional nicer column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(90);  // Employee ID
        table.getColumnModel().getColumn(1).setPreferredWidth(120); // Last Name
        table.getColumnModel().getColumn(2).setPreferredWidth(120); // First Name
    }

    private void buildScrollPane() {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getHorizontalScrollBar().setUI(new ModernScrollBarUI());

        add(scrollPane, BorderLayout.CENTER);
    }

    private static String safe(Object o) {
        return (o == null) ? "" : o.toString().trim();
    }

    // ---------------- renderers ----------------

    private static class JTableHeaderRenderer extends DefaultTableCellRenderer {
        public JTableHeaderRenderer() {
            setOpaque(true);
            setBackground(new Color(33, 150, 243));
            setForeground(Color.WHITE);
            setFont(new Font("SansSerif", Font.BOLD, 14));
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column
        ) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            return this;
        }
    }

    private class ResponsiveCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column
        ) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(230, 240, 255));
            } else {
                c.setBackground(new Color(0, 0, 0)); //200, 225, 255
            }

            // Slight padding
            if (c instanceof JLabel label) {
                label.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            }

            // responsive font
            int width = EmployeeTable.this.getWidth();
            int fontSize = width < 650 ? 11 : (width < 900 ? 13 : 14);
            c.setFont(new Font("SansSerif", Font.PLAIN, fontSize));

            return c;
        }
    }

    private static class ModernScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            thumbColor = new Color(255, 255, 255, 200);
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
            button.setOpaque(false);
            button.setContentAreaFilled(false);
            button.setBorderPainted(false);
            return button;
        }
    }

    // ---------------- gradient background ----------------

//    @Override
//    protected void paintComponent(Graphics g) {
//        super.paintComponent(g);
//        Graphics2D g2d = (Graphics2D) g;
//
//        GradientPaint gp = new GradientPaint(0, 0, gradientStart, 0, getHeight(), gradientEnd);
//        g2d.setPaint(gp);
//        g2d.fillRect(0, 0, getWidth(), getHeight());
//    }
}
