package gui;

import model.AttendanceLog;
import model.PayrollResult;
import model.Employee;

import repository.AttendanceRepository;
import repository.EmployeeRepository;
import service.PayrollService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.time.*;
import java.time.format.TextStyle;
import java.util.List;
import java.util.*;

public class ViewEmployeePanel extends JFrame {

    private static final Font UI_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 16);

    private static final Color GRADIENT_START = new Color(255, 204, 229);
    private static final Color GRADIENT_END   = new Color(255, 229, 180);

    private final EmployeeRepository employeeRepo;
    private final AttendanceRepository attendanceRepo;
    private final PayrollService payrollService;

    private final String employeeId;

    private final JComboBox<MonthYear> monthBox = new JComboBox<>();
    private final JTextPane reportPane = new JTextPane();

    public ViewEmployeePanel(
            Vector<Object> employeeData,
            EmployeeRepository employeeRepo,
            AttendanceRepository attendanceRepo,
            PayrollService payrollService
    ) {
        this.employeeRepo = employeeRepo;
        this.attendanceRepo = attendanceRepo;
        this.payrollService = payrollService;

        this.employeeId = Objects.toString(employeeData.get(0), "").trim();

        setTitle("Employee Details");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Load cache (if repo uses caching)
        this.employeeRepo.load();
        this.attendanceRepo.load();

        // ===== LEFT PANEL =====
        JPanel leftPanel = buildLeftDetailsPanel(employeeData);
        JScrollPane leftScroll = new JScrollPane(leftPanel);
        leftScroll.setBorder(null);
        leftScroll.getVerticalScrollBar().setUI(createScrollBarUI());

        // ===== RIGHT PANEL =====
        JPanel rightPanel = buildRightPayrollPanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll, rightPanel);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerLocation(0.5);
        splitPane.setOneTouchExpandable(true);
        add(splitPane);

        loadAvailableMonths();

        setVisible(true);
    }

    public ViewEmployeePanel(
            String employeeId,
            EmployeeRepository employeeRepo,
            AttendanceRepository attendanceRepo,
            PayrollService payrollService
    ) {
        this(toVectorFromEmployeeId(employeeId, employeeRepo), employeeRepo, attendanceRepo, payrollService);
    }

    // =========================
    // LEFT DETAILS PANEL
    // =========================
    private JPanel buildLeftDetailsPanel(Vector<Object> employeeData) {
        JPanel leftPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setPaint(new GradientPaint(0, 0, GRADIENT_START, 0, getHeight(), GRADIENT_END));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        leftPanel.setOpaque(false);
        leftPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        String[][] sections = {
                {"Personal Information", "Employee No.", "Last Name", "First Name", "Birthday", "Address", "Phone Number"},
                {"Government Identifications", "SSS No.", "PhilHealth No.", "TIN No.", "PAG-IBIG No."},
                {"Job Information", "Status", "Position", "Immediate Supervisor"},
                {"Compensation & Benefits", "Basic Salary", "Rice Subsidy", "Phone Allowance", "Clothing Allowance", "Gross Semi-monthly Rate", "Hourly Rate"}
        };

        int dataIndex = 0;
        gbc.gridy = 0;

        for (String[] section : sections) {
            JLabel sectionTitle = new JLabel(section[0]);
            sectionTitle.setFont(HEADER_FONT);
            sectionTitle.setForeground(new Color(70, 70, 70));

            gbc.gridx = 0;
            gbc.gridwidth = 2;
            leftPanel.add(sectionTitle, gbc);
            gbc.gridy++;
            gbc.gridwidth = 1;

            for (int i = 1; i < section.length; i++) {
                gbc.gridx = 0;
                JLabel label = new JLabel(section[i] + ":");
                label.setFont(UI_FONT);
                leftPanel.add(label, gbc);

                gbc.gridx = 1;

                String val = (dataIndex < employeeData.size())
                        ? Objects.toString(employeeData.get(dataIndex), "")
                        : "";

                JTextArea dataField = new JTextArea(val);
                dataField.setWrapStyleWord(true);
                dataField.setLineWrap(true);
                dataField.setEditable(false);
                dataField.setOpaque(false);
                dataField.setFont(UI_FONT);
                dataField.setBorder(null);

                leftPanel.add(dataField, gbc);

                gbc.gridy++;
                dataIndex++;
            }

            gbc.gridy++;
        }

        return leftPanel;
    }

    // =========================
    // RIGHT PAYROLL PANEL
    // =========================
    private JPanel buildRightPayrollPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setPaint(new GradientPaint(0, 0, GRADIENT_START, 0, getHeight(), GRADIENT_END));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        rightPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        monthBox.setFont(UI_FONT);

        monthBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                c.setFont(UI_FONT);

                if (value instanceof MonthYear my && my.isPlaceholder()) {
                    c.setForeground(Color.GRAY);
                    setText("Select Month");
                } else {
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });

        JButton computeBtn = new JButton("Compute");
        computeBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        computeBtn.setBackground(Color.BLACK);
        computeBtn.setForeground(Color.WHITE);
        computeBtn.setFocusPainted(false);
        computeBtn.setBorder(BorderFactory.createEmptyBorder());
        computeBtn.setPreferredSize(new Dimension(130, 40));

        reportPane.setContentType("text/html");
        reportPane.setEditable(false);
        reportPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        reportPane.setBackground(UIManager.getColor("Panel.background"));

        JScrollPane reportScroll = new JScrollPane(reportPane);
        reportScroll.setBorder(null);
        reportScroll.getVerticalScrollBar().setUI(createScrollBarUI());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setOpaque(false);
        topPanel.add(monthBox);
        topPanel.add(Box.createHorizontalStrut(10));
        topPanel.add(computeBtn);

        computeBtn.addActionListener(e -> computeSelectedMonth());

        rightPanel.add(topPanel, BorderLayout.NORTH);
        rightPanel.add(reportScroll, BorderLayout.CENTER);

        return rightPanel;
    }

    // =========================
    // MONTHS + COMPUTE
    // =========================
        private void loadAvailableMonths() {
         monthBox.removeAllItems();
         monthBox.addItem(new MonthYear(null, 0));
         
         attendanceRepo.load();

         LocalDate start = LocalDate.of(2000, 1, 1);
         LocalDate end = LocalDate.now();

         List<AttendanceLog> logs = attendanceRepo.findByEmployeeAndDateRange(employeeId.trim(), start, end);

         SortedSet<MonthYear> months = new TreeSet<>();
         for (AttendanceLog log : logs) {
             LocalDate d = log.getDate();
             if (d != null) months.add(new MonthYear(d.getMonth(), d.getYear()));
         }

         for (MonthYear my : months) monthBox.addItem(my);
     }

        private void computeSelectedMonth() {
            Object selected = monthBox.getSelectedItem();
            if (!(selected instanceof MonthYear my) || my.isPlaceholder()) {
                JOptionPane.showMessageDialog(this, "Please select a valid month.");
                return;
            }

            try {
                PayrollResult result = payrollService.computeForMonth(employeeId, my.year, my.month.getValue());
                reportPane.setText(buildPayrollHtml(result, my));
                reportPane.setCaretPosition(0);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Calculation error: " + ex.getClass().getSimpleName() + " - " + ex.getMessage());

            }
        }
        //Overload instances
        private String buildPayrollHtml(PayrollResult r, MonthYear my) {
            Employee emp = employeeRepo.findById(employeeId.trim()).orElse(null);
            return buildPayrollHtml(r, my, emp);
        }
    

       private String buildPayrollHtml(PayrollResult r, MonthYear my, Employee emp) {
            String monthName = my.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH);

                // Employee benefits (from Employee, not PayrollResult)
                double rice = emp != null ? safeBD(emp.getRiceSubsidy()) : 0;
                double phone = emp != null ? safeBD(emp.getPhoneAllowance()) : 0;
                double clothing = emp != null ? safeBD(emp.getClothingAllowance()) : 0;
                double totalBenefits = rice + phone + clothing;

                // Contributions
                double sss = safeBD(r.getContributions() != null ? r.getContributions().getSss() : null);
                double ph  = safeBD(r.getContributions() != null ? r.getContributions().getPhilHealth() : null);
                double pi  = safeBD(r.getContributions() != null ? r.getContributions().getPagIbig() : null);

                // Tax
                double tax = safeBD(r.getWithholdingTax());

                // Late / OT
                int lateMinutes = r.getLateMinutes();
                double lateDeduction = safeBD(r.getLateDeduction());

                double overtimeHours = safeBD(r.getOvertimeHours());
                double overtimePay = safeBD(r.getOvertimePay());

                // Gross before/after late
                double grossAfterLate = safeBD(r.getGrossPay());
                double grossBeforeLate = grossAfterLate + lateDeduction; // Option A requirement

                double totalDeductions = sss + ph + pi + tax;

                return String.format("""
                    <html>
                    <body style='font-family:Calibri; font-size:12px;'>
                    <pre>
            <span style='font-size:16px; font-weight:bold;'>===== PAYROLL REPORT =====</span>
            <span style='font-size:13px; font-weight:bold;'>%s %d</span>

            <b>WORK:</b>
            • Total Hours Worked: %s
            • Overtime Hours: %,.2f
            • Late Minutes: %d

            <b>BENEFITS / ALLOWANCES:</b>
            • Rice Subsidy: ₱%,.2f
            • Phone Allowance: ₱%,.2f
            • Clothing Allowance: ₱%,.2f
            • <b>Total Benefits:</b> ₱%,.2f

            <b>PAY:</b>
            • Basic Pay: ₱%,.2f
            • Overtime Pay: ₱%,.2f
            • <b>Gross Before Late Deduction:</b> ₱%,.2f
            • Late Deduction: -₱%,.2f
            • <b>Gross After Late Deduction:</b> ₱%,.2f

            <b>DEDUCTIONS:</b>
            • Withholding Tax: ₱%,.2f
            • SSS: ₱%,.2f
            • PhilHealth: ₱%,.2f
            • Pag-IBIG: ₱%,.2f
            • <b>Total Deductions:</b> ₱%,.2f

            <b><span style='color:green;'>NET PAY: ₱%,.2f</span></b>
                    </pre>
                    </body>
                    </html>
                    """,
                    monthName, my.year,

                    safeObj(r.getHoursWorked()),
                    overtimeHours,
                    lateMinutes,

                    rice, phone, clothing, totalBenefits,

                    safeBD(r.getBasicPay()),
                    overtimePay,
                    grossBeforeLate,
                    lateDeduction,
                    grossAfterLate,

                    tax, sss, ph, pi, totalDeductions,

                    safeBD(r.getNetPay())
                );
            }

            private double safeBD(java.math.BigDecimal bd) {
            return (bd == null) ? 0.0 : bd.doubleValue();
            }

            
            private String safeObj(Object o) {
            return (o == null) ? "0" : o.toString();
            }


    // =========================
    // SCROLLBAR UI
    // =========================
    private static BasicScrollBarUI createScrollBarUI() {
        return new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = Color.WHITE;
                this.trackColor = GRADIENT_END;
            }

            @Override
            protected Dimension getMinimumThumbSize() {
                return new Dimension(8, 30);
            }
        };
    }

    // =========================
    // BUILD VECTOR FROM REPO
    // =========================
    private static Vector<Object> toVectorFromEmployeeId(String employeeId, EmployeeRepository repo) {
        repo.load();

        Employee emp = null;

        // Supports either:
        // Employee findById(String)
        // or Optional<Employee> findById(String)
        try {
            Object result = repo.findById(employeeId);

            if (result instanceof Optional<?> opt) {
                Object o = opt.orElse(null);
                if (o instanceof Employee e) emp = e;
            } else if (result instanceof Employee e) {
                emp = e;
            }
        } catch (Exception ignored) {
            emp = null;
        }

        Vector<Object> v = new Vector<>();
        v.add(employeeId);

        if (emp == null) {
            return v;
        }

        // ORDER must match your left section headings
        v.clear();

        // Personal Information
        v.add(emp.getEmployeeID());
        v.add(emp.getLastName());
        v.add(emp.getFirstName());
        v.add(emp.getBirthday() != null ? emp.getBirthday().toString() : "");
        v.add(emp.getAddress());
        v.add(emp.getPhoneNumber());

        // Gov IDs
        v.add(emp.getSssNumber());
        v.add(emp.getPhilHealthNumber());
        v.add(emp.getTinNumber());
        v.add(emp.getPagIbigNumber());

        // Job
        v.add(emp.getStatus());
        v.add(emp.getPosition());
        v.add(emp.getImmediateSupervisor());

        // Pay
        v.add(emp.getBasicSalary());
        v.add(emp.getRiceSubsidy());
        v.add(emp.getPhoneAllowance());
        v.add(emp.getClothingAllowance());
        v.add(emp.getSemiMonthlyRate());
        v.add(emp.getHourlyRate());

        return v;
    }

    // =========================
    // MonthYear type
    // =========================
    private static class MonthYear implements Comparable<MonthYear> {
        final Month month;
        final int year;

        MonthYear(Month month, int year) {
            this.month = month;
            this.year = year;
        }

        boolean isPlaceholder() {
            return month == null || year == 0;
        }

        @Override
        public String toString() {
            if (isPlaceholder()) return "Select Month";
            return month.getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + year;
        }

        @Override
        public int compareTo(MonthYear o) {
            if (this.isPlaceholder()) return -1;
            if (o.isPlaceholder()) return 1;

            int c = Integer.compare(this.year, o.year);
            if (c != 0) return c;
            return Integer.compare(this.month.getValue(), o.month.getValue());
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof MonthYear other)) return false;
            return this.year == other.year && this.month == other.month;
        }

        @Override
        public int hashCode() {
            return Objects.hash(month, year);
        }
    }
}
