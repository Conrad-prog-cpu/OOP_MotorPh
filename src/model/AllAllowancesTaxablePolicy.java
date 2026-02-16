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
public class AllAllowancesTaxablePolicy implements TaxableBenefitsPolicy {

    @Override
    public BigDecimal taxableBenefitsOf(Employee employee) {
        return employee.getTotalAllowances();
    }

    @Override
    public String name() {
        return "All allowances taxable";
    }
}
