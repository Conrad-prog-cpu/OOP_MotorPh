package model;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

public class DefaultWorkHoursCalculator implements WorkHoursCalculator {

    private static final int LUNCH_BREAK_MINUTES = 60;

    private final OvertimePolicy overtimePolicy;

    public DefaultWorkHoursCalculator(OvertimePolicy overtimePolicy) {
        this.overtimePolicy = Objects.requireNonNull(overtimePolicy, "overtimePolicy is required");
    }

    @Override
    public WorkHoursSummary summarize(List<AttendanceLog> logs) {
        int totalWorkedMinutes = 0;
        int totalLateMinutes = 0;
        int totalOvertimeMinutes = 0;

        if (logs == null || logs.isEmpty()) {
            return new WorkHoursSummary(0, 0, 0);
        }

        for (AttendanceLog log : logs) {
            if (!isComputable(log)) {
                continue;
            }

            totalWorkedMinutes += computeMinutesWorked(log);
            totalLateMinutes += computeLateMinutes(log);
            totalOvertimeMinutes += computeOvertimeMinutes(log);
        }

        return new WorkHoursSummary(
                totalWorkedMinutes,
                totalLateMinutes,
                totalOvertimeMinutes
        );
    }

    @Override
    public int computeMinutesWorked(AttendanceLog log) {
        if (!hasCompleteTime(log)) {
            return 0;
        }

        LocalTime timeIn = log.getTimeIn();
        LocalTime timeOut = log.getTimeOut();

        if (!timeOut.isAfter(timeIn)) {
            return 0;
        }

        int rawMinutesWorked = (int) Duration.between(timeIn, timeOut).toMinutes();
        int netMinutesWorked = rawMinutesWorked - LUNCH_BREAK_MINUTES;

        return Math.max(netMinutesWorked, 0);
    }

    @Override
    public int computeLateMinutes(AttendanceLog log) {
        if (log == null || log.getTimeIn() == null) {
            return 0;
        }

        if (!overtimePolicy.isLate(log)) {
            return 0;
        }

        LocalTime graceTime = overtimePolicy.getGraceTime();
        LocalTime actualTimeIn = log.getTimeIn();

        if (graceTime == null || actualTimeIn == null) {
            return 0;
        }

        if (!actualTimeIn.isAfter(graceTime)) {
            return 0;
        }

        return (int) Duration.between(graceTime, actualTimeIn).toMinutes();
    }

    @Override
    public int computeOvertimeMinutes(AttendanceLog log) {
        if (!hasCompleteTime(log)) {
            return 0;
        }

        if (!overtimePolicy.isOvertimeAllowed(log)) {
            return 0;
        }

        LocalTime workEndTime = overtimePolicy.getWorkEndTime();
        LocalTime actualTimeOut = log.getTimeOut();

        if (workEndTime == null || actualTimeOut == null) {
            return 0;
        }

        if (!actualTimeOut.isAfter(workEndTime)) {
            return 0;
        }

        return (int) Duration.between(workEndTime, actualTimeOut).toMinutes();
    }

    private boolean isComputable(AttendanceLog log) {
        return log != null && hasCompleteTime(log);
    }

    private boolean hasCompleteTime(AttendanceLog log) {
        return log != null
                && log.getTimeIn() != null
                && log.getTimeOut() != null;
    }
}