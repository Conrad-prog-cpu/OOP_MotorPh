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
public interface TaxCalculator {
    BigDecimal computeTaxableIncome(BigDecimal grossPay, Contributions contributions, TaxableBenefitsPolicy benefitsPolicy);
    BigDecimal computeWithholdingTax(BigDecimal taxableIncome);

    BigDecimal computeNetPay(BigDecimal grossPay, Contributions contributions, BigDecimal withholdingTax);
}
