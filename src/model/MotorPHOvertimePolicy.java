/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.math.BigDecimal;
import java.time.LocalTime;

public class MotorPHOvertimePolicy implements OvertimePolicy {

    private static final BigDecimal OT_MULTIPLIER = new BigDecimal("1.25");
    private static final LocalTime GRACE_TIME = LocalTime.of(8, 10); // 8:11 is late
    private static final LocalTime WORK_END = LocalTime.of(17, 0);

    @Override
    public BigDecimal getOvertimeMultiplier() {
        return OT_MULTIPLIER;
    }

    @Override
    public LocalTime getGraceTime() {
        return GRACE_TIME;
    }

    @Override
    public LocalTime getWorkEndTime() {
        return WORK_END;
    }

    // Optional: You can keep defaults in interface OR override here for clarity
    // (not required)
}
