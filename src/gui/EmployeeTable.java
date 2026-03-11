package gui;

import service.EmployeeRowDto;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeTable extends JPanel {

    private static final String[] COLUMN_NAMES = {
            "Employee ID", "Last Name", "First Name",
            "SSS No.", "PhilHealth No.", "TIN", "Pag-IBIG No."
    };

    private JTable table;
    private DefaultTableModel model;

    private List<EmployeeRowDto> employeeRows = new ArrayList<>();

    public EmployeeTable() {
        setLayout(new BorderLayout());
        setOpaque(false);

        buildTable();
        buildScrollPane();
    }

    public JTable getTable() {
        return table;
    }

    public void refreshTable(List<EmployeeRowDto> rows) {
        employeeRows = rows == null ? new ArrayList<>() : new ArrayList<>(rows);

        model.setRowCount(0);

        for (EmployeeRowDto row : employeeRows) {
            if (row == null) {
                continue;
            }

            model.addRow(new Object[]{
                    safe(row.getEmployeeId()),
                    safe(row.getLastName()),
                    safe(row.getFirstName()),
                    safe(row.getSssNumber()),
                    safe(row.getPhilHealthNumber()),
                    safe(row.getTinNumber()),
                    safe(row.getPagIbigNumber())
            });
        }
    }

    public void filterTable(String query) {
        if (query == null || query.trim().isEmpty()) {
            refreshTable(employeeRows);
            return;
        }

        String q = query.trim().toLowerCase();
        List<EmployeeRowDto> filtered = new ArrayList<>();

        for (EmployeeRowDto row : employeeRows) {
            if (row == null) {
                continue;
            }

            if (containsIgnoreCase(row.getEmployeeId(), q)
                    || containsIgnoreCase(row.getLastName(), q)
                    || containsIgnoreCase(row.getFirstName(), q)
                    || containsIgnoreCase(row.getSssNumber(), q)
                    || containsIgnoreCase(row.getPhilHealthNumber(), q)
                    || containsIgnoreCase(row.getTinNumber(), q)
                    || containsIgnoreCase(row.getPagIbigNumber(), q)) {
                filtered.add(row);
            }
        }

        model.setRowCount(0);

        for (EmployeeRowDto row : filtered) {
            model.addRow(new Object[]{
                    safe(row.getEmployeeId()),
                    safe(row.getLastName()),
                    safe(row.getFirstName()),
                    safe(row.getSssNumber()),
                    safe(row.getPhilHealthNumber()),
                    safe(row.getTinNumber()),
                    safe(row.getPagIbigNumber())
            });
        }
    }

    public String getSelectedEmployeeId() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            return null;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        Object value = table.getModel().getValueAt(modelRow, 0);

        return safe(value);
    }

    private void buildTable() {
        model = new DefaultTableModel(COLUMN_NAMES, 0) {
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

        table.setDefaultRenderer(Object.class, new ResponsiveCellRenderer());

        JTableHeaderRenderer headerRenderer = new JTableHeaderRenderer();
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }

        table.getColumnModel().getColumn(0).setPreferredWidth(90);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
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

    private boolean containsIgnoreCase(String value, String query) {
        return safe(value).toLowerCase().contains(query);
    }

    private static String safe(Object value) {
        return value == null ? "" : value.toString().trim();
    }

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
            Component component = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column
            );

            if (!isSelected) {
                component.setBackground(row % 2 == 0 ? Color.WHITE : new Color(230, 240, 255));
            } else {
                component.setBackground(Color.BLACK);
            }

            if (component instanceof JLabel label) {
                label.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            }

            int width = EmployeeTable.this.getWidth();
            int fontSize = width < 650 ? 11 : (width < 900 ? 13 : 14);
            component.setFont(new Font("SansSerif", Font.PLAIN, fontSize));

            return component;
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
}