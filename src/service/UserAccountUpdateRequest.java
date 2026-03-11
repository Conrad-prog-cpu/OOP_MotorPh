package service;

public class UserAccountUpdateRequest {

    private final String username;
    private final String newPassword;
    private final String newRole;
    private final String newEmployeeNumber;

    public UserAccountUpdateRequest(
            String username,
            String newPassword,
            String newRole,
            String newEmployeeNumber
    ) {
        this.username = username;
        this.newPassword = newPassword;
        this.newRole = newRole;
        this.newEmployeeNumber = newEmployeeNumber;
    }

    public String getUsername() {
        return username;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public String getNewRole() {
        return newRole;
    }

    public String getNewEmployeeNumber() {
        return newEmployeeNumber;
    }
}