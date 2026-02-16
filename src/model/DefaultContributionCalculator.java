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
public class DefaultContributionCalculator implements ContributionCalculator {

    @Override
    public BigDecimal computeSSS(BigDecimal monthlyBasicSalary) {
        // Placeholder logic; replace with SSS bracket table later
        return monthlyBasicSalary.multiply(BigDecimal.valueOf(0.045));
    }

    @Override
    public BigDecimal computePhilHealth(BigDecimal monthlyBasicSalary) {
        return monthlyBasicSalary.multiply(BigDecimal.valueOf(0.03));
    }

    @Override
    public BigDecimal computePagIbig(BigDecimal monthlyBasicSalary) {
        // Example cap
        BigDecimal max = BigDecimal.valueOf(100);
        BigDecimal computed = monthlyBasicSalary.multiply(BigDecimal.valueOf(0.02));
        return computed.min(max);
    }

    @Override
    public Contributions computeAll(BigDecimal monthlyBasicSalary) {
        return new Contributions(
                computeSSS(monthlyBasicSalary),
                computePhilHealth(monthlyBasicSalary),
                computePagIbig(monthlyBasicSalary)
        );
    }
}
