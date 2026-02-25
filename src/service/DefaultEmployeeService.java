package service;

import dao.UserSession;
import model.Employee;
import repository.EmployeeRepository;

import java.util.List;

public class DefaultEmployeeService implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    public DefaultEmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public List<Employee> getEmployees(UserSession session) {

        if (session.isEmployee()) {

            Employee employee = employeeRepository
                    .findById(session.getEmployeeId())
                    .orElseThrow(() -> new RuntimeException("Employee not found"));

            return List.of(employee);
        }

        return employeeRepository.findAll();
    }

    @Override
    public Employee getEmployeeById(UserSession session, String employeeNo) {

        if (session.isEmployee() &&
            !session.getEmployeeId().equals(employeeNo)) {
            throw new RuntimeException("Access denied.");
        }

        return employeeRepository
                .findById(employeeNo)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
    }

    @Override
    public void updateEmployee(UserSession session, Employee employee) {

        if (session.isEmployee() &&
            !session.getEmployeeId().equals(employee.getEmployeeID())) {
            throw new RuntimeException("Cannot edit other employees.");
        }
 
        boolean updated = employeeRepository.updateEmployee(employee);

        if (!updated) {
            throw new RuntimeException("Failed to update employee.");
        }
    }
}