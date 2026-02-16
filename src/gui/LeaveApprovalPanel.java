package gui;

import model.LeaveRequest;
import model.LeaveStatus;
import model.User;
import repository.FileLeaveRepository;
import repository.LeaveRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class LeaveApprovalPanel extends JPanel {

    private final User user;
    private final LeaveRepository repo;

    // ✅ Added Date Range column + RequestedAt
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"LeaveId", "EmployeeId", "Type", "Date Range", "Status", "Requested At"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // read-only
        }
    };

    private final JTable table = new JTable(model);

    private final JButton viewReasonBtn = new JButton("View Reason");
    private final JButton refreshBtn = new JButton("Refresh");
    private final JButton approveBtn = new JButton("Approve");
    private final JButton denyBtn = new JButton("Deny");

    public LeaveApprovalPanel(User user) {
        this.user = user;
        this.repo = new FileLeaveRepository();

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setOpaque(false);

        JLabel title = new JLabel("Leave Approvals (HR/Admin)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));

        // Table settings
        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // ✅ Enable/disable buttons on selection
        table.getSelectionModel().addListSelectionListener(e -> {
            boolean selected = table.getSelectedRow() != -1;
            viewReasonBtn.setEnabled(selected);
            approveBtn.setEnabled(selected);
            denyBtn.setEnabled(selected);
        });

        // ✅ Double-click opens "View Reason"
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    showReasonPopup();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);

        // Buttons
        refreshBtn.addActionListener(e -> loadPending());
        viewReasonBtn.addActionListener(e -> showReasonPopup());
        approveBtn.addActionListener(e -> approveSelected());
        denyBtn.addActionListener(e -> denySelected());

        // Initial state
        viewReasonBtn.setEnabled(false);
        approveBtn.setEnabled(false);
        denyBtn.setEnabled(false);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);
        top.add(refreshBtn);
        top.add(viewReasonBtn);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        bottom.add(denyBtn);
        bottom.add(approveBtn);

        add(title, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(top, BorderLayout.WEST);
        add(bottom, BorderLayout.SOUTH);

        loadPending();
    }

    private void loadPending() {
        model.setRowCount(0);

        List<LeaveRequest> pending = repo.findByStatus(LeaveStatus.PENDING);
        for (LeaveRequest r : pending) {

            String range = formatRange(r);

            model.addRow(new Object[]{
                    r.getLeaveId(),
                    r.getEmployeeId(),
                    r.getLeaveType().name(),
                    range,
                    r.getStatus().name(),
                    r.getRequestedAt() == null ? "" : r.getRequestedAt().toString()
            });
        }
    }

    private String formatRange(LeaveRequest r) {
        long days = ChronoUnit.DAYS.between(r.getStartDate(), r.getEndDate()) + 1; // inclusive
        String dayLabel = days == 1 ? "day" : "days";
        return r.getStartDate() + " \u2192 " + r.getEndDate() + " (" + days + " " + dayLabel + ")";
    }

    private void showReasonPopup() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a leave request first.", "Select",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String leaveId = model.getValueAt(row, 0).toString();

        repo.findById(leaveId).ifPresentOrElse(req -> {
            JTextArea area = new JTextArea(req.getReason());
            area.setWrapStyleWord(true);
            area.setLineWrap(true);
            area.setEditable(false);
            area.setCaretPosition(0);

            JScrollPane sp = new JScrollPane(area);
            sp.setPreferredSize(new Dimension(420, 220));

            String title = "Reason - " + req.getEmployeeId() + " (" + req.getLeaveType().name() + ")";
            JOptionPane.showMessageDialog(this, sp, title, JOptionPane.INFORMATION_MESSAGE);

        }, () -> JOptionPane.showMessageDialog(this, "Leave request not found.", "Error",
                JOptionPane.ERROR_MESSAGE));
    }

    private void approveSelected() {
        if (!user.canManageEmployees()) {
            JOptionPane.showMessageDialog(this, "Access denied.", "Permission",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a leave request first.", "Select",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String leaveId = model.getValueAt(row, 0).toString();

        repo.findById(leaveId).ifPresent(req -> {
            req.approve(user.getEmployeeNumber());
            repo.update(req);
        });

        loadPending();
        JOptionPane.showMessageDialog(this, "✅ Approved.");
    }

    private void denySelected() {
        if (!user.canManageEmployees()) {
            JOptionPane.showMessageDialog(this, "Access denied.", "Permission",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a leave request first.", "Select",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String leaveId = model.getValueAt(row, 0).toString();

        repo.findById(leaveId).ifPresent(req -> {
            req.deny(user.getEmployeeNumber());
            repo.update(req);
        });

        loadPending();
        JOptionPane.showMessageDialog(this, "✅ Denied.");
    }
}
