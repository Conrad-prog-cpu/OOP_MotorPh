package service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

public final class EmployeeValidator {

    private static final int MINIMUM_AGE = 18;

    private EmployeeValidator() {
    }

    public static EmployeeValidationResult validateForCreate(EmployeeCreateRequest request) {
        List<EmployeeValidationResult.FieldError> errors = new ArrayList<>();

        if (request == null) {
            errors.add(new EmployeeValidationResult.FieldError("Form", "Request cannot be null."));
            return new EmployeeValidationResult(errors);
        }

        validateCommon(
                request.getEmployeeId(),
                request.getLastName(),
                request.getFirstName(),
                request.getBirthday(),
                request.getAddress(),
                request.getPhoneNumber(),
                request.getSssNumber(),
                request.getPhilHealthNumber(),
                request.getTinNumber(),
                request.getPagIbigNumber(),
                request.getStatus(),
                request.getPosition(),
                request.getImmediateSupervisor(),
                request.getBasicSalary(),
                request.getRiceSubsidy(),
                request.getPhoneAllowance(),
                request.getClothingAllowance(),
                request.getGrossSemiMonthlyRate(),
                request.getHourlyRate(),
                errors
        );

        return new EmployeeValidationResult(errors);
    }

    public static EmployeeValidationResult validateForUpdate(EmployeeUpdateRequest request) {
        List<EmployeeValidationResult.FieldError> errors = new ArrayList<>();

        if (request == null) {
            errors.add(new EmployeeValidationResult.FieldError("Form", "Request cannot be null."));
            return new EmployeeValidationResult(errors);
        }

        validateCommon(
                request.getEmployeeId(),
                request.getLastName(),
                request.getFirstName(),
                request.getBirthday(),
                request.getAddress(),
                request.getPhoneNumber(),
                request.getSssNumber(),
                request.getPhilHealthNumber(),
                request.getTinNumber(),
                request.getPagIbigNumber(),
                request.getStatus(),
                request.getPosition(),
                request.getImmediateSupervisor(),
                request.getBasicSalary(),
                request.getRiceSubsidy(),
                request.getPhoneAllowance(),
                request.getClothingAllowance(),
                request.getGrossSemiMonthlyRate(),
                request.getHourlyRate(),
                errors
        );

        return new EmployeeValidationResult(errors);
    }

    private static void validateCommon(
            String employeeId,
            String lastName,
            String firstName,
            LocalDate birthday,
            String address,
            String phoneNumber,
            String sssNumber,
            String philHealthNumber,
            String tinNumber,
            String pagIbigNumber,
            String status,
            String position,
            String immediateSupervisor,
            BigDecimal basicSalary,
            BigDecimal riceSubsidy,
            BigDecimal phoneAllowance,
            BigDecimal clothingAllowance,
            BigDecimal grossSemiMonthlyRate,
            BigDecimal hourlyRate,
            List<EmployeeValidationResult.FieldError> errors
    ) {
        require(employeeId, "Employee #", errors);
        require(lastName, "Last Name", errors);
        require(firstName, "First Name", errors);
        require(phoneNumber, "Phone Number", errors);
        require(sssNumber, "SSS #", errors);
        require(philHealthNumber, "Philhealth #", errors);
        require(tinNumber, "TIN #", errors);
        require(pagIbigNumber, "Pag-ibig #", errors);

        validateBirthday(birthday, errors);

        numeric(employeeId, "Employee #", errors);
        numeric(phoneNumber, "Phone Number", errors);
        numeric(sssNumber, "SSS #", errors);
        numeric(philHealthNumber, "Philhealth #", errors);
        numeric(tinNumber, "TIN #", errors);
        numeric(pagIbigNumber, "Pag-ibig #", errors);

        nonNegative(basicSalary, "Basic Salary", errors);
        nonNegative(riceSubsidy, "Rice Subsidy", errors);
        nonNegative(phoneAllowance, "Phone Allowance", errors);
        nonNegative(clothingAllowance, "Clothing Allowance", errors);
        nonNegative(grossSemiMonthlyRate, "Gross Semi-monthly Rate", errors);
        nonNegative(hourlyRate, "Hourly Rate", errors);
    }

    private static void validateBirthday(LocalDate birthday, List<EmployeeValidationResult.FieldError> errors) {
        if (birthday == null) {
            errors.add(new EmployeeValidationResult.FieldError("Birthday", "Birthday is required."));
            return;
        }

        LocalDate today = LocalDate.now();

        if (birthday.isAfter(today)) {
            errors.add(new EmployeeValidationResult.FieldError("Birthday", "Birthday cannot be in the future."));
            return;
        }

        int age = Period.between(birthday, today).getYears();
        if (age < MINIMUM_AGE) {
            errors.add(new EmployeeValidationResult.FieldError("Birthday", "Employee must be at least 18 years old."));
        }
    }

    private static void require(String value, String fieldName, List<EmployeeValidationResult.FieldError> errors) {
        if (value == null || value.trim().isEmpty()) {
            errors.add(new EmployeeValidationResult.FieldError(fieldName, fieldName + " is required."));
        }
    }

    private static void numeric(String value, String fieldName, List<EmployeeValidationResult.FieldError> errors) {
        if (value == null || value.trim().isEmpty()) {
            return;
        }

        if (!value.matches("\\d+")) {
            errors.add(new EmployeeValidationResult.FieldError(fieldName, fieldName + " must contain numbers only."));
        }
    }

    private static void nonNegative(BigDecimal value, String fieldName, List<EmployeeValidationResult.FieldError> errors) {
        if (value == null) {
            return;
        }

        if (value.compareTo(BigDecimal.ZERO) < 0) {
            errors.add(new EmployeeValidationResult.FieldError(fieldName, fieldName + " cannot be negative."));
        }
    }
}