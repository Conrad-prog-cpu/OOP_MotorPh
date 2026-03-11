package repository;

import model.Role;
import model.UserAccount;

import java.util.List;

public interface CredentialRepository {

    void load();

    List<UserAccount> findAll();

    UserAccount findByUsername(String username);

    UserAccount validate(String username, String password);

    boolean add(String username, String password, Role role, String employeeNo);

    boolean delete(String username);

    boolean updatePassword(String username, String newPassword);

    boolean updateRole(String username, Role newRole);

    boolean updateEmployeeNo(String username, String newEmployeeNo);
}