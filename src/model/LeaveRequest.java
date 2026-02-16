/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
/**
 *
 * @author ca
 */
public class LeaveRequest {
    private final String leaveId;
    private final String employeeId;
    private final String employeeName;

    private final LeaveType leaveType;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final String reason;

    private LeaveStatus status;

    private final LocalDateTime requestedAt;
    private String reviewedBy;
    private LocalDateTime reviewedAt;

    public LeaveRequest(
            String leaveId,
            String employeeId,
            String employeeName,
            LeaveType leaveType,
            LocalDate startDate,
            LocalDate endDate,
            String reason,
            LeaveStatus status,
            LocalDateTime requestedAt,
            String reviewedBy,
            LocalDateTime reviewedAt
    ) {
        this.leaveId = leaveId;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.status = status;
        this.requestedAt = requestedAt;
        this.reviewedBy = reviewedBy;
        this.reviewedAt = reviewedAt;
    }

    public String getLeaveId() { return leaveId; }
    public String getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public LeaveType getLeaveType() { return leaveType; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public String getReason() { return reason; }

    public LeaveStatus getStatus() { return status; }
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public String getReviewedBy() { return reviewedBy; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }

    public void approve(String reviewer) {
        this.status = LeaveStatus.APPROVED;
        this.reviewedBy = reviewer;
        this.reviewedAt = LocalDateTime.now();
    }

    public void deny(String reviewer) {
        this.status = LeaveStatus.DENIED;
        this.reviewedBy = reviewer;
        this.reviewedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LeaveRequest)) return false;
        LeaveRequest that = (LeaveRequest) o;
        return Objects.equals(leaveId, that.leaveId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(leaveId);
    }
}

