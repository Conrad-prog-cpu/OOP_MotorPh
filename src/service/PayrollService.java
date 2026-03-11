/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package service;
import model.PayrollResult;
import java.time.LocalDate;
/**
 *
 * @author ca
 */
public interface PayrollService {
    PayrollResult computeForDateRange(String employeeNumber, LocalDate start, LocalDate end);
    PayrollResult computeForMonth(String employeeNumber, int year, int month);
    PayrollResultDto computeForMonthDto(String employeeId, int year, int month);
}
