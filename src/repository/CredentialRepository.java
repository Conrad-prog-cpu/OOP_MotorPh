/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package repository;

import model.Role;
import model.UserAccount;
import java.util.List;
/**
 *
 * @author ca
 */
public interface CredentialRepository {
    List<UserAccount> findAll();
    UserAccount findByUsername(String username);

    // ✅ NEW: validates username+password and returns UserAccount if correct
    UserAccount validate(String username, String password);

    boolean add(String username, String password, Role role, String employeeNo);
    boolean delete(String username);

    boolean updatePassword(String username, String newPassword);
    boolean updateRole(String username, Role newRole);
    boolean updateEmployeeNo(String username, String newEmployeeNo);
}