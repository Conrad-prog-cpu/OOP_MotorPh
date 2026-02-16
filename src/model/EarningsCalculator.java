/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package model;
import java.math.BigDecimal;
/**
 *
 * @author ca
 */
public interface EarningsCalculator {
    BigDecimal computeBasicPay(BigDecimal hourlyRate, BigDecimal hoursWorked);

    BigDecimal computeAllowances(Employee employee); 
    // rice + phone + clothing (or only those applicable)

    BigDecimal computeOvertimePay(BigDecimal hourlyRate, BigDecimal overtimeHours, OvertimePolicy policy);

    BigDecimal computeGrossPay(BigDecimal basicPay, BigDecimal allowances, BigDecimal overtimePay);
}