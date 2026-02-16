/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;
import model.*;
import util.PayslipIdGenerator;

import java.time.LocalDate;
import java.time.YearMonth;
/**
 *
 * @author ca
 */
public class PayslipService {

    private final PayslipRepository payslipRepo;

    public PayslipService(PayslipRepository payslipRepo) {
        this.payslipRepo = payslipRepo;
    }

    public Payslip savePayslip(Employee e, PayrollResult result, LocalDate start, LocalDate end) {
        YearMonth ym = YearMonth.from(start);

        String payslipId = PayslipIdGenerator.generate(e.getEmployeeID(), ym, start, end);

        Payslip payslip = new Payslip(
                payslipId,
                e.getEmployeeID(),
                e.getFullName(),
                ym,
                start,
                end,
                result.getHoursWorked(),
                result.getGrossPay(),
                result.getContributions().getSss(),
                result.getContributions().getPhilHealth(),
                result.getContributions().getPagIbig(),
                result.getWithholdingTax(),
                result.getNetPay()
        );

        return payslipRepo.create(payslip);
    }
}
