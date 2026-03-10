package service;

import model.Employee;
import model.UserAccount;
import repository.CredentialRepository;
import repository.EmployeeRepository;

import java.util.Objects;
import java.util.Optional;

public class DefaultAuthService implements AuthService {

    private final CredentialRepository credentialRepo;
    private final EmployeeRepository employeeRepo;

    public DefaultAuthService(CredentialRepository credentialRepo, EmployeeRepository employeeRepo) {
        this.credentialRepo = Objects.requireNonNull(credentialRepo, "credentialRepo is required");
        this.employeeRepo = Objects.requireNonNull(employeeRepo, "employeeRepo is required");
    }

    @Override
    public AuthenticatedUser login(String username, String password) {
        String cleanUsername = safe(username);
        String cleanPassword = safe(password);

        if (cleanUsername.isEmpty() || cleanPassword.isEmpty()) {
            throw new IllegalArgumentException("Username and password are required.");
        }

        credentialRepo.load();
        employeeRepo.load();

        UserAccount account = credentialRepo.validate(cleanUsername, cleanPassword);
        if (account == null) {
            return null;
        }

        Optional<Employee> employeeOpt = employeeRepo.findById(account.getEmployeeNumber());

        String firstName = "(Unknown)";
        String position = "(Unknown)";

        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            firstName = safeOrDefault(employee.getFirstName(), "(Unknown)");
            position = safeOrDefault(employee.getPosition(), "(Unknown)");
        }

        return new AuthenticatedUser(
                account.getCredentialId(),
                account.getUsername(),
                account.getRole(),
                account.getEmployeeNumber(),
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