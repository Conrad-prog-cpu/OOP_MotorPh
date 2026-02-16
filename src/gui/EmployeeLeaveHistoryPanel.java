/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gui;

import model.LeaveRequest;
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

public class EmployeeLeaveHistoryPanel extends JPanel {

    private final User user;
    private final LeaveRepository repo;

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"LeaveId", "Type", "Date Range", "Status", "Reviewed By", "Reviewed At"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(model);

    private final JButton refreshBtn = new JButton("Refresh");
    private final JButton viewDetailsBtn = new JButton("View Details");

    public EmployeeLeaveHistoryPanel(User user) {
        this.user = user;
        this.repo = new FileLeaveRepository();

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setOpaque(false);

        JLabel title = new JLabel("My Leave Requests");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));

        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.getSelectionModel().addListSelectionListener(e -> {
            viewDetailsBtn.setEnabled(table.getSelectedRow() != -1);
        });

        // Double-click = open details
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    showDetailsPopup();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);

        refreshBtn.addActionListener(e -> loadMyLeaves());
        viewDetailsBtn.addActionListener(e -> showDetailsPopup());
        viewDetailsBtn.setEnabled(false);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);
        top.add(refreshBtn);
        top.add(viewDetailsBtn);

        add(title, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(top, BorderLayout.SOUTH);

        loadMyLeaves();
    }

    private void loadMyLeaves() {
        model.setRowCount(0);

        // ✅ show only current user's leaves (employeeId = username in our minimum version)
        List<LeaveRequest> mine = repo.findByEmployeeId(user.getEmployeeNumber());

        // Optional: newest first
        mine.sort((a, b) -> b.getRequestedAt().compareTo(a.getRequestedAt()));

        for (LeaveRequest r : mine) {
            model.addRow(new Object[]{
                    r.getLeaveId(),
                    r.getLeaveType().name(),
                    formatRange(r),
                    r.getStatus().name(),
                    r.getReviewedBy() == null ? "" : r.getReviewedBy(),
                    r.getReviewedAt() == null ? "" : r.getReviewedAt().toString()
            });
        }
    }

    private String formatRange(LeaveRequest r) {
        long days = ChronoUnit.DAYS.between(r.getStartDate(), r.getEndDate()) + 1;
        String dayLabel = days == 1 ? "day" : "days";
        return r.getStartDate() + " \u2192 " + r.getEndDate() + " (" + days + " " + dayLabel + ")";
    }

    private void showDetailsPopup() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a request first.", "Select",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String leaveId = model.getValueAt(row, 0).toString();

        repo.findById(leaveId).ifPresentOrElse(req -> {

            String reviewedBy = (req.getReviewedBy() == null || req.getReviewedBy().isBlank()) ? "—" : req.getReviewedBy();
            String reviewedAt = (req.getReviewedAt() == null) ? "—" : req.getReviewedAt().toString();

            JTextArea area = new JTextArea();
            area.setEditable(false);
            area.setLineWrap(true);
            area.setWrapStyleWord(true);

            area.setText(
                    "Employee: " + req.getEmployeeName() + "\n" +
                    "Type: " + req.getLeaveType().name() + "\n" +
                    "Date Range: " + formatRange(req) + "\n" +
                    "Status: " + req.getStatus().name() + "\n" +
                    "Requested At: " + (req.getRequestedAt() == null ? "" : req.getRequestedAt()) + "\n" +
                    "Reviewed By: " + reviewedBy + "\n" +
                    "Reviewed At: " + reviewedAt + "\n\n" +
                    "Reason:\n" + req.getReason()
            );

            JScrollPane sp = new JScrollPane(area);
            sp.setPreferredSize(new Dimension(450, 260));

            JOptionPane.showMessageDialog(this, sp, "Leave Details", JOptionPane.INFORMATION_MESSAGE);

        }, () -> JOptionPane.showMessageDialog(this, "Leave request not found.", "Error",
                JOptionPane.ERROR_MESSAGE));
    }
}

