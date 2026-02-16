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
public class DefaultTaxableBenefitsPolicy implements TaxableBenefitsPolicy {

    @Override
    public BigDecimal taxableBenefitsOf(Employee employee) {
        // If you want: treat allowances as taxable benefits
        // return employee.getTotalAllowances();

        // If you want: benefits NOT taxable (common in your earlier logic)
        return BigDecimal.ZERO;
    }

    @Override
    public String name() {
        return "Default Taxable Benefits Policy";
    }
}
