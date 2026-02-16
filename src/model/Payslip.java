/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;
/**
 *
 * @author ca
 */
public class Payslip {
    private final String payslipId;

    private final String employeeId;
    private final String employeeName; // stored snapshot for audit (name can change later)

    private final YearMonth yearMonth;
    private final LocalDate periodStart;
    private final LocalDate periodEnd;

    private final BigDecimal hoursWorked;
    private final BigDecimal grossPay;

    private final BigDecimal sss;
    private final BigDecimal philHealth;
    private final BigDecimal pagIbig;
    private final BigDecimal withholdingTax;

    private final BigDecimal netPay;

    public Payslip(
            String payslipId,
            String employeeId,
            String employeeName,
            YearMonth yearMonth,
            LocalDate periodStart,
            LocalDate periodEnd,
            BigDecimal hoursWorked,
            BigDecimal grossPay,
            BigDecimal sss,
            BigDecimal philHealth,
            BigDecimal pagIbig,
            BigDecimal withholdingTax,
            BigDecimal netPay
    ) {
        this.payslipId = payslipId;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.yearMonth = yearMonth;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.hoursWorked = hoursWorked;
        this.grossPay = grossPay;
        this.sss = sss;
        this.philHealth = philHealth;
        this.pagIbig = pagIbig;
        this.withholdingTax = withholdingTax;
        this.netPay = netPay;
    }

    public String getPayslipId() { return payslipId; }

    public String getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }

    public YearMonth getYearMonth() { return yearMonth; }
    public LocalDate getPeriodStart() { return periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }

    public BigDecimal getHoursWorked() { return hoursWorked; }
    public BigDecimal getGrossPay() { return grossPay; }

    public BigDecimal getSss() { return sss; }
    public BigDecimal getPhilHealth() { return philHealth; }
    public BigDecimal getPagIbig() { return pagIbig; }
    public BigDecimal getWithholdingTax() { return withholdingTax; }

    public BigDecimal getNetPay() { return netPay; }

    public BigDecimal getTotalContributions() {
        return sss.add(philHealth).add(pagIbig);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Payslip)) return false;
        Payslip payslip = (Payslip) o;
        return Objects.equals(payslipId, payslip.payslipId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(payslipId);
    }
}
