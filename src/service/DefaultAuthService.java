/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;
import model.Role;
import model.User;
import model.UserAccount;
import repository.CredentialRepository;
import repository.EmployeeRepository;
/**
 *
 * @author ca
 */
public class DefaultAuthService implements AuthService {

    private final CredentialRepository credentialRepo;
    private final EmployeeRepository employeeRepo;

    public DefaultAuthService(CredentialRepository credentialRepo, EmployeeRepository employeeRepo) {
        this.credentialRepo = credentialRepo;
        this.employeeRepo = employeeRepo;
        this.employeeRepo.load(); // needed to map employee# -> firstName/position
    }

    @Override
    public User login(String username, String password) {
        UserAccount account = credentialRepo.validate(username, password);
        if (account == null) return null;

        String[] empRow = employeeRepo.findRowByEmployeeNo(account.getEmployeeNumber());

        String firstName = "(Unknown)";
        String position = "(Unknown)";

        if (empRow != null) {
            int firstNameIdx = employeeRepo.getHeaders().indexOf("First Name");
            int positionIdx  = employeeRepo.getHeaders().indexOf("Position");

            if (firstNameIdx >= 0 && firstNameIdx < empRow.length) firstName = empRow[firstNameIdx];
            if (positionIdx  >= 0 && positionIdx  < empRow.length) position  = empRow[positionIdx];
        }

        return new User(
                account.getCredentialId(),
                account.getUsername(),
                account.getRole(),
                account.getEmployeeNumber(),
                firstName,
                position
        );
    }
}