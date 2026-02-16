package model;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Attendance {

    private static final LocalTime STANDARD_START = LocalTime.of(8, 0);
    private static final int GRACE_PERIOD_MINUTES = 10;

    /**
     * Calculates lateness in minutes after the grace period.
     * @param loginTimeStr employee login time in HH:mm format (24-hour)
     * @return number of minutes late beyond grace period, or 0 if within grace period
     */
    public static int calculateLateness(String loginTimeStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime loginTime = LocalTime.parse(loginTimeStr, formatter);

        LocalTime graceTime = STANDARD_START.plusMinutes(GRACE_PERIOD_MINUTES);

        if (loginTime.isAfter(graceTime)) {
            return (int) java.time.Duration.between(graceTime, loginTime).toMinutes();
        }
        return 0;
    }

    /**
     * Optionally calculates monetary deduction based on hourly rate.
     * @param minutesLate Number of minutes late after grace period
     * @param hourlyRate Employee's hourly rate
     * @return Deduction amount in currency
     */
    public static double calculateLateDeduction(int minutesLate, double hourlyRate) {
        double perMinuteRate = hourlyRate / 60.0;
        return minutesLate * perMinuteRate;
    }
}