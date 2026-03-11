package service;

public class UserAccountCreateRequest {

    private final String username;
    private final String password;
    private final String role;
    private final String employeeNumber;

    public UserAccountCreateRequest(
            String username,
            String password,
            String role,
            String employeeNumber
    ) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.employeeNumber = employeeNumber;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }
}