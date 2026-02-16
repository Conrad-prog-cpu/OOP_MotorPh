/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;
import java.math.BigDecimal;
/**
 *
 * @author ca
 */
public class WorkHoursSummary {
    private final int totalMinutesWorked;
    private final int totalLateMinutes;
    private final int totalOvertimeMinutes;

    public WorkHoursSummary(int totalMinutesWorked, int totalLateMinutes, int totalOvertimeMinutes) {
        this.totalMinutesWorked = totalMinutesWorked;
        this.totalLateMinutes = totalLateMinutes;
        this.totalOvertimeMinutes = totalOvertimeMinutes;
    }

    public int getTotalMinutesWorked() { return totalMinutesWorked; }
    public int getTotalLateMinutes() { return totalLateMinutes; }
    public int getTotalOvertimeMinutes() { return totalOvertimeMinutes; }

    public BigDecimal getTotalHoursWorked() {
        return BigDecimal.valueOf(totalMinutesWorked).divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP);
    }

    public BigDecimal getOvertimeHoursWorked() {
        return BigDecimal.valueOf(totalOvertimeMinutes).divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP);
    }
}
