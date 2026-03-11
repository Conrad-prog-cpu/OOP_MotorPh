package service;

import java.time.LocalDate;

public class LeaveCreateRequest {

    private final String employeeNumber;
    private final String employeeName;
    private final String leaveType;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final String reason;

    public LeaveCreateRequest(
            String employeeNumber,
            String employeeName,
            String leaveType,
            LocalDate startDate,
            LocalDate endDate,
            String reason
    ) {
        this.employeeNumber = employeeNumber;
        this.employeeName = employeeName;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public String getLeaveType() {
        return leaveType;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getReason() {
        return reason;
    }
}