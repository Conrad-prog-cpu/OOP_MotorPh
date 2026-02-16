package service;

import model.*;
import repository.AttendanceRepository;
import repository.EmployeeRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

public class DefaultPayrollService implements PayrollService {

    private final EmployeeRepository employeeRepo;
    private final AttendanceRepository attendanceRepo;
    private final WorkHoursCalculator hoursCalculator;
    private final EarningsCalculator earningsCalculator;
    private final ContributionCalculator contributionCalculator;
    private final TaxCalculator taxCalculator;

    private final OvertimePolicy overtimePolicy;
    private final TaxableBenefitsPolicy benefitsPolicy;

    public DefaultPayrollService(
            EmployeeRepository employeeRepo,
            AttendanceRepository attendanceRepo,
            WorkHoursCalculator hoursCalculator,
            EarningsCalculator earningsCalculator,
            ContributionCalculator contributionCalculator,
            TaxCalculator taxCalculator,
            OvertimePolicy overtimePolicy,
            TaxableBenefitsPolicy benefitsPolicy
    ) {
        this.employeeRepo = Objects.requireNonNull(employeeRepo);
        this.attendanceRepo = Objects.requireNonNull(attendanceRepo);
        this.hoursCalculator = Objects.requireNonNull(hoursCalculator);
        this.earningsCalculator = Objects.requireNonNull(earningsCalculator);
        this.contributionCalculator = Objects.requireNonNull(contributionCalculator);
        this.taxCalculator = Objects.requireNonNull(taxCalculator);
        this.overtimePolicy = Objects.requireNonNull(overtimePolicy);
        this.benefitsPolicy = Objects.requireNonNull(benefitsPolicy);
    }

    @Override
    public PayrollResult computeForDateRange(String employeeNumber, LocalDate start, LocalDate end) {

        String empNo = employeeNumber == null ? "" : employeeNumber.trim();
        if (empNo.isEmpty()) throw new IllegalArgumentException("Employee number is required");

        employeeRepo.load();
        attendanceRepo.load();

        Employee employee = employeeRepo.findById(empNo)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + empNo));

        List<AttendanceLog> logs = attendanceRepo.findByEmployeeAndDateRange(empNo, start, end);
        WorkHoursSummary summary = hoursCalculator.summarize(logs);

        BigDecimal hoursWorked = nz(summary.getTotalHoursWorked());
        BigDecimal overtimeHours = nz(summary.getOvertimeHoursWorked());
        int lateMinutes = summary.getTotalLateMinutes();

        // PAY breakdown
        BigDecimal basicPay = nz(earningsCalculator.computeBasicPay(employee.getHourlyRate(), hoursWorked));
        BigDecimal allowances = nz(earningsCalculator.computeAllowances(employee));

        BigDecimal overtimePay = nz(earningsCalculator.computeOvertimePay(
                employee.getHourlyRate(),
                overtimeHours,
                overtimePolicy
        ));

        BigDecimal grossBeforeLate = nz(earningsCalculator.computeGrossPay(basicPay, allowances, overtimePay));

        BigDecimal lateDeduction = computeLateDeduction(employee.getHourlyRate(), lateMinutes);

        BigDecimal grossPay = grossBeforeLate.subtract(lateDeduction);
        if (grossPay.compareTo(BigDecimal.ZERO) < 0) grossPay = BigDecimal.ZERO;

        Contributions contributions = contributionCalculator.computeAll(employee.getBasicSalary());
        BigDecimal taxableIncome = taxCalculator.computeTaxableIncome(grossPay, contributions, benefitsPolicy);
        BigDecimal tax = nz(taxCalculator.computeWithholdingTax(taxableIncome));
        BigDecimal netPay = nz(taxCalculator.computeNetPay(grossPay, contributions, tax));

        // ✅ return extended PayrollResult
        return new PayrollResult(
                empNo,
                scale2(hoursWorked),
                scale2(overtimeHours),
                lateMinutes,
                scale2(basicPay),
                scale2(allowances),
                scale2(overtimePay),
                scale2(lateDeduction),
                scale2(grossPay),
                contributions,
                scale2(tax),
                scale2(netPay)
        );
    }

    @Override
    public PayrollResult computeForMonth(String employeeNumber, int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        return computeForDateRange(employeeNumber, ym.atDay(1), ym.atEndOfMonth());
    }

    private BigDecimal computeLateDeduction(BigDecimal hourlyRate, int lateMinutes) {
        if (hourlyRate == null || lateMinutes <= 0) return BigDecimal.ZERO;

        BigDecimal perMinute = hourlyRate.divide(BigDecimal.valueOf(60), 6, RoundingMode.HALF_UP);
        return perMinute.multiply(BigDecimal.valueOf(lateMinutes))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static BigDecimal scale2(BigDecimal v) {
        return nz(v).setScale(2, RoundingMode.HALF_UP);
    }
}
