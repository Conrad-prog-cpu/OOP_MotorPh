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
import java.util.List;

public class LeaveApprovalPanel extends JPanel {

    private final AuthenticatedUser currentUser;
    private final LeaveService leaveService;

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"LeaveId", "EmployeeId", "Type", "Date Range", "Status", "Requested At"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(model);

    private final JButton viewReasonButton = new JButton("View Reason");
    private final JButton refreshButton = new JButton("Refresh");
    private final JButton approveButton = new JButton("Approve");
    private final JButton denyButton = new JButton("Deny");

    public LeaveApprovalPanel(LeaveService leaveService, AuthenticatedUser currentUser) {
        this.leaveService = leaveService;
        this.currentUser = currentUser;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setOpaque(false);

        buildUI();
        wireEvents();
        loadPending();
    }

    private void buildUI() {
        JLabel title = new JLabel("Leave Approvals (HR/Admin)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));

        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(table);

        viewReasonButton.setEnabled(false);
        approveButton.setEnabled(false);
        denyButton.setEnabled(false);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setOpaque(false);
        topPanel.add(refreshButton);
        topPanel.add(viewReasonButton);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        bottomPanel.add(denyButton);
        bottomPanel.add(approveButton);

        add(title, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(topPanel, BorderLayout.WEST);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void wireEvents() {
        table.getSelectionModel().addListSelectionListener(e -> {
            boolean selected = table.getSelectedRow() != -1;
            viewReasonButton.setEnabled(selected);
            approveButton.setEnabled(selected);
            denyButton.setEnabled(selected);
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    showReasonPopup();
                }
            }
        });

        refreshButton.addActionListener(e -> loadPending());
        viewReasonButton.addActionListener(e -> showReasonPopup());
        approveButton.addActionListener(e -> approveSelected());
        denyButton.addActionListener(e -> denySelected());
    }

    private void loadPending() {
        model.setRowCount(0);

        List<LeaveRequestDto> pendingRequests = leaveService.findPendingLeaves();

        for (LeaveRequestDto request : pendingRequests) {
            model.addRow(new Object[]{
                    safe(request.getLeaveId()),
                    safe(request.getEmployeeId()),
                    safe(request.getLeaveType()),
                    safe(request.getDateRangeDisplay()),
                    safe(request.getStatus()),
                    safe(request.getRequestedAt())
            });
        }
    }

    private void showReasonPopup() {
        String leaveId = getSelectedLeaveId();
        if (leaveId == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Select a leave request first.",
                    "Select",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        LeaveRequestDto request = leaveService.findLeaveById(leaveId);

        if (request == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Leave request not found.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        JTextArea textArea = new JTextArea(safe(request.getReason()));
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setEditable(false);
        textArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(420, 220));

        String dialogTitle = "Reason - " + safe(request.getEmployeeId()) + " (" + safe(request.getLeaveType()) + ")";
        JOptionPane.showMessageDialog(this, scrollPane, dialogTitle, JOptionPane.INFORMATION_MESSAGE);
    }

    private void approveSelected() {
        if (!canManageLeaves()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Access denied.",
                    "Permission",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String leaveId = getSelectedLeaveId();
        if (leaveId == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Select a leave request first.",
                    "Select",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        boolean approved = leaveService.approveLeave(leaveId, currentUser);

        if (!approved) {
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to approve leave request.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        loadPending();
        JOptionPane.showMessageDialog(this, "Approved.");
    }

    private void denySelected() {
        if (!canManageLeaves()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Access denied.",
                    "Permission",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String leaveId = getSelectedLeaveId();
        if (leaveId == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Select a leave request first.",
                    "Select",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        boolean denied = leaveService.rejectLeave(leaveId, currentUser);

        if (!denied) {
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to deny leave request.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        loadPending();
        JOptionPane.showMessageDialog(this, "Denied.");
    }

    private boolean canManageLeaves() {
        return currentUser != null
                && currentUser.getRole() != null
                && "HRADMIN".equalsIgnoreCase(currentUser.getRole().name());
    }

    private String getSelectedLeaveId() {
        int row = table.getSelectedRow();
        if (row == -1) {
            return null;
        }

        Object value = model.getValueAt(row, 0);
        return safe(value);
    }

    private String safe(Object value) {
        return value == null ? "" : value.toString().trim();
    }
}