package service;

import model.LeaveRequest;
import model.LeaveStatus;
import model.LeaveType;
import repository.LeaveRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class DefaultLeaveService implements LeaveService {

    private final LeaveRepository leaveRepository;

    public DefaultLeaveService(LeaveRepository leaveRepository) {
        this.leaveRepository = Objects.requireNonNull(leaveRepository, "leaveRepository is required");
    }

    @Override
    public boolean fileLeaveRequest(AuthenticatedUser currentUser, LeaveCreateRequest request) {
        if (currentUser == null || request == null) {
            return false;
        }

        if (safe(request.getEmployeeNumber()).isEmpty()
                || request.getStartDate() == null
                || request.getEndDate() == null
                || safe(request.getReason()).isEmpty()) {
            return false;
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            return false;
        }

        LeaveRequest leaveRequest = new LeaveRequest(
                UUID.randomUUID().toString(),
                safe(request.getEmployeeNumber()),
                safe(request.getEmployeeName()),
                toLeaveType(request.getLeaveType()),
                request.getStartDate(),
                request.getEndDate(),
                safe(request.getReason()),
                LeaveStatus.PENDING,
                LocalDateTime.now(),
                "",
                null
        );

        leaveRepository.create(leaveRequest);
        return true;
    }

    @Override
    public List<LeaveRequestDto> findMyLeaves(String employeeNumber) {
        List<LeaveRequest> requests = leaveRepository.findByEmployeeId(safe(employeeNumber));
        List<LeaveRequestDto> dtos = new ArrayList<>();

        if (requests == null) {
            return dtos;
        }

        requests.sort(Comparator.comparing(LeaveRequest::getRequestedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed());

        for (LeaveRequest request : requests) {
            dtos.add(toDto(request));
        }

        return dtos;
    }

    @Override
    public List<LeaveRequestDto> findPendingLeaves() {
        List<LeaveRequest> requests = leaveRepository.findByStatus(LeaveStatus.PENDING);
        List<LeaveRequestDto> dtos = new ArrayList<>();

        if (requests == null) {
            return dtos;
        }

        for (LeaveRequest request : requests) {
            dtos.add(toDto(request));
        }

        return dtos;
    }

    @Override
    public LeaveRequestDto findLeaveById(String leaveId) {
        LeaveRequest request = leaveRepository.findById(safe(leaveId)).orElse(null);
        return request == null ? null : toDto(request);
    }

    @Override
    public boolean approveLeave(String leaveId, AuthenticatedUser approver) {
        if (approver == null) {
            return false;
        }

        LeaveRequest request = leaveRepository.findById(safe(leaveId)).orElse(null);
        if (request == null) {
            return false;
        }

        request.approve(safe(approver.getEmployeeNumber()));
        leaveRepository.update(request);
        return true;
    }

    @Override
    public boolean rejectLeave(String leaveId, AuthenticatedUser approver) {
        if (approver == null) {
            return false;
        }

        LeaveRequest request = leaveRepository.findById(safe(leaveId)).orElse(null);
        if (request == null) {
            return false;
        }

        request.deny(safe(approver.getEmployeeNumber()));
        leaveRepository.update(request);
        return true;
    }

    private LeaveRequestDto toDto(LeaveRequest request) {
        return new LeaveRequestDto(
                safe(request.getLeaveId()),
                safe(request.getEmployeeId()),
                safe(request.getEmployeeName()),
                request.getLeaveType() == null ? "" : request.getLeaveType().name(),
                request.getStartDate() == null ? "" : request.getStartDate().toString(),
                request.getEndDate() == null ? "" : request.getEndDate().toString(),
                buildDateRangeDisplay(request),
                safe(request.getReason()),
                request.getStatus() == null ? "" : request.getStatus().name(),
                request.getRequestedAt() == null ? "" : request.getRequestedAt().toString(),
                safe(request.getReviewedBy()),
                request.getReviewedAt() == null ? "" : request.getReviewedAt().toString()
        );
    }

    private String buildDateRangeDisplay(LeaveRequest request) {
        if (request.getStartDate() == null || request.getEndDate() == null) {
            return "";
        }

        long days = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
        String dayLabel = days == 1 ? "day" : "days";

        return request.getStartDate() + " -> " + request.getEndDate() + " (" + days + " " + dayLabel + ")";
    }

    private LeaveType toLeaveType(String value) {
        String normalized = safe(value).toUpperCase().replace(" ", "_");
        try {
            return LeaveType.valueOf(normalized);
        } catch (Exception ex) {
            return LeaveType.OTHER;
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}