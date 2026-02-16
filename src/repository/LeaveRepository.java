/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package repository;
import model.LeaveRequest;
import model.LeaveStatus;

import java.util.List;
import java.util.Optional;
/**
 *
 * @author ca
 */
public interface LeaveRepository {
    LeaveRequest create(LeaveRequest request);
    Optional<LeaveRequest> findById(String leaveId);
    List<LeaveRequest> findAll();
    List<LeaveRequest> findByEmployeeId(String employeeId);
    List<LeaveRequest> findByStatus(LeaveStatus status);

    LeaveRequest update(LeaveRequest request);
    boolean deleteById(String leaveId);
}