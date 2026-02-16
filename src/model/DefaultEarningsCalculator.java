/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DefaultEarningsCalculator implements EarningsCalculator {

    @Override
    public BigDecimal computeBasicPay(BigDecimal hourlyRate, BigDecimal hoursWorked) {
        if (hourlyRate == null || hoursWorked == null) return BigDecimal.ZERO;
        return hourlyRate.multiply(hoursWorked).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal computeAllowances(Employee employee) {
        if (employee == null) return BigDecimal.ZERO;

        BigDecimal rice = employee.getRiceSubsidy();
        BigDecimal phone = employee.getPhoneAllowance();
        BigDecimal clothing = employee.getClothingAllowance();

        return rice.add(phone).add(clothing).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal computeOvertimePay(BigDecimal hourlyRate,
                                         BigDecimal overtimeHours,
                                         OvertimePolicy policy) {

        if (hourlyRate == null || overtimeHours == null || policy == null)
            return BigDecimal.ZERO;

        BigDecimal multiplier = policy.getOvertimeMultiplier();  // ✅ FIXED

        return hourlyRate
                .multiply(overtimeHours)
                .multiply(multiplier)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal computeGrossPay(BigDecimal basicPay,
                                      BigDecimal allowances,
                                      BigDecimal overtimePay) {

        if (basicPay == null) basicPay = BigDecimal.ZERO;
        if (allowances == null) allowances = BigDecimal.ZERO;
        if (overtimePay == null) overtimePay = BigDecimal.ZERO;

        return basicPay.add(allowances).add(overtimePay)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
