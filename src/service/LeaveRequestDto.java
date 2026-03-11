package service;

public class LeaveRequestDto {

    private final String leaveId;
    private final String employeeId;
    private final String employeeName;
    private final String leaveType;
    private final String startDate;
    private final String endDate;
    private final String dateRangeDisplay;
    private final String reason;
    private final String status;
    private final String requestedAt;
    private final String reviewedBy;
    private final String decisionAt;

    public LeaveRequestDto(
            String leaveId,
            String employeeId,
            String employeeName,
            String leaveType,
            String startDate,
            String endDate,
            String dateRangeDisplay,
            String reason,
            String status,
            String requestedAt,
            String reviewedBy,
            String decisionAt
    ) {
        this.leaveId = leaveId;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.dateRangeDisplay = dateRangeDisplay;
        this.reason = reason;
        this.status = status;
        this.requestedAt = requestedAt;
        this.reviewedBy = reviewedBy;
        this.decisionAt = decisionAt;
    }

    public String getLeaveId() {
        return leaveId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public String getLeaveType() {
        return leaveType;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getDateRangeDisplay() {
        return dateRangeDisplay;
    }

    public String getReason() {
        return reason;
    }

    public String getStatus() {
        return status;
    }

    public String getRequestedAt() {
        return requestedAt;
    }

    public String getReviewedBy() {
        return reviewedBy;
    }

    public String getDecisionAt() {
        return decisionAt;
    }
}