package gui;

import service.AuthenticatedUser;
import service.LeaveRequestDto;
import service.LeaveService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class EmployeeLeaveHistoryPanel extends JPanel {

    private final AuthenticatedUser currentUser;
    private final LeaveService leaveService;

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"LeaveId", "Type", "Date Range", "Status", "Reviewed By", "Reviewed At"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(model);
    private final JButton refreshButton = new JButton("Refresh");
    private final JButton viewDetailsButton = new JButton("View Details");

    private List<LeaveRequestDto> currentRows = new ArrayList<>();

    public EmployeeLeaveHistoryPanel(LeaveService leaveService, AuthenticatedUser currentUser) {
        this.leaveService = leaveService;
        this.currentUser = currentUser;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setOpaque(false);

        buildUI();
        wireEvents();
        loadMyLeaves();
    }

    private void buildUI() {
        JLabel title = new JLabel("My Leave Requests");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));

        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(table);

        viewDetailsButton.setEnabled(false);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setOpaque(false);
        bottomPanel.add(refreshButton);
        bottomPanel.add(viewDetailsButton);

        add(title, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void wireEvents() {
        table.getSelectionModel().addListSelectionListener(e ->
                viewDetailsButton.setEnabled(table.getSelectedRow() != -1)
        );

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    showDetailsPopup();
                }
            }
        });

        refreshButton.addActionListener(e -> loadMyLeaves());
        viewDetailsButton.addActionListener(e -> showDetailsPopup());
    }

    private void loadMyLeaves() {
        model.setRowCount(0);

        String employeeNumber = currentUser == null ? "" : safe(currentUser.getEmployeeNumber());
        currentRows = leaveService.findMyLeaves(employeeNumber);

        if (currentRows == null) {
            currentRows = new ArrayList<>();
        }

        for (LeaveRequestDto request : currentRows) {
            model.addRow(new Object[]{
                    safe(request.getLeaveId()),
                    safe(request.getLeaveType()),
                    safe(request.getDateRangeDisplay()),
                    safe(request.getStatus()),
                    safe(request.getReviewedBy()),
                    safe(request.getDecisionAt())
            });
        }
    }

    private void showDetailsPopup() {
        LeaveRequestDto request = getSelectedRequest();

        if (request == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Select a request first.",
                    "Select",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String reviewedBy = safeOrDash(request.getReviewedBy());
        String reviewedAt = safeOrDash(request.getDecisionAt());

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);

        area.setText(
                "Employee: " + safe(request.getEmployeeName()) + "\n" +
                "Type: " + safe(request.getLeaveType()) + "\n" +
                "Date Range: " + safe(request.getDateRangeDisplay()) + "\n" +
                "Status: " + safe(request.getStatus()) + "\n" +
                "Requested At: " + safe(request.getRequestedAt()) + "\n" +
                "Reviewed By: " + reviewedBy + "\n" +
                "Reviewed At: " + reviewedAt + "\n\n" +
                "Reason:\n" + safe(request.getReason())
        );

        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setPreferredSize(new Dimension(450, 260));

        JOptionPane.showMessageDialog(
                this,
                scrollPane,
                "Leave Details",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private LeaveRequestDto getSelectedRequest() {
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

    private String safeOrDash(Object value) {
        String text = safe(value);
        return text.isBlank() ? "—" : text;
    }
}