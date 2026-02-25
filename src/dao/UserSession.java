/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

/**
 *
 * @author ca
 */
public class UserSession {
    private final String username;
    private final String role;
    private final String employeeId;

    public UserSession(String username, String role, String employeeId) {
        this.username = username;
        this.role = role;
        this.employeeId = employeeId;
    }

    public boolean isEmployee() {
        return "EMPLOYEE".equalsIgnoreCase(role);
    }

    public boolean isHR() {
        return "HR".equalsIgnoreCase(role);
    }

    public boolean isAdmin() {
        return "HRADMIN".equalsIgnoreCase(role);
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getRole() {
        return role;
    }
}