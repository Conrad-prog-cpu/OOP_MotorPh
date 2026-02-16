/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package model;
import model.AttendanceLog;
import model.WorkHoursSummary;
import java.util.List;
/**
 *
 * @author ca
 */
public interface WorkHoursCalculator {
    WorkHoursSummary summarize(List<AttendanceLog> logs);

    // Optional helpers
    int computeMinutesWorked(AttendanceLog log);
    int computeLateMinutes(AttendanceLog log);
    int computeOvertimeMinutes(AttendanceLog log);
}
