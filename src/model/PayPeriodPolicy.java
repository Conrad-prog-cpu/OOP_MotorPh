/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package model;
import java.time.LocalDate;
/**
 *
 * @author ca
 */
public interface PayPeriodPolicy {
    LocalDate periodStart(int year, int month);     // or accept YearMonth
    LocalDate periodEnd(int year, int month);

    boolean isSemiMonthly(); // optional
    String name();           // "Monthly", "Semi-monthly"
}
