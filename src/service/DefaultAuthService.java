package service;

import model.Employee;
import model.UserAccount;
import repository.CredentialRepository;
import repository.EmployeeRepository;

import java.util.Objects;
import java.util.Optional;

public class DefaultAuthService implements AuthService {

    private final CredentialRepository credentialRepository;
    private final EmployeeRepository employeeRepository;

    public DefaultAuthService(
            CredentialRepository credentialRepository,
            EmployeeRepository employeeRepository
    ) {
        this.credentialRepository = Objects.requireNonNull(credentialRepository, "credentialRepository is required");
        this.employeeRepository = Objects.requireNonNull(employeeRepository, "employeeRepository is required");
    }

    @Override
    public AuthenticatedUser login(String username, String password) {
        String cleanUsername = safe(username);
        String cleanPassword = safe(password);

        if (cleanUsername.isEmpty() || cleanPassword.isEmpty()) {
            throw new IllegalArgumentException("Username and password are required.");
        }

        credentialRepository.load();
        employeeRepository.load();

        UserAccount account = credentialRepository.validate(cleanUsername, cleanPassword);
        if (account == null) {
            return null;
        }

        Optional<Employee> employeeOpt = employeeRepository.findById(account.getEmployeeNumber());

        String firstName = "(Unknown)";
        String position = "(Unknown)";

        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            firstName = safeOrDefault(employee.getFirstName(), "(Unknown)");
            position = safeOrDefault(employee.getPosition(), "(Unknown)");
        }

        return new AuthenticatedUser(
                safe(account.getCredentialId()),
                safe(account.getUsername()),
                account.getRole(),
                safe(account.getEmployeeNumber()),
                firstName,
                position
        );
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String safeOrDefault(String value, String fallback) {
        String clean = safe(value);
        return clean.isEmpty() ? fallback : clean;
    }
}