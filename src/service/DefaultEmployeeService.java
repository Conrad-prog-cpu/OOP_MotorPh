package service;

import model.AttendanceLog;
import model.Employee;
import model.ProbationaryEmployee;
import model.RegularEmployee;
import repository.AttendanceRepository;
import repository.EmployeeRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

public class DefaultEmployeeService implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;

    public DefaultEmployeeService(
            EmployeeRepository employeeRepository,
            AttendanceRepository attendanceRepository
    ) {
        this.employeeRepository = Objects.requireNonNull(employeeRepository, "employeeRepository is required");
        this.attendanceRepository = Objects.requireNonNull(attendanceRepository, "attendanceRepository is required");
    }

    @Override
    public List<EmployeeRowDto> findAllRows() {
        employeeRepository.load();

        List<Employee> employees = employeeRepository.findAll();
        List<EmployeeRowDto> rows = new ArrayList<>();

        if (employees == null) {
            return rows;
        }

        for (Employee employee : employees) {
            if (employee == null) {
                continue;
            }

            rows.add(new EmployeeRowDto(
                    safe(employee.getEmployeeID()),
                    safe(employee.getLastName()),
                    safe(employee.getFirstName()),
                    safe(employee.getSssNumber()),
                    safe(employee.getPhilHealthNumber()),
                    safe(employee.getTinNumber()),
                    safe(employee.getPagIbigNumber())
            ));
        }

        return rows;
    }

    @Override
    public EmployeeDetailsDto findDetailsById(String employeeId) {
        employeeRepository.load();

        Employee employee = employeeRepository.findById(safe(employeeId)).orElse(null);
        if (employee == null) {
            return null;
        }

        return new EmployeeDetailsDto(
                safe(employee.getEmployeeID()),
                safe(employee.getLastName()),
                safe(employee.getFirstName()),
                employee.getBirthday() == null ? "" : employee.getBirthday().toString(),
                safe(employee.getAddress()),
                safe(employee.getPhoneNumber()),
                safe(employee.getSssNumber()),
                safe(employee.getPhilHealthNumber()),
                safe(employee.getTinNumber()),
                safe(employee.getPagIbigNumber()),
                safe(employee.getStatus()),
                safe(employee.getPosition()),
                safe(employee.getImmediateSupervisor()),
                formatMoney(employee.getBasicSalary()),
                formatMoney(employee.getRiceSubsidy()),
                formatMoney(employee.getPhoneAllowance()),
                formatMoney(employee.getClothingAllowance()),
                formatMoney(employee.getSemiMonthlyRate()),
                formatMoney(employee.getHourlyRate())
        );
    }

    @Override
    public boolean existsByEmployeeId(String employeeId) {
        employeeRepository.load();
        return employeeRepository.findById(safe(employeeId)).isPresent();
    }

    @Override
    public boolean addEmployee(EmployeeCreateRequest request) {
        employeeRepository.load();

        EmployeeValidationResult validationResult = EmployeeValidator.validateForCreate(request);
        if (!validationResult.isValid()) {
            return false;
        }

        String employeeId = request == null ? "" : safe(request.getEmployeeId());
        if (employeeId.isEmpty()) {
            return false;
        }

        if (existsByEmployeeId(employeeId)) {
            return false;
        }

        try {
            Employee employee = toEmployee(request);
            return employeeRepository.addEmployee(employee);
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public boolean updateEmployee(EmployeeUpdateRequest request) {
        employeeRepository.load();

        EmployeeValidationResult validationResult = EmployeeValidator.validateForUpdate(request);
        if (!validationResult.isValid()) {
            return false;
        }

        String employeeId = request == null ? "" : safe(request.getEmployeeId());
        if (employeeId.isEmpty()) {
            return false;
        }

        Employee existing = employeeRepository.findById(employeeId).orElse(null);
        if (existing == null) {
            return false;
        }

        try {
            Employee updated = toEmployee(request);
            return employeeRepository.updateEmployee(updated);
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public boolean deleteByEmployeeId(String employeeId) {
        employeeRepository.load();
        return employeeRepository.deleteByEmployeeNo(safe(employeeId));
    }

    @Override
    public List<YearMonth> findAvailablePayrollMonths(String employeeId) {
        attendanceRepository.load();

        LocalDate start = LocalDate.of(2000, 1, 1);
        LocalDate end = LocalDate.now();

        List<AttendanceLog> logs = attendanceRepository.findByEmployeeAndDateRange(
                safe(employeeId),
                start,
                end
        );

        SortedSet<YearMonth> months = new TreeSet<>();

        if (logs != null) {
            for (AttendanceLog log : logs) {
                if (log == null || log.getDate() == null) {
                    continue;
                }
                months.add(YearMonth.from(log.getDate()));
            }
        }

        return new ArrayList<>(months);
    }

    private Employee toEmployee(EmployeeCreateRequest request) {
        String status = safe(request.getStatus());

        if ("Probationary".equalsIgnoreCase(status)) {
            return new ProbationaryEmployee(
                    safe(request.getEmployeeId()),
                    safe(request.getLastName()),
                    safe(request.getFirstName()),
                    request.getBirthday(),
                    safe(request.getAddress()),
                    safe(request.getPhoneNumber()),
                    safe(request.getSssNumber()),
                    safe(request.getPhilHealthNumber()),
                    safe(request.getTinNumber()),
                    safe(request.getPagIbigNumber()),
                    safe(request.getStatus()),
                    safe(request.getPosition()),
                    safe(request.getImmediateSupervisor()),
                    nz(request.getBasicSalary()),
                    nz(request.getRiceSubsidy()),
                    nz(request.getPhoneAllowance()),
                    nz(request.getClothingAllowance()),
                    nz(request.getGrossSemiMonthlyRate()),
                    nz(request.getHourlyRate())
            );
        }

        return new RegularEmployee(
                safe(request.getEmployeeId()),
                safe(request.getLastName()),
                safe(request.getFirstName()),
                request.getBirthday(),
                safe(request.getAddress()),
                safe(request.getPhoneNumber()),
                safe(request.getSssNumber()),
                safe(request.getPhilHealthNumber()),
                safe(request.getTinNumber()),
                safe(request.getPagIbigNumber()),
                safe(request.getStatus()),
                safe(request.getPosition()),
                safe(request.getImmediateSupervisor()),
                nz(request.getBasicSalary()),
                nz(request.getRiceSubsidy()),
                nz(request.getPhoneAllowance()),
                nz(request.getClothingAllowance()),
                nz(request.getGrossSemiMonthlyRate()),
                nz(request.getHourlyRate())
        );
    }

    private Employee toEmployee(EmployeeUpdateRequest request) {
        String status = safe(request.getStatus());

        if ("Probationary".equalsIgnoreCase(status)) {
            return new ProbationaryEmployee(
                    safe(request.getEmployeeId()),
                    safe(request.getLastName()),
                    safe(request.getFirstName()),
                    request.getBirthday(),
                    safe(request.getAddress()),
                    safe(request.getPhoneNumber()),
                    safe(request.getSssNumber()),
                    safe(request.getPhilHealthNumber()),
                    safe(request.getTinNumber()),
                    safe(request.getPagIbigNumber()),
                    safe(request.getStatus()),
                    safe(request.getPosition()),
                    safe(request.getImmediateSupervisor()),
                    nz(request.getBasicSalary()),
                    nz(request.getRiceSubsidy()),
                    nz(request.getPhoneAllowance()),
                    nz(request.getClothingAllowance()),
                    nz(request.getGrossSemiMonthlyRate()),
                    nz(request.getHourlyRate())
            );
        }

        return new RegularEmployee(
                safe(request.getEmployeeId()),
                safe(request.getLastName()),
                safe(request.getFirstName()),
                request.getBirthday(),
                safe(request.getAddress()),
                safe(request.getPhoneNumber()),
                safe(request.getSssNumber()),
                safe(request.getPhilHealthNumber()),
                safe(request.getTinNumber()),
                safe(request.getPagIbigNumber()),
                safe(request.getStatus()),
                safe(request.getPosition()),
                safe(request.getImmediateSupervisor()),
                nz(request.getBasicSalary()),
                nz(request.getRiceSubsidy()),
                nz(request.getPhoneAllowance()),
                nz(request.getClothingAllowance()),
                nz(request.getGrossSemiMonthlyRate()),
                nz(request.getHourlyRate())
        );
    }

    private String formatMoney(BigDecimal value) {
        return nz(value).toPlainString();
    }

    private BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}