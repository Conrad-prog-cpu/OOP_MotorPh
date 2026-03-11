package service;

import java.time.YearMonth;
import java.util.List;

public interface EmployeeService {

    List<EmployeeRowDto> findAllRows();

    EmployeeDetailsDto findDetailsById(String employeeId);

    boolean existsByEmployeeId(String employeeId);

    boolean addEmployee(EmployeeCreateRequest request);

    boolean updateEmployee(EmployeeUpdateRequest request);

    boolean deleteByEmployeeId(String employeeId);

    List<YearMonth> findAvailablePayrollMonths(String employeeId);
}