package gui;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import service.EmployeeCreateRequest;
import service.EmployeeService;
import service.EmployeeValidationResult;
import service.EmployeeValidator;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class AddEmployeePanel extends JPanel {

    private static final Color GRADIENT_START = new Color(0xFFD1DC);
    private static final Color GRADIENT_END = new Color(0xFFE4CC);

    private static final Border DEFAULT_BORDER = UIManager.getBorder("TextField.border");
    private static final Border ERROR_BORDER = new LineBorder(Color.RED, 2);

    private static final String EMPLOYEE_ID = "Employee #";
    private static final String LAST_NAME = "Last Name";
    private static final String FIRST_NAME = "First Name";
    private static final String BIRTHDAY = "Birthday";
    private static final String ADDRESS = "Address";
    private static final String PHONE_NUMBER = "Phone Number";
    private static final String SSS = "SSS #";
    private static final String PHILHEALTH = "Philhealth #";
    private static final String TIN = "TIN #";
    private static final String PAG_IBIG = "Pag-ibig #";
    private static final String STATUS = "Status";
    private static final String POSITION = "Position";
    private static final String IMMEDIATE_SUPERVISOR = "Immediate Supervisor";
    private static final String BASIC_SALARY = "Basic Salary";
    private static final String RICE_SUBSIDY = "Rice Subsidy";
    private static final String PHONE_ALLOWANCE = "Phone Allowance";
    private static final String CLOTHING_ALLOWANCE = "Clothing Allowance";
    private static final String GROSS_SEMI_MONTHLY_RATE = "Gross Semi-monthly Rate";
    private static final String HOURLY_RATE = "Hourly Rate";

    private static final String[] FORM_HEADERS = {
            EMPLOYEE_ID,
            LAST_NAME,
            FIRST_NAME,
            BIRTHDAY,
            ADDRESS,
            PHONE_NUMBER,
            SSS,
            PHILHEALTH,
            TIN,
            PAG_IBIG,
            STATUS,
            POSITION,
            IMMEDIATE_SUPERVISOR,
            BASIC_SALARY,
            RICE_SUBSIDY,
            PHONE_ALLOWANCE,
            CLOTHING_ALLOWANCE,
            GROSS_SEMI_MONTHLY_RATE,
            HOURLY_RATE
    };

    private static final Set<String> REQUIRED_FIELDS = Set.of(
            EMPLOYEE_ID,
            LAST_NAME,
            FIRST_NAME,
            BIRTHDAY,
            PHONE_NUMBER,
            SSS,
            PHILHEALTH,
            TIN,
            PAG_IBIG
    );

    private static final Set<String> NUMERIC_FIELDS = Set.of(
            EMPLOYEE_ID,
            PHONE_NUMBER,
            SSS,
            PHILHEALTH,
            TIN,
            PAG_IBIG
    );

    private final EmployeeService employeeService;
    private final Runnable onEmployeeAdded;

    private final Map<String, JComponent> fields = new LinkedHashMap<>();

    private final JButton submitButton = new JButton("Add Employee");
    private final JButton backButton = new JButton("Back");
    private final JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));

    public AddEmployeePanel(EmployeeService employeeService, Runnable onEmployeeAdded) {
        this.employeeService = Objects.requireNonNull(employeeService, "employeeService is required");
        this.onEmployeeAdded = onEmployeeAdded;

        setOpaque(false);
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        formPanel.setOpaque(false);

        buildForm();
        styleButtons();
        wireEvents();

        JPanel formContainer = new JPanel(new BorderLayout());
        formContainer.setOpaque(false);
        formContainer.setBorder(new EmptyBorder(10, 10, 10, 10));
        formContainer.add(formPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(formContainer);
        scrollPane.setPreferredSize(new Dimension(520, 460));
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(10, 10, 10, 10));
        bottom.add(new JLabel("* Required fields"));
        bottom.add(backButton);
        bottom.add(submitButton);

        add(scrollPane, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    private void buildForm() {
        for (String header : FORM_HEADERS) {
            formPanel.add(buildLabelCell(header, isRequired(header)));

            JComponent input = buildInputFor(header);
            formPanel.add(input);

            fields.put(header, input);
        }
    }

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
        String normalized = header.trim().toLowerCase();

        switch (normalized) {
            case "employee #" -> {
                JTextField textField = new JTextField();
                restrictNumeric(textField);
                addTooltipOnFocus(textField, "Numbers only");
                return textField;
            }
            case "birthday" -> {
                DatePickerSettings settings = new DatePickerSettings();
                settings.setFormatForDatesCommonEra("yyyy/MM/dd");

                DatePicker picker = new DatePicker(settings);
                addTooltipOnFocus(picker.getComponentDateTextField(), "Pick date (yyyy/MM/dd)");
                return picker;
            }
            case "phone number", "sss #", "philhealth #", "tin #", "pag-ibig #" -> {
                JTextField textField = new JTextField();
                restrictNumeric(textField);
                addTooltipOnFocus(textField, "Numbers only");
                return textField;
            }
            case "status" -> {
                return new JComboBox<>(new String[]{"Regular", "Probationary"});
            }
            default -> {
                return new JTextField();
            }
        }
    }

    private void wireEvents() {
        submitButton.addActionListener(this::onSubmit);
        backButton.addActionListener(e -> closeWindow());
    }

    private void onSubmit(ActionEvent event) {
        resetBorders();

        EmployeeCreateRequest request = buildCreateRequestFromForm();

        EmployeeValidationResult validationResult = EmployeeValidator.validateForCreate(request);
        if (!validationResult.isValid()) {
            markErrors(validationResult);
            showValidationMessages(validationResult.getMessages());
            return;
        }

        String employeeId = safe(request.getEmployeeId());
        if (employeeService.existsByEmployeeId(employeeId)) {
            setError(EMPLOYEE_ID);
            JOptionPane.showMessageDialog(
                    this,
                    "Employee # already exists!",
                    "Duplicate Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        boolean added = employeeService.addEmployee(request);

        if (!added) {
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to add employee.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        JOptionPane.showMessageDialog(this, "Employee added successfully.");
        clearFields();

        if (onEmployeeAdded != null) {
            onEmployeeAdded.run();
        }

        closeWindow();
    }

    private EmployeeCreateRequest buildCreateRequestFromForm() {
        Map<String, String> values = collectValues();

        return new EmployeeCreateRequest(
                values.getOrDefault(EMPLOYEE_ID, "").trim(),
                values.getOrDefault(LAST_NAME, "").trim(),
                values.getOrDefault(FIRST_NAME, "").trim(),
                parseDate(values.get(BIRTHDAY)),
                values.getOrDefault(ADDRESS, "").trim(),
                values.getOrDefault(PHONE_NUMBER, "").trim(),
                values.getOrDefault(SSS, "").trim(),
                values.getOrDefault(PHILHEALTH, "").trim(),
                values.getOrDefault(TIN, "").trim(),
                values.getOrDefault(PAG_IBIG, "").trim(),
                values.getOrDefault(STATUS, "Regular").trim(),
                values.getOrDefault(POSITION, "").trim(),
                values.getOrDefault(IMMEDIATE_SUPERVISOR, "").trim(),
                money(values.get(BASIC_SALARY)),
                money(values.get(RICE_SUBSIDY)),
                money(values.get(PHONE_ALLOWANCE)),
                money(values.get(CLOTHING_ALLOWANCE)),
                money(values.get(GROSS_SEMI_MONTHLY_RATE)),
                money(values.get(HOURLY_RATE))
        );
    }

    private Map<String, String> collectValues() {
        Map<String, String> values = new LinkedHashMap<>();

        for (Map.Entry<String, JComponent> entry : fields.entrySet()) {
            values.put(entry.getKey(), extractValue(entry.getValue()));
        }

        return values;
    }

    private String extractValue(JComponent component) {
        if (component instanceof JTextField textField) {
            return textField.getText().trim();
        }

        if (component instanceof DatePicker datePicker) {
            return datePicker.getDate() != null ? datePicker.getDate().toString() : "";
        }

        if (component instanceof JComboBox<?> comboBox) {
            Object selected = comboBox.getSelectedItem();
            return selected == null ? "" : selected.toString().trim();
        }

        return "";
    }

    private void markErrors(EmployeeValidationResult validationResult) {
        for (EmployeeValidationResult.FieldError error : validationResult.getErrors()) {
            setError(error.getField());
        }
    }

    private void showValidationMessages(List<String> messages) {
        String text = messages.stream()
                .map(message -> "- " + message)
                .reduce((a, b) -> a + "\n" + b)
                .orElse("Validation failed.");

        JOptionPane.showMessageDialog(
                this,
                "Please fix the following:\n" + text,
                "Validation Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private void setError(String header) {
        JComponent component = fields.get(header);
        if (component == null) {
            return;
        }

        if (component instanceof DatePicker datePicker) {
            datePicker.getComponentDateTextField().setBorder(ERROR_BORDER);
            return;
        }

        component.setBorder(ERROR_BORDER);
    }

    private void resetBorders() {
        for (JComponent component : fields.values()) {
            if (component instanceof DatePicker datePicker) {
                datePicker.getComponentDateTextField().setBorder(DEFAULT_BORDER);
            } else {
                component.setBorder(DEFAULT_BORDER);
            }
        }
    }

    private void clearFields() {
        for (Map.Entry<String, JComponent> entry : fields.entrySet()) {
            String header = entry.getKey();
            JComponent component = entry.getValue();

            if (component instanceof JTextField textField) {
                textField.setText("");
            } else if (component instanceof DatePicker datePicker) {
                datePicker.clear();
            } else if (component instanceof JComboBox<?> comboBox) {
                if (STATUS.equals(header)) {
                    comboBox.setSelectedItem("Regular");
                } else {
                    comboBox.setSelectedIndex(0);
                }
            }
        }

        resetBorders();
    }

    private boolean isRequired(String header) {
        return REQUIRED_FIELDS.contains(header);
    }

    private BigDecimal money(String value) {
        if (value == null || value.isBlank()) {
            return BigDecimal.ZERO;
        }

        String clean = value.replace("\"", "").replace(",", "").trim();

        try {
            return new BigDecimal(clean);
        } catch (Exception ex) {
            return BigDecimal.ZERO;
        }
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String text = value.trim();

        try {
            return LocalDate.parse(text);
        } catch (Exception ex) {
            try {
                if (text.contains("/")) {
                    String[] parts = text.split("/");
                    return LocalDate.of(
                            Integer.parseInt(parts[0]),
                            Integer.parseInt(parts[1]),
                            Integer.parseInt(parts[2])
                    );
                }
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private void closeWindow() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.dispose();
        }
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
                                1,
                                1,
                                0,
                                false
                        )
                );
            }
        });
    }

    private void restrictNumeric(JTextField textField) {
        ((AbstractDocument) textField.getDocument()).setDocumentFilter(new NumericDocumentFilter());
    }

    private void styleButtons() {
        styleRoundedButton(submitButton, Color.BLACK, 140, 38);
        styleRoundedButton(backButton, Color.BLACK, 90, 38);
    }

    private void styleRoundedButton(JButton button, Color background, int width, int height) {
        button.setPreferredSize(new Dimension(width, height));
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
                g2.setColor(background);
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 20, 20);
                super.paint(g2, c);
                g2.dispose();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        GradientPaint paint = new GradientPaint(0, 0, GRADIENT_START, 0, getHeight(), GRADIENT_END);
        g2.setPaint(paint);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }

    private static class NumericDocumentFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            if (string == null) {
                return;
            }

            if (string.matches("\\d+")) {
                super.insertString(fb, offset, string, attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            if (text == null || text.isEmpty()) {
                super.replace(fb, offset, length, text, attrs);
                return;
            }

            if (text.matches("\\d+")) {
                super.replace(fb, offset, length, text, attrs);
            }
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            super.remove(fb, offset, length);
        }
    }
}