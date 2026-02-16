/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.math.BigDecimal;

public class PayrollResult {

    private final String employeeId;

    // Work
    private final BigDecimal hoursWorked;
    private final BigDecimal overtimeHours;
    private final int lateMinutes;

    // Pay breakdown
    private final BigDecimal basicPay;
    private final BigDecimal allowances;
    private final BigDecimal overtimePay;
    private final BigDecimal lateDeduction;

    // Totals
    private final BigDecimal grossPay;
    private final Contributions contributions;
    private final BigDecimal withholdingTax;
    private final BigDecimal netPay;

    public PayrollResult(
            String employeeId,
            BigDecimal hoursWorked,
            BigDecimal overtimeHours,
            int lateMinutes,
            BigDecimal basicPay,
            BigDecimal allowances,
            BigDecimal overtimePay,
            BigDecimal lateDeduction,
            BigDecimal grossPay,
            Contributions contributions,
            BigDecimal withholdingTax,
            BigDecimal netPay
    ) {
        this.employeeId = employeeId;

        this.hoursWorked = hoursWorked;
        this.overtimeHours = overtimeHours;
        this.lateMinutes = lateMinutes;

        this.basicPay = basicPay;
        this.allowances = allowances;
        this.overtimePay = overtimePay;
        this.lateDeduction = lateDeduction;

        this.grossPay = grossPay;
        this.contributions = contributions;
        this.withholdingTax = withholdingTax;
        this.netPay = netPay;
    }

    public String getEmployeeId() { return employeeId; }

    public BigDecimal getHoursWorked() { return hoursWorked; }
    public BigDecimal getOvertimeHours() { return overtimeHours; }
    public int getLateMinutes() { return lateMinutes; }

    public BigDecimal getBasicPay() { return basicPay; }
    public BigDecimal getAllowances() { return allowances; }
    public BigDecimal getOvertimePay() { return overtimePay; }
    public BigDecimal getLateDeduction() { return lateDeduction; }

    public BigDecimal getGrossPay() { return grossPay; }
    public Contributions getContributions() { return contributions; }
    public BigDecimal getWithholdingTax() { return withholdingTax; }
    public BigDecimal getNetPay() { return netPay; }
}
