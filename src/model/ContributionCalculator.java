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
public interface ContributionCalculator {
    BigDecimal computeSSS(BigDecimal monthlyBasicSalary);
    BigDecimal computePhilHealth(BigDecimal monthlyBasicSalary);
    BigDecimal computePagIbig(BigDecimal monthlyBasicSalary);

    Contributions computeAll(BigDecimal monthlyBasicSalary);
}
