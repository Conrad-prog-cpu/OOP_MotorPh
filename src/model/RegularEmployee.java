/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;
import java.math.BigDecimal;
import java.time.LocalDate;
/**
 *
 * @author ca
 */
public class RegularEmployee extends Employee {

    public RegularEmployee(String employeeID, String lastName, String firstName, LocalDate birthday, String address, String phoneNumber, String sssNumber, String philHealthNumber, String tinNumber, String pagIbigNumber, String status, String position, String immediateSupervisor, BigDecimal basicSalary, BigDecimal riceSubsidy, BigDecimal phoneAllowance, BigDecimal clothingAllowance, BigDecimal semiMonthlyRate, BigDecimal hourlyRate) {
        super(employeeID, lastName, firstName, birthday, address, phoneNumber, sssNumber, philHealthNumber, tinNumber, pagIbigNumber, status, position, immediateSupervisor, basicSalary, riceSubsidy, phoneAllowance, clothingAllowance, semiMonthlyRate, hourlyRate);
    }
    

    @Override
    public BigDecimal computeGrossPay(BigDecimal hoursWorked) {
        return getHourlyRate().multiply(hoursWorked).add(getTotalAllowances());
    }

    @Override
    public BigDecimal computeNetPay(BigDecimal hoursWorked) {
        // later call deduction/tax services
        return computeGrossPay(hoursWorked);
    }
}
