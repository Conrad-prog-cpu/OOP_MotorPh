package service;

public class PayrollResultDto {

    private final String employeeId;
    private final String hoursWorked;
    private final String overtimeHours;
    private final int lateMinutes;
    private final String basicPay;
    private final String allowances;
    private final String overtimePay;
    private final String lateDeduction;
    private final String grossPay;
    private final String sss;
    private final String philHealth;
    private final String pagIbig;
    private final String withholdingTax;
    private final String netPay;

    public PayrollResultDto(
            String employeeId,
            String hoursWorked,
            String overtimeHours,
            int lateMinutes,
            String basicPay,
            String allowances,
            String overtimePay,
            String lateDeduction,
            String grossPay,
            String sss,
            String philHealth,
            String pagIbig,
            String withholdingTax,
            String netPay
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
        this.sss = sss;
        this.philHealth = philHealth;
        this.pagIbig = pagIbig;
        this.withholdingTax = withholdingTax;
        this.netPay = netPay;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getHoursWorked() {
        return hoursWorked;
    }

    public String getOvertimeHours() {
        return overtimeHours;
    }

    public int getLateMinutes() {
        return lateMinutes;
    }

    public String getBasicPay() {
        return basicPay;
    }

    public String getAllowances() {
        return allowances;
    }

    public String getOvertimePay() {
        return overtimePay;
    }

    public String getLateDeduction() {
        return lateDeduction;
    }

    public String getGrossPay() {
        return grossPay;
    }

    public String getSss() {
        return sss;
    }

    public String getPhilHealth() {
        return philHealth;
    }

    public String getPagIbig() {
        return pagIbig;
    }

    public String getWithholdingTax() {
        return withholdingTax;
    }

    public String getNetPay() {
        return netPay;
    }
}