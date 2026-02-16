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
public class NonTaxableAllowancesPolicy implements TaxableBenefitsPolicy {

    @Override
    public BigDecimal taxableBenefitsOf(Employee employee) {
        return BigDecimal.ZERO;
    }

    @Override
    public String name() {
        return "Non-taxable allowances";
    }
}