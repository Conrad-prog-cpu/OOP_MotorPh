/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class MotorPhEmployeeFactory implements EmployeeFactory {

    @Override
    public Employee fromRow(List<String> headers, String[] row) {
        String employeeId = get(headers, row, "Employee #");
        String lastName   = get(headers, row, "Last Name");
        String firstName  = get(headers, row, "First Name");
        LocalDate bday    = parseDate(get(headers, row, "Birthday"));

        String address    = get(headers, row, "Address");
        String phone      = get(headers, row, "Phone Number");

        String sss        = get(headers, row, "SSS #");
        String phil       = get(headers, row, "Philhealth #");
        String tin        = get(headers, row, "TIN #");
        String pagibig    = get(headers, row, "Pag-ibig #");

        String status     = get(headers, row, "Status");
        String position   = get(headers, row, "Position");
        String supervisor = get(headers, row, "Immediate Supervisor");

        BigDecimal basicSalary     = money(get(headers, row, "Basic Salary"));
        BigDecimal riceSubsidy     = money(get(headers, row, "Rice Subsidy"));
        BigDecimal phoneAllowance  = money(get(headers, row, "Phone Allowance"));
        BigDecimal clothingAllow   = money(get(headers, row, "Clothing Allowance"));
        BigDecimal semiMonthlyRate = money(get(headers, row, "Gross Semi-monthly Rate"));
        BigDecimal hourlyRate      = money(get(headers, row, "Hourly Rate"));

        // ---- Polymorphism decision point ----
        if ("Regular".equalsIgnoreCase(status)) {
            return new RegularEmployee(
                    employeeId, lastName, firstName, bday, address, phone,
                    sss, phil, tin, pagibig, status, position, supervisor,
                    basicSalary, riceSubsidy, phoneAllowance, clothingAllow,
                    semiMonthlyRate, hourlyRate
            );
        } else if ("Probationary".equalsIgnoreCase(status)) {
            return new ProbationaryEmployee(
                    employeeId, lastName, firstName, bday, address, phone,
                    sss, phil, tin, pagibig, status, position, supervisor,
                    basicSalary, riceSubsidy, phoneAllowance, clothingAllow,
                    semiMonthlyRate, hourlyRate
            );
        }

        // Default fallback subtype
        return new RegularEmployee(
                employeeId, lastName, firstName, bday, address, phone,
                sss, phil, tin, pagibig, status, position, supervisor,
                basicSalary, riceSubsidy, phoneAllowance, clothingAllow,
                semiMonthlyRate, hourlyRate
        );
    }

    @Override
    public String[] toRow(List<String> headers, Employee e) {
        String[] row = new String[headers.size()];
        set(headers, row, "Employee #", e.getEmployeeID());
        set(headers, row, "Last Name", e.getLastName());
        set(headers, row, "First Name", e.getFirstName());
        set(headers, row, "Birthday", e.getBirthday() != null ? e.getBirthday().toString() : "");
        set(headers, row, "Address", e.getAddress());
        set(headers, row, "Phone Number", e.getPhoneNumber());
        set(headers, row, "SSS #", e.getSssNumber());
        set(headers, row, "Philhealth #", e.getPhilHealthNumber());
        set(headers, row, "TIN #", e.getTinNumber());
        set(headers, row, "Pag-ibig #", e.getPagIbigNumber());
        set(headers, row, "Status", e.getStatus());
        set(headers, row, "Position", e.getPosition());
        set(headers, row, "Immediate Supervisor", e.getImmediateSupervisor());
        set(headers, row, "Basic Salary", e.getBasicSalary().toPlainString());
        set(headers, row, "Rice Subsidy", e.getRiceSubsidy().toPlainString());
        set(headers, row, "Phone Allowance", e.getPhoneAllowance().toPlainString());
        set(headers, row, "Clothing Allowance", e.getClothingAllowance().toPlainString());
        set(headers, row, "Gross Semi-monthly Rate", e.getSemiMonthlyRate().toPlainString());
        set(headers, row, "Hourly Rate", e.getHourlyRate().toPlainString());
        return row;
    }

    // ---------- helpers ----------
    private String get(List<String> headers, String[] row, String col) {
        int idx = headers.indexOf(col);
        return (idx >= 0 && idx < row.length) ? safe(row[idx]) : "";
    }

    private void set(List<String> headers, String[] row, String col, String val) {
        int idx = headers.indexOf(col);
        if (idx >= 0 && idx < row.length) row[idx] = safe(val);
    }

    private String safe(String s) {
        return s == null ? "" : s.replace("\"", "").trim();
    }

    private BigDecimal money(String s) {
        if (s == null || s.isBlank()) return BigDecimal.ZERO;
        String clean = s.replace("\"","").replace(",","").trim();
        try { return new BigDecimal(clean); }
        catch (Exception ex) { return BigDecimal.ZERO; }
    }

    private LocalDate parseDate(String s) {
    if (s == null) return null;

    s = s.replace("\"", "").trim();
    if (s.isEmpty()) return null;

    // try multiple known formats
    java.util.List<java.time.format.DateTimeFormatter> formats = java.util.List.of(
            java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy"), // 02/14/1988
            java.time.format.DateTimeFormatter.ofPattern("M/d/yyyy"),   // 2/4/1988
            java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd"), // 1988/02/14
            java.time.format.DateTimeFormatter.ISO_LOCAL_DATE           // 1988-02-14
    );

    for (var fmt : formats) {
        try {
            return LocalDate.parse(s, fmt);
        } catch (Exception ignored) {}
    }

    return null; // if still invalid, return null instead of crashing
    }   
}
