/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;
/**
 *
 * @author ca
 */
public class User {
    private final String credentialId;
    private final String username;
    private final Role role;
    private final String employeeNumber;

    // from employee.txt
    private final String firstName;
    private final String position;

    public User(String credentialId,
                String username,
                Role role,
                String employeeNumber,
                String firstName,
                String position) {
        this.credentialId = credentialId;
        this.username = username;
        this.role = role;
        this.employeeNumber = employeeNumber;
        this.firstName = firstName;
        this.position = position;
    }

    public String getCredentialId() { return credentialId; }
    public String getUsername() { return username; }
    public Role getRole() { return role; }
    public String getEmployeeNumber() { return employeeNumber; }

    public String getFirstName() { return firstName; }
    public String getPosition() { return position; }

    // Access control helpers
    public boolean isEmployee() { return role == Role.EMPLOYEE; }
    public boolean isHrAdmin() { return role == Role.HRADMIN; }
    public boolean isIt() { return role == Role.IT; }

    public boolean canAccessAll() {
        return role == Role.HRADMIN || role == Role.IT;
    }

    public boolean canManageEmployees() {
        return canAccessAll(); // HRADMIN/IT can manage employees
    }

    public boolean canApproveLeaves() {
        return canAccessAll();
    }
}
