/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package service;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import model.Payslip;
/**
 *
 * @author ca
 */
public interface PayslipRepository {
    Payslip create(Payslip entity);
    Optional<Payslip> findById(String payslipId);
    List<Payslip> findAll();
    Payslip update(Payslip entity);
    boolean deleteById(String payslipId);

    List<Payslip> findByEmployeeAndMonth(String employeeId, YearMonth month);
    List<Payslip> findByMonth(YearMonth month);
}