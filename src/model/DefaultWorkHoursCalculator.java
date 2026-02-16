/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

public class DefaultWorkHoursCalculator implements WorkHoursCalculator {

    private final OvertimePolicy overtimePolicy;

    public DefaultWorkHoursCalculator(OvertimePolicy overtimePolicy) {
        this.overtimePolicy = Objects.requireNonNull(overtimePolicy, "overtimePolicy is required");
    }

    @Override
    public WorkHoursSummary summarize(List<AttendanceLog> logs) {
        int totalMinutes = 0;
        int totalLate = 0;
        int totalOT = 0;

        if (logs == null) return new WorkHoursSummary(0, 0, 0);

        for (AttendanceLog log : logs) {
            if (log == null || log.getTimeIn() == null || log.getTimeOut() == null) continue;

            totalMinutes += computeMinutesWorked(log);
            totalLate += computeLateMinutes(log);
            totalOT += computeOvertimeMinutes(log); // OT suppressed if late (policy)
        }

        return new WorkHoursSummary(totalMinutes, totalLate, totalOT);
    }

    @Override
    public int computeMinutesWorked(AttendanceLog log) {
        int minutes = (int) Duration.between(log.getTimeIn(), log.getTimeOut()).toMinutes();
        // subtract lunch break (1 hour)
        return Math.max(minutes - 60, 0);
    }

    @Override
    public int computeLateMinutes(AttendanceLog log) {
        if (!overtimePolicy.isLate(log)) return 0;
        return (int) Duration.between(overtimePolicy.getGraceTime(), log.getTimeIn()).toMinutes();
    }

    @Override
    public int computeOvertimeMinutes(AttendanceLog log) {
        // ✅ Your rule: if late, no overtime
        if (!overtimePolicy.isOvertimeAllowed(log)) return 0;

        if (log.getTimeOut().isAfter(overtimePolicy.getWorkEndTime())) {
            return (int) Duration.between(overtimePolicy.getWorkEndTime(), log.getTimeOut()).toMinutes();
        }
        return 0;
    }
}

