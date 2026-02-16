package gui;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import model.Employee;
import model.ProbationaryEmployee;
import model.RegularEmployee;
import repository.EmployeeRepository;
import repository.FileEmployeeRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import javax.swing.border.Border;

public class AddEmployeePanel extends JPanel {

    private final EmployeeRepository employeeRepo;
    private final Runnable onEmployeeAdded; // callback after successful save

    private final Map<String, JComponent> fields = new LinkedHashMap<>();

    private final JButton submitButton = new JButton("Add Employee");
    private final JButton backButton   = new JButton("Back");

    private final JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));

    private static final String[] EXTRA_FIELDS = {"Birthday", "Phone Number"};

    private static final Color GRADIENT_START = new Color(0xFFD1DC);
    private static final Color GRADIENT_END   = new Color(0xFFE4CC);

    private static final Border DEFAULT_BORDER = UIManager.getBorder("TextField.border");
    private static final Border ERROR_BORDER = new LineBorder(Color.RED, 2);

    public AddEmployeePanel(EmployeeRepository employeeRepo, Runnable onEmployeeAdded) {
        this.employeeRepo = employeeRepo;
        this.onEmployeeAdded = onEmployeeAdded;

        setOpaque(false);
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        employeeRepo.load();

        formPanel.setOpaque(false);

        LinkedHashMap<String, Boolean> headers = buildFinalHeaders();

        for (Map.Entry<String, Boolean> entry : headers.entrySet()) {
            String header = entry.getKey();
            boolean required = entry.getValue();

            formPanel.add(buildLabelCell(header, required));
            JComponent input = buildInputFor(header);
            formPanel.add(input);

            fields.put(header, input);
        }

        JPanel formContainer = new JPanel(new BorderLayout());
        formContainer.setOpaque(false);
        formContainer.setBorder(new EmptyBorder(10, 10, 10, 10));
        formContainer.add(formPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(formContainer);
        scrollPane.setPreferredSize(new Dimension(520, 460));
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);

        styleButtons();
        wireEvents();

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(10, 10, 10, 10));
        bottom.add(new JLabel("* Required fields"));
        bottom.add(backButton);
        bottom.add(submitButton);

        add(scrollPane, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    // Convenience constructor
    public AddEmployeePanel(Runnable onEmployeeAdded) {
        this(new FileEmployeeRepository(), onEmployeeAdded);
    }

    // ---------------- UI building ----------------

    private JPanel buildLabelCell(String header, boolean required) {
        JPanel labelPanel = new JPanel(new BorderLayout());
        labelPanel.setOpaque(false);

        JLabel label = new JLabel(header + ":");

        if (required) {
            JLabel asterisk = new JLabel("*");
            asterisk.setForeground(Color.RED);
            asterisk.setFont(asterisk.getFont().deriveFont(Font.BOLD));
            label.setFont(label.getFont().deriveFont(Font.BOLD));
            label.setToolTipText("Required field");

            labelPanel.add(label, BorderLayout.WEST);
            labelPanel.add(asterisk, BorderLayout.EAST);
        } else {
            label.setForeground(Color.GRAY);
            label.setFont(label.getFont().deriveFont(Font.ITALIC));
            labelPanel.add(label, BorderLayout.WEST);
        }

        return labelPanel;
    }

    private JComponent buildInputFor(String header) {
        String h = header.trim().toLowerCase();

        switch (h) {
            case "employee #" -> {
                JTextField tf = new JTextField();
                restrictNumeric(tf);
                addTooltipOnFocus(tf, "Numbers only");
                return tf;
            }
            case "birthday" -> {
                DatePickerSettings settings = new DatePickerSettings();
                settings.setFormatForDatesCommonEra("yyyy/MM/dd");

                DatePicker picker = new DatePicker(settings);
                addTooltipOnFocus(picker.getComponentDateTextField(), "Pick date (yyyy/MM/dd)");
                return picker;
            }
            case "phone number", "sss #", "philhealth #", "tin #", "pag-ibig #" -> {
                JTextField tf = new JTextField();
                restrictNumeric(tf);
                addTooltipOnFocus(tf, "Numbers only");
                return tf;
            }
            case "status" -> {
                return new JComboBox<>(new String[]{"Regular", "Probationary"});
            }
            default -> {
                return new JTextField();
            }
        }
    }

    private void styleButtons() {
        styleRoundedButton(submitButton, Color.BLACK, 140, 38);
        styleRoundedButton(backButton, Color.BLACK, 90, 38);
    }

    private void wireEvents() {
        submitButton.addActionListener(this::onSubmit);
        backButton.addActionListener(e -> closeWindow());
    }

    private void styleRoundedButton(JButton button, Color bg, int w, int h) {
        button.setPreferredSize(new Dimension(w, h));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 20, 20);
                super.paint(g2, c);
                g2.dispose();
            }
        });
    }

    // ---------------- Background ----------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        GradientPaint gp = new GradientPaint(0, 0, GRADIENT_START, 0, getHeight(), GRADIENT_END);
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }

    // ---------------- Submit flow ----------------

    private void onSubmit(ActionEvent e) {
        resetBorders();

        Map<String, String> values = collectValues();

        List<String> errors = validate(values);
        if (!errors.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fix the following:\n" + String.join("\n", errors),
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String employeeId = values.getOrDefault("Employee #", "").trim();
        if (employeeExists(employeeId)) {
            setError("Employee #");
            JOptionPane.showMessageDialog(this,
                    "Employee # already exists!",
                    "Duplicate Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        Employee emp = buildEmployee(values);

        boolean ok = employeeRepo.addEmployee(emp);
        if (!ok) {
            JOptionPane.showMessageDialog(this,
                    "❌ Failed to add employee.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        employeeRepo.load(); // refresh cache for any future reads

        JOptionPane.showMessageDialog(this, "✅ Employee added successfully!");
        clearFields();

        if (onEmployeeAdded != null) onEmployeeAdded.run();

        closeWindow();
    }

    // ---------------- Values & Validation ----------------

    private Map<String, String> collectValues() {
        Map<String, String> out = new HashMap<>();

        for (Map.Entry<String, JComponent> entry : fields.entrySet()) {
            String header = entry.getKey();
            JComponent c = entry.getValue();

            String value = "";

            if (c instanceof JTextField tf) {
                value = tf.getText().trim();
            } else if (c instanceof DatePicker dp) {
                value = dp.getDate() != null ? dp.getDate().toString() : "";
            } else if (c instanceof JComboBox<?> cb) {
                value = Objects.toString(cb.getSelectedItem(), "").trim();
            }

            out.put(header, value);
        }

        return out;
    }

    private List<String> validate(Map<String, String> values) {
        List<String> errors = new ArrayList<>();

        for (String header : fields.keySet()) {
            String val = values.getOrDefault(header, "").trim();

            if (isRequired(header) && val.isEmpty()) {
                setError(header);
                errors.add("- " + header + " is required.");
                continue;
            }

            if (isNumericField(header) && !val.isEmpty() && !val.matches("\\d+")) {
                setError(header);
                errors.add("- " + header + " must be numeric.");
            }
        }

        return errors;
    }

    private Employee buildEmployee(Map<String, String> v) {
        String employeeId = v.getOrDefault("Employee #", "");
        String status = v.getOrDefault("Status", "Regular");
        LocalDate birthday = parseDate(v.get("Birthday"));

        BigDecimal basicSalary     = money(v.get("Basic Salary"));
        BigDecimal riceSubsidy     = money(v.get("Rice Subsidy"));
        BigDecimal phoneAllowance  = money(v.get("Phone Allowance"));
        BigDecimal clothingAllow   = money(v.get("Clothing Allowance"));
        BigDecimal semiMonthlyRate = money(v.get("Gross Semi-monthly Rate"));
        BigDecimal hourlyRate      = money(v.get("Hourly Rate"));

        if ("Probationary".equalsIgnoreCase(status)) {
            return new ProbationaryEmployee(
                    employeeId,
                    v.getOrDefault("Last Name", ""),
                    v.getOrDefault("First Name", ""),
                    birthday,
                    v.getOrDefault("Address", ""),
                    v.getOrDefault("Phone Number", ""),
                    v.getOrDefault("SSS #", ""),
                    v.getOrDefault("Philhealth #", ""),
                    v.getOrDefault("TIN #", ""),
                    v.getOrDefault("Pag-ibig #", ""),
                    status,
                    v.getOrDefault("Position", ""),
                    v.getOrDefault("Immediate Supervisor", ""),
                    basicSalary, riceSubsidy, phoneAllowance, clothingAllow, semiMonthlyRate, hourlyRate
            );
        }

        return new RegularEmployee(
                employeeId,
                v.getOrDefault("Last Name", ""),
                v.getOrDefault("First Name", ""),
                birthday,
                v.getOrDefault("Address", ""),
                v.getOrDefault("Phone Number", ""),
                v.getOrDefault("SSS #", ""),
                v.getOrDefault("Philhealth #", ""),
                v.getOrDefault("TIN #", ""),
                v.getOrDefault("Pag-ibig #", ""),
                status,
                v.getOrDefault("Position", ""),
                v.getOrDefault("Immediate Supervisor", ""),
                basicSalary, riceSubsidy, phoneAllowance, clothingAllow, semiMonthlyRate, hourlyRate
        );
    }

    // ---------------- Repository / Data helpers ----------------

    private boolean employeeExists(String employeeNo) {
        if (employeeNo == null || employeeNo.isBlank()) return false;
        return employeeRepo.findRowByEmployeeNo(employeeNo) != null;
    }

    private LinkedHashMap<String, Boolean> buildFinalHeaders() {
        LinkedHashMap<String, Boolean> out = new LinkedHashMap<>();

        List<String> fileHeaders = employeeRepo.getHeaders();
        if (fileHeaders != null && !fileHeaders.isEmpty()) {
            for (String h : fileHeaders) out.put(h, isRequired(h));
        } else {
            for (String h : defaultHeaders()) out.put(h, isRequired(h));
        }

        for (String extra : EXTRA_FIELDS) out.putIfAbsent(extra, true);

        return out;
    }

    // ---------------- UI state helpers ----------------

    private void setError(String header) {
        JComponent c = fields.get(header);
        if (c != null) c.setBorder(ERROR_BORDER);
    }

    private void resetBorders() {
        for (JComponent c : fields.values()) {
            c.setBorder(DEFAULT_BORDER);
        }
    }

    private void clearFields() {
        for (JComponent c : fields.values()) {
            if (c instanceof JTextField tf) tf.setText("");
            else if (c instanceof DatePicker dp) dp.clear();
            else if (c instanceof JComboBox<?> cb) cb.setSelectedIndex(0);

            c.setBorder(DEFAULT_BORDER);
        }
    }

    private void closeWindow() {
        Window w = SwingUtilities.getWindowAncestor(this);
        if (w != null) w.dispose();
    }

    // ---------------- Field rules ----------------

    private boolean isRequired(String header) {
        return header.equalsIgnoreCase("Employee #")
                || header.equalsIgnoreCase("Last Name")
                || header.equalsIgnoreCase("First Name")
                || header.equalsIgnoreCase("Birthday")
                || header.equalsIgnoreCase("Phone Number")
                || header.equalsIgnoreCase("SSS #")
                || header.equalsIgnoreCase("Philhealth #")
                || header.equalsIgnoreCase("TIN #")
                || header.equalsIgnoreCase("Pag-ibig #");
    }

    private boolean isNumericField(String header) {
        return header.equalsIgnoreCase("Employee #")
                || header.equalsIgnoreCase("Phone Number")
                || header.equalsIgnoreCase("SSS #")
                || header.equalsIgnoreCase("Philhealth #")
                || header.equalsIgnoreCase("TIN #")
                || header.equalsIgnoreCase("Pag-ibig #");
    }

    // ---------------- Parsing helpers ----------------

    private BigDecimal money(String s) {
        if (s == null || s.isBlank()) return BigDecimal.ZERO;
        String clean = s.replace("\"", "").replace(",", "").trim();
        try { return new BigDecimal(clean); }
        catch (Exception ex) { return BigDecimal.ZERO; }
    }

    private LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return null;

        s = s.trim();
        try {
            return LocalDate.parse(s); // yyyy-MM-dd (DatePicker toString)
        } catch (Exception ex) {
            // fallback: yyyy/MM/dd
            try {
                if (s.contains("/")) {
                    String[] p = s.split("/");
                    return LocalDate.of(
                            Integer.parseInt(p[0]),
                            Integer.parseInt(p[1]),
                            Integer.parseInt(p[2])
                    );
                }
            } catch (Exception ignore) {}
        }
        return null;
    }

    private void addTooltipOnFocus(JTextField textField, String tooltipText) {
        textField.setToolTipText(tooltipText);
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                ToolTipManager.sharedInstance().mouseMoved(
                        new MouseEvent(
                                textField,
                                MouseEvent.MOUSE_MOVED,
                                System.currentTimeMillis(),
                                0,
                                1, 1,
                                0,
                                false
                        )
                );
            }
        });
    }

    private void restrictNumeric(JTextField tf) {
        ((AbstractDocument) tf.getDocument()).setDocumentFilter(new NumericDocumentFilter());
    }

    private String[] defaultHeaders() {
        return new String[]{
                "Employee #", "Last Name", "First Name", "Birthday", "Address", "Phone Number",
                "SSS #", "Philhealth #", "TIN #", "Pag-ibig #", "Status", "Position",
                "Immediate Supervisor", "Basic Salary", "Rice Subsidy", "Phone Allowance",
                "Clothing Allowance", "Gross Semi-monthly Rate", "Hourly Rate"
        };
    }

    // numeric-only filter
    private static class NumericDocumentFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            if (string != null && string.matches("\\d+")) super.insertString(fb, offset, string, attr);
        }
        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            if (text != null && text.matches("\\d+")) super.replace(fb, offset, length, text, attrs);
        }
    }
}
