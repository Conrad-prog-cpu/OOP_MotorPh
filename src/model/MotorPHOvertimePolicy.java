package model;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Objects;

public class MotorPHOvertimePolicy implements OvertimePolicy {

    private static final BigDecimal DEFAULT_OVERTIME_MULTIPLIER = new BigDecimal("1.25");

    private static final LocalTime DEFAULT_WORK_START_TIME = LocalTime.of(8, 0);
    private static final LocalTime DEFAULT_GRACE_TIME = LocalTime.of(8, 10);
    private static final LocalTime DEFAULT_WORK_END_TIME = LocalTime.of(17, 0);

    private final BigDecimal overtimeMultiplier;
    private final LocalTime workStartTime;
    private final LocalTime graceTime;
    private final LocalTime workEndTime;

    public MotorPHOvertimePolicy() {
        this(
                DEFAULT_OVERTIME_MULTIPLIER,
                DEFAULT_WORK_START_TIME,
                DEFAULT_GRACE_TIME,
                DEFAULT_WORK_END_TIME
        );
    }

    public MotorPHOvertimePolicy(
            BigDecimal overtimeMultiplier,
            LocalTime workStartTime,
            LocalTime graceTime,
            LocalTime workEndTime
    ) {
        this.overtimeMultiplier = Objects.requireNonNull(overtimeMultiplier, "overtimeMultiplier is required");
        this.workStartTime = Objects.requireNonNull(workStartTime, "workStartTime is required");
        this.graceTime = Objects.requireNonNull(graceTime, "graceTime is required");
        this.workEndTime = Objects.requireNonNull(workEndTime, "workEndTime is required");

        validateTimes();
    }

    @Override
    public BigDecimal getOvertimeMultiplier() {
        return overtimeMultiplier;
    }

    public LocalTime getWorkStartTime() {
        return workStartTime;
    }

    @Override
    public LocalTime getGraceTime() {
        return graceTime;
    }

    @Override
    public LocalTime getWorkEndTime() {
        return workEndTime;
    }

    @Override
    public boolean isLate(AttendanceLog log) {
        if (log == null || log.getTimeIn() == null) {
            return false;
        }

        LocalTime actualTimeIn = log.getTimeIn();
        return actualTimeIn.isAfter(graceTime);
    }

    @Override
    public boolean isOvertimeAllowed(AttendanceLog log) {
        if (log == null || log.getTimeIn() == null || log.getTimeOut() == null) {
            return false;
        }

        if (isLate(log)) {
            return false;
        }

        return log.getTimeOut().isAfter(workEndTime);
    }

    public int computeLateMinutes(AttendanceLog log) {
        if (!isLate(log)) {
            return 0;
        }

        return (int) java.time.Duration.between(graceTime, log.getTimeIn()).toMinutes();
    }

    public int computeOvertimeMinutes(AttendanceLog log) {
        if (!isOvertimeAllowed(log)) {
            return 0;
        }

        return (int) java.time.Duration.between(workEndTime, log.getTimeOut()).toMinutes();
    }

    private void validateTimes() {
        if (graceTime.isBefore(workStartTime)) {
            throw new IllegalArgumentException("Grace time cannot be earlier than work start time.");
        }

        if (!workEndTime.isAfter(workStartTime)) {
            throw new IllegalArgumentException("Work end time must be after work start time.");
        }
    }
}