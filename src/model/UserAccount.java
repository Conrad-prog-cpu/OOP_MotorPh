/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author ca
 */
public class UserAccount {
    private final String credentialId;
    private final String username;
    private final Role role;
    private final String employeeNumber;

    public UserAccount(String credentialId, String username, Role role, String employeeNumber) {
        this.credentialId = credentialId;
        this.username = username;
        this.role = role;
        this.employeeNumber = employeeNumber;
    }

    public String getCredentialId() { return credentialId; }
    public String getUsername() { return username; }
    public Role getRole() { return role; }
    public String getEmployeeNumber() { return employeeNumber; }
}
