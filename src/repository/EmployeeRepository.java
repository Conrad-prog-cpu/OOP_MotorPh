/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package repository;

import model.Employee;
import java.util.List;
import java.util.Optional;

public interface EmployeeRepository {
    void load();

    List<String> getHeaders();
    List<String[]> getRows();
    String[] findRowByEmployeeNo(String employeeNo);

    Optional<Employee> findById(String employeeNo);   // ✅ Optional now
    List<Employee> findAll();

    boolean addRow(String[] row);
    boolean addEmployee(Employee emp);

    boolean updateEmployee(Employee emp);
    boolean updateField(String employeeNo, String column, String newValue);

    boolean deleteByEmployeeNo(String employeeNo);
}
