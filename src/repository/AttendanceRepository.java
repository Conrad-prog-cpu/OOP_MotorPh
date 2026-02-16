/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package repository;
import java.time.LocalDate;
import java.util.List;
import model.AttendanceLog;
/**
 *
 * @author ca
 */
public interface AttendanceRepository {
    void load();

    List<AttendanceLog> findByEmployeeAndDateRange(String employeeNo, LocalDate start, LocalDate end);

    boolean updateAttendance(String employeeNo, String date, String[] newRow);

    // ✅ THIS is the one your compiler is asking for
    boolean deleteAttendance(String employeeNo, String date);
}