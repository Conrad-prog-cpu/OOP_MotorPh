/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package model;

import java.math.BigDecimal;
import java.time.LocalTime;

public interface OvertimePolicy {

    // e.g. 1.25
    BigDecimal getOvertimeMultiplier();

    // grace cutoff: 8:10 means 8:11 onward is late
    LocalTime getGraceTime();

    // official work end time for OT calculation
    LocalTime getWorkEndTime();

    // ✅ Late logic lives here
    default boolean isLate(AttendanceLog log) {
        if (log == null || log.getTimeIn() == null) return false;
        return log.getTimeIn().isAfter(getGraceTime());
    }

    // ✅ Your rule: if late, no overtime will be applied
    default boolean isOvertimeAllowed(AttendanceLog log) {
        return !isLate(log);
    }
}