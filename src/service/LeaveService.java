package service;

import java.util.List;

public interface LeaveService {

    boolean fileLeaveRequest(AuthenticatedUser currentUser, LeaveCreateRequest request);

    List<LeaveRequestDto> findMyLeaves(String employeeNumber);

    List<LeaveRequestDto> findPendingLeaves();

    LeaveRequestDto findLeaveById(String leaveId);

    boolean approveLeave(String leaveId, AuthenticatedUser approver);

    boolean rejectLeave(String leaveId, AuthenticatedUser approver);
}