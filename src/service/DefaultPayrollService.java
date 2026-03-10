package service;

import model.AttendanceLog;
import model.ContributionCalculator;
import model.Contributions;
import model.EarningsCalculator;
import model.Employee;
import model.OvertimePolicy;
import model.PayrollResult;
import model.TaxCalculator;
import model.TaxableBenefitsPolicy;
import model.WorkHoursCalculator;
import model.WorkHoursSummary;
import repository.AttendanceRepository;
import repository.EmployeeRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

public class DefaultPayrollService implements PayrollService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final int MAX_REASONABLE_LATE_MINUTES_PER_MONTH = 3000;

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
        this.employeeRepo = Objects.requireNonNull(employeeRepo, "employeeRepo is required");
        this.attendanceRepo = Objects.requireNonNull(attendanceRepo, "attendanceRepo is required");
        this.hoursCalculator = Objects.requireNonNull(hoursCalculator, "hoursCalculator is required");
        this.earningsCalculator = Objects.requireNonNull(earningsCalculator, "earningsCalculator is required");
        this.contributionCalculator = Objects.requireNonNull(contributionCalculator, "contributionCalculator is required");
        this.taxCalculator = Objects.requireNonNull(taxCalculator, "taxCalculator is required");
        this.overtimePolicy = Objects.requireNonNull(overtimePolicy, "overtimePolicy is required");
        this.benefitsPolicy = Objects.requireNonNull(benefitsPolicy, "benefitsPolicy is required");
    }

    @Override
    public PayrollResult computeForDateRange(String employeeNumber, LocalDate start, LocalDate end) {
        String employeeId = normalizeEmployeeNumber(employeeNumber);
        validateDateRange(start, end);

        employeeRepo.load();
        attendanceRepo.load();

        Employee employee = employeeRepo.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));

        List<AttendanceLog> logs = attendanceRepo.findByEmployeeAndDateRange(employeeId, start, end);
        WorkHoursSummary summary = safeSummary(hoursCalculator.summarize(logs));

        BigDecimal hoursWorked = scale2(summary.getTotalHoursWorked());
        BigDecimal overtimeHours = scale2(summary.getOvertimeHoursWorked());
        int lateMinutes = sanitizeLateMinutes(summary.getTotalLateMinutes());

        BigDecimal hourlyRate = nz(employee.getHourlyRate());
        BigDecimal basicSalary = nz(employee.getBasicSalary());

        BigDecimal basicPay = scale2(
                nz(earningsCalculator.computeBasicPay(hourlyRate, hoursWorked))
        );

        BigDecimal allowances = scale2(
                nz(earningsCalculator.computeAllowances(employee))
        );

        BigDecimal overtimePay = scale2(
                nz(earningsCalculator.computeOvertimePay(hourlyRate, overtimeHours, overtimePolicy))
        );

        BigDecimal grossBeforeLate = scale2(
                nz(earningsCalculator.computeGrossPay(basicPay, allowances, overtimePay))
        );

        BigDecimal lateDeduction = scale2(
                computeLateDeduction(hourlyRate, lateMinutes)
        );

        BigDecimal grossPay = scale2(nonNegative(grossBeforeLate.subtract(lateDeduction)));

        Contributions contributions = safeContributions(
                contributionCalculator.computeAll(basicSalary)
        );

        BigDecimal taxableIncome = scale2(
                nz(taxCalculator.computeTaxableIncome(grossPay, contributions, benefitsPolicy))
        );

        BigDecimal withholdingTax = scale2(
                nz(taxCalculator.computeWithholdingTax(taxableIncome))
        );

        BigDecimal netPay = scale2(
                nz(taxCalculator.computeNetPay(grossPay, contributions, withholdingTax))
        );

        return new PayrollResult(
                employeeId,
                hoursWorked,
                overtimeHours,
                lateMinutes,
                basicPay,
                allowances,
                overtimePay,
                lateDeduction,
                grossPay,
                contributions,
                withholdingTax,
                netPay
        );
    }

    @Override
    public PayrollResult computeForMonth(String employeeNumber, int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        return computeForDateRange(employeeNumber, ym.atDay(1), ym.atEndOfMonth());
    }

    private String normalizeEmployeeNumber(String employeeNumber) {
        String value = employeeNumber == null ? "" : employeeNumber.trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Employee number is required");
        }
        return value;
    }

    private void validateDateRange(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start date and end date are required");
        }
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End date cannot be earlier than start date");
        }
    }

    private WorkHoursSummary safeSummary(WorkHoursSummary summary) {
        return summary == null ? new WorkHoursSummary(0, 0, 0) : summary;
    }

    private Contributions safeContributions(Contributions contributions) {
        return contributions == null ? new Contributions(ZERO, ZERO, ZERO) : contributions;
    }

    private int sanitizeLateMinutes(int lateMinutes) {
        if (lateMinutes < 0) {
            return 0;
        }
        if (lateMinutes > MAX_REASONABLE_LATE_MINUTES_PER_MONTH) {
            return 0;
        }
        return lateMinutes;
    }

    private BigDecimal computeLateDeduction(BigDecimal hourlyRate, int lateMinutes) {
        if (hourlyRate == null || hourlyRate.compareTo(ZERO) <= 0 || lateMinutes <= 0) {
            return ZERO;
        }

        BigDecimal perMinuteRate = hourlyRate.divide(BigDecimal.valueOf(60), 6, RoundingMode.HALF_UP);

        return perMinuteRate
                .multiply(BigDecimal.valueOf(lateMinutes))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal nonNegative(BigDecimal value) {
        return nz(value).compareTo(ZERO) < 0 ? ZERO : value;
    }

    private BigDecimal nz(BigDecimal value) {
        return value == null ? ZERO : value;
    }

    private BigDecimal scale2(BigDecimal value) {
        return nz(value).setScale(2, RoundingMode.HALF_UP);
    }
}