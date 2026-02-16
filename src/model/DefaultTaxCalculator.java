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
public class DefaultTaxCalculator implements TaxCalculator {

    @Override
    public BigDecimal computeTaxableIncome(BigDecimal grossPay, Contributions contributions, TaxableBenefitsPolicy benefitsPolicy) {
        // simple: gross - contributions
        return grossPay.subtract(contributions.total()).max(BigDecimal.ZERO);
    }

    @Override
    public BigDecimal computeWithholdingTax(BigDecimal taxableIncome) {
        // Placeholder simplified tax rule
        if (taxableIncome.compareTo(BigDecimal.valueOf(20000)) <= 0) return BigDecimal.ZERO;
        return taxableIncome.subtract(BigDecimal.valueOf(20000)).multiply(BigDecimal.valueOf(0.10));
    }

    @Override
    public BigDecimal computeNetPay(BigDecimal grossPay, Contributions contributions, BigDecimal withholdingTax) {
        return grossPay.subtract(contributions.total()).subtract(withholdingTax);
    }
}
