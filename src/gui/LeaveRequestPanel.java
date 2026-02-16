package gui;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;

import model.LeaveRequest;
import model.LeaveStatus;
import model.LeaveType;
import model.User;
import repository.FileLeaveRepository;
import repository.LeaveRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class LeaveRequestPanel extends JPanel {

    private final User user;
    private final LeaveRepository repo;

    private final JComboBox<LeaveType> typeCombo = new JComboBox<>(LeaveType.values());

    // ✅ DatePickers
    private final DatePicker startDatePicker;
    private final DatePicker endDatePicker;

    private final JTextArea reasonArea = new JTextArea(4, 20);

    public LeaveRequestPanel(User user) {
        this.user = user;
        this.repo = new FileLeaveRepository();

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setOpaque(false);

        JLabel title = new JLabel("Leave Request");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));

        // ✅ DatePicker settings
        DatePickerSettings startSettings = new DatePickerSettings();
        startSettings.setFormatForDatesCommonEra("yyyy-MM-dd");
        startDatePicker = new DatePicker(startSettings);

        DatePickerSettings endSettings = new DatePickerSettings();
        endSettings.setFormatForDatesCommonEra("yyyy-MM-dd");
        endDatePicker = new DatePicker(endSettings);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        addRow(form, gbc, row++, "Employee:", new JLabel(user.getEmployeeNumber()+ " " + user.getFirstName() + " (" + user.getRole() + ")"));
        addRow(form, gbc, row++, "Leave Type:", typeCombo);
        addRow(form, gbc, row++, "Start Date:", startDatePicker);
        addRow(form, gbc, row++, "End Date:", endDatePicker);

        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        JScrollPane reasonScroll = new JScrollPane(reasonArea);
        reasonScroll.setPreferredSize(new Dimension(320, 100));
        addRow(form, gbc, row++, "Reason:", reasonScroll);

        JButton submit = new JButton("Submit Request");
        submit.addActionListener(e -> submitLeave());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        bottom.add(submit);

        add(title, BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    private void submitLeave() {
        try {
            LocalDate start = startDatePicker.getDate();
            LocalDate end = endDatePicker.getDate();

            if (start == null || end == null) {
                JOptionPane.showMessageDialog(this, "Start Date and End Date are required.",
                        "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (end.isBefore(start)) {
                JOptionPane.showMessageDialog(this, "End date must be on/after start date.",
                        "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String reason = reasonArea.getText().trim();
            if (reason.isBlank()) {
                JOptionPane.showMessageDialog(this, "Reason is required.",
                        "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String leaveId = UUID.randomUUID().toString();

            LeaveRequest req = new LeaveRequest(
                    leaveId,
                     user.getEmployeeNumber(),  // ✅ employeeId mapping
                     user.getFirstName(),       // or full name if you have
                    (LeaveType) typeCombo.getSelectedItem(),
                    start,
                    end,
                    reason,
                    LeaveStatus.PENDING,
                    LocalDateTime.now(),
                    "",
                    null
            );

            repo.create(req);

            JOptionPane.showMessageDialog(this, "✅ Leave request submitted (PENDING).");

            // Clear form
            reasonArea.setText("");
            startDatePicker.clear();
            endDatePicker.clear();
            typeCombo.setSelectedIndex(0);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addRow(JPanel p, GridBagConstraints gbc, int row, String label, Component field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        p.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 1;
        p.add(field, gbc);
    }
}
