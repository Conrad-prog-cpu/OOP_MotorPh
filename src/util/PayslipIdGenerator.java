/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 *
 * @author ca
 */
public class PayslipIdGenerator {
    public static String generate(String employeeId, YearMonth ym, LocalDate start, LocalDate end) {
        return employeeId + "|" + ym + "|" + start + "|" + end;
    }
}
