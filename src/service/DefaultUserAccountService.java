package service;

import model.Role;
import model.UserAccount;
import repository.CredentialRepository;
import repository.EmployeeRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DefaultUserAccountService implements UserAccountService {

    private final CredentialRepository credentialRepository;
    private final EmployeeRepository employeeRepository;

    public DefaultUserAccountService(
            CredentialRepository credentialRepository,
            EmployeeRepository employeeRepository
    ) {
        this.credentialRepository = Objects.requireNonNull(credentialRepository, "credentialRepository is required");
        this.employeeRepository = Objects.requireNonNull(employeeRepository, "employeeRepository is required");
    }

    @Override
    public List<UserAccountDto> findAll() {
        credentialRepository.load();
        employeeRepository.load();

        List<UserAccount> accounts = credentialRepository.findAll();
        List<UserAccountDto> dtos = new ArrayList<>();

        if (accounts == null) {
            return dtos;
        }

        for (UserAccount account : accounts) {
            String employeeNo = safe(account.getEmployeeNumber());
            String firstName = "(Unknown)";
            String position = "(Unknown)";

            String[] row = employeeRepository.findRowByEmployeeNo(employeeNo);
            if (row != null) {
                List<String> headers = employeeRepository.getHeaders();
                int firstNameIndex = headers.indexOf("First Name");
                int positionIndex = headers.indexOf("Position");

                if (firstNameIndex >= 0 && firstNameIndex < row.length) {
                    firstName = safe(row[firstNameIndex]);
                }
                if (positionIndex >= 0 && positionIndex < row.length) {
                    position = safe(row[positionIndex]);
                }
            }

            dtos.add(new UserAccountDto(
                    safe(account.getCredentialId()),
                    safe(account.getUsername()),
                    account.getRole() == null ? "" : account.getRole().name(),
                    employeeNo,
                    firstName,
                    position
            ));
        }

        return dtos;
    }

    @Override
    public UserAccountDto findByUsername(String username) {
        credentialRepository.load();
        employeeRepository.load();

        UserAccount account = credentialRepository.findByUsername(safe(username));
        if (account == null) {
            return null;
        }

        String employeeNo = safe(account.getEmployeeNumber());
        String firstName = "(Unknown)";
        String position = "(Unknown)";

        String[] row = employeeRepository.findRowByEmployeeNo(employeeNo);
        if (row != null) {
            List<String> headers = employeeRepository.getHeaders();
            int firstNameIndex = headers.indexOf("First Name");
            int positionIndex = headers.indexOf("Position");

            if (firstNameIndex >= 0 && firstNameIndex < row.length) {
                firstName = safe(row[firstNameIndex]);
            }
            if (positionIndex >= 0 && positionIndex < row.length) {
                position = safe(row[positionIndex]);
            }
        }

        return new UserAccountDto(
                safe(account.getCredentialId()),
                safe(account.getUsername()),
                account.getRole() == null ? "" : account.getRole().name(),
                employeeNo,
                firstName,
                position
        );
    }

    @Override
    public boolean add(UserAccountCreateRequest request) {
        if (request == null) {
            return false;
        }

        return credentialRepository.add(
                safe(request.getUsername()),
                safe(request.getPassword()),
                Role.from(safe(request.getRole())),
                safe(request.getEmployeeNumber())
        );
    }

    @Override
    public boolean update(UserAccountUpdateRequest request) {
        if (request == null || safe(request.getUsername()).isEmpty()) {
            return false;
        }

        boolean changed = false;
        boolean ok = true;

        if (!safe(request.getNewPassword()).isEmpty()) {
            ok &= credentialRepository.updatePassword(
                    safe(request.getUsername()),
                    safe(request.getNewPassword())
            );
            changed = true;
        }

        if (!safe(request.getNewRole()).isEmpty()) {
            ok &= credentialRepository.updateRole(
                    safe(request.getUsername()),
                    Role.from(safe(request.getNewRole()))
            );
            changed = true;
        }

        if (!safe(request.getNewEmployeeNumber()).isEmpty()) {
            ok &= credentialRepository.updateEmployeeNo(
                    safe(request.getUsername()),
                    safe(request.getNewEmployeeNumber())
            );
            changed = true;
        }

        return changed && ok;
    }

    @Override
    public boolean deleteByUsername(String username) {
        return credentialRepository.delete(safe(username));
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}