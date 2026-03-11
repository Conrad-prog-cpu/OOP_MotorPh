package gui;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import service.AuthenticatedUser;
import service.LeaveCreateRequest;
import service.LeaveService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;

public class LeaveRequestPanel extends JPanel {

    private final AuthenticatedUser currentUser;
    private final LeaveService leaveService;

    private final JComboBox<String> typeCombo = new JComboBox<>(
            new String[]{"Vacation", "Sick", "Emergency", "Maternity", "Paternity", "Other"}
    );

    private final DatePicker startDatePicker;
    private final DatePicker endDatePicker;
    private final JTextArea reasonArea = new JTextArea(4, 20);

    public LeaveRequestPanel(LeaveService leaveService, AuthenticatedUser currentUser) {
        this.leaveService = leaveService;
        this.currentUser = currentUser;

        DatePickerSettings startSettings = new DatePickerSettings();
        startSettings.setFormatForDatesCommonEra("yyyy-MM-dd");
        this.startDatePicker = new DatePicker(startSettings);

        DatePickerSettings endSettings = new DatePickerSettings();
        endSettings.setFormatForDatesCommonEra("yyyy-MM-dd");
        this.endDatePicker = new DatePicker(endSettings);

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setOpaque(false);

        buildUI();
    }

    private void buildUI() {
        JLabel title = new JLabel("Leave Request");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        addRow(
                form,
                gbc,
                row++,
                "Employee:",
                new JLabel(buildEmployeeDisplay())
        );
        addRow(form, gbc, row++, "Leave Type:", typeCombo);
        addRow(form, gbc, row++, "Start Date:", startDatePicker);
        addRow(form, gbc, row++, "End Date:", endDatePicker);

        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);

        JScrollPane reasonScroll = new JScrollPane(reasonArea);
        reasonScroll.setPreferredSize(new Dimension(320, 100));
        addRow(form, gbc, row++, "Reason:", reasonScroll);

        JButton submitButton = new JButton("Submit Request");
        submitButton.addActionListener(e -> submitLeave());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        bottom.add(submitButton);

        add(title, BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    private void submitLeave() {
        try {
            LocalDate startDate = startDatePicker.getDate();
            LocalDate endDate = endDatePicker.getDate();
            String leaveType = safe(typeCombo.getSelectedItem());
            String reason = reasonArea.getText().trim();

            if (startDate == null || endDate == null) {
                JOptionPane.showMessageDialog(
                        this,
                        "Start Date and End Date are required.",
                        "Validation",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            if (endDate.isBefore(startDate)) {
                JOptionPane.showMessageDialog(
                        this,
                        "End date must be on or after the start date.",
                        "Validation",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            if (reason.isBlank()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Reason is required.",
                        "Validation",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            LeaveCreateRequest request = new LeaveCreateRequest(
                    safe(currentUser.getEmployeeNumber()),
                    safe(currentUser.getFirstName()),
                    leaveType,
                    startDate,
                    endDate,
                    reason
            );

            boolean created = leaveService.fileLeaveRequest(currentUser, request);

            if (!created) {
                JOptionPane.showMessageDialog(
                        this,
                        "Failed to submit leave request.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            JOptionPane.showMessageDialog(this, "Leave request submitted successfully.");
            clearForm();

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Validation",
                    JOptionPane.WARNING_MESSAGE
            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void clearForm() {
        reasonArea.setText("");
        startDatePicker.clear();
        endDatePicker.clear();
        typeCombo.setSelectedIndex(0);
    }

    private String buildEmployeeDisplay() {
        String employeeNumber = safe(currentUser.getEmployeeNumber());
        String firstName = safe(currentUser.getFirstName());
        String role = currentUser != null && currentUser.getRole() != null
                ? currentUser.getRole().name()
                : "";

        return employeeNumber + " " + firstName + " (" + role + ")";
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, Component field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 1;
        panel.add(field, gbc);
    }

    private String safe(Object value) {
        return value == null ? "" : value.toString().trim();
    }
}