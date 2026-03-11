package service;

public class UserAccountDto {

    private final String credentialId;
    private final String username;
    private final String role;
    private final String employeeNumber;
    private final String firstName;
    private final String position;

    public UserAccountDto(
            String credentialId,
            String username,
            String role,
            String employeeNumber,
            String firstName,
            String position
    ) {
        this.credentialId = credentialId;
        this.username = username;
        this.role = role;
        this.employeeNumber = employeeNumber;
        this.firstName = firstName;
        this.position = position;
    }

    public String getCredentialId() {
        return credentialId;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getPosition() {
        return position;
    }
}