/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package service;

/**
 *
 * @author ca
 */
import dao.UserSession;
import model.Employee;

import java.util.List;

public interface EmployeeService {

    List<Employee> getEmployees(UserSession session);

    Employee getEmployeeById(UserSession session, String employeeId);

    void updateEmployee(UserSession session, Employee employee);
}
