package repository;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import model.Role;
import model.UserAccount;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class FileCredentialRepository implements CredentialRepository {

    private static final String CREDENTIALS_FILE_PATH = "data/credentials.csv";

    private final List<String> headers = new ArrayList<>();
    private final List<Rec> records = new ArrayList<>();

    private static class Rec {
        String id;
        String username;
        String password;
        String employeeNo;
        Role role;
    }

    @Override
    public void load() {
        headers.clear();
        records.clear();

        File file = new File(CREDENTIALS_FILE_PATH);
        if (!file.exists()) {
            headers.addAll(Arrays.asList(defaultHeaders()));
            return;
        }

        try (CSVReader reader = new CSVReaderBuilder(new FileReader(file)).build()) {
            String[] headerRow = reader.readNext();

            if (headerRow == null || headerRow.length == 0) {
                headers.addAll(Arrays.asList(defaultHeaders()));
                return;
            }

            for (String header : headerRow) {
                headers.add(clean(header));
            }

            String[] row;
            while ((row = reader.readNext()) != null) {
                if (isBlankRow(row)) {
                    continue;
                }

                Rec rec = parseRow(adjustRowLength(cleanRow(row), headers.size()));
                if (rec != null) {
                    records.add(rec);
                }
            }

        } catch (IOException | CsvValidationException e) {
            System.err.println("Error reading credentials.csv: " + e.getMessage());
            headers.clear();
            records.clear();
            headers.addAll(Arrays.asList(defaultHeaders()));
        }
    }

    @Override
    public List<UserAccount> findAll() {
        load();

        List<UserAccount> accounts = new ArrayList<>();
        for (Rec rec : records) {
            accounts.add(toUserAccount(rec));
        }
        return accounts;
    }

    @Override
    public UserAccount findByUsername(String username) {
        load();

        Rec rec = findRec(username);
        return rec == null ? null : toUserAccount(rec);
    }

    @Override
    public UserAccount validate(String username, String password) {
        load();

        String targetUsername = safe(username);
        String targetPassword = safe(password);

        for (Rec rec : records) {
            if (rec.username.equalsIgnoreCase(targetUsername)
                    && rec.password.equals(targetPassword)) {
                return toUserAccount(rec);
            }
        }

        return null;
    }

    @Override
    public boolean add(String username, String password, Role role, String employeeNo) {
        load();

        String cleanUsername = safe(username);
        String cleanPassword = safe(password);
        String cleanEmployeeNo = safe(employeeNo);
        Role finalRole = role == null ? Role.EMPLOYEE : role;

        if (cleanUsername.isEmpty() || cleanPassword.isEmpty()) {
            return false;
        }

        if (findRec(cleanUsername) != null) {
            return false;
        }

        Rec rec = new Rec();
        rec.id = String.valueOf(getNextId());
        rec.username = cleanUsername;
        rec.password = cleanPassword;
        rec.role = finalRole;
        rec.employeeNo = cleanEmployeeNo;

        records.add(rec);
        return writeAll();
    }

    @Override
    public boolean delete(String username) {
        load();

        String targetUsername = safe(username);
        boolean removed = records.removeIf(rec -> rec.username.equalsIgnoreCase(targetUsername));

        return removed && writeAll();
    }

    @Override
    public boolean updatePassword(String username, String newPassword) {
        load();

        String targetUsername = safe(username);
        String cleanPassword = safe(newPassword);

        if (cleanPassword.isEmpty()) {
            return false;
        }

        boolean changed = false;

        for (Rec rec : records) {
            if (rec.username.equalsIgnoreCase(targetUsername)) {
                if (!Objects.equals(rec.password, cleanPassword)) {
                    rec.password = cleanPassword;
                    changed = true;
                }
                break;
            }
        }

        return changed && writeAll();
    }

    @Override
    public boolean updateRole(String username, Role newRole) {
        load();

        String targetUsername = safe(username);
        Role finalRole = newRole == null ? Role.EMPLOYEE : newRole;

        boolean changed = false;

        for (Rec rec : records) {
            if (rec.username.equalsIgnoreCase(targetUsername)) {
                if (rec.role != finalRole) {
                    rec.role = finalRole;
                    changed = true;
                }
                break;
            }
        }

        return changed && writeAll();
    }

    @Override
    public boolean updateEmployeeNo(String username, String newEmployeeNo) {
        load();

        String targetUsername = safe(username);
        String cleanEmployeeNo = safe(newEmployeeNo);

        boolean changed = false;

        for (Rec rec : records) {
            if (rec.username.equalsIgnoreCase(targetUsername)) {
                if (!Objects.equals(rec.employeeNo, cleanEmployeeNo)) {
                    rec.employeeNo = cleanEmployeeNo;
                    changed = true;
                }
                break;
            }
        }

        return changed && writeAll();
    }

    private boolean writeAll() {
        ensureHeadersLoaded();

        try (ICSVWriter writer = new CSVWriterBuilder(new FileWriter(CREDENTIALS_FILE_PATH))
                .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
                .withQuoteChar(CSVWriter.DEFAULT_QUOTE_CHARACTER)
                .build()) {

            writer.writeNext(headers.toArray(new String[0]));

            for (Rec rec : records) {
                writer.writeNext(toRow(rec));
            }

            return true;

        } catch (IOException e) {
            System.err.println("Error writing credentials.csv: " + e.getMessage());
            return false;
        }
    }

    private void ensureHeadersLoaded() {
        if (headers.isEmpty()) {
            headers.addAll(Arrays.asList(defaultHeaders()));
        }
    }

    private Rec findRec(String username) {
        String targetUsername = safe(username);

        for (Rec rec : records) {
            if (rec.username.equalsIgnoreCase(targetUsername)) {
                return rec;
            }
        }

        return null;
    }

    private int getNextId() {
        int max = 0;

        for (Rec rec : records) {
            try {
                max = Math.max(max, Integer.parseInt(safe(rec.id)));
            } catch (NumberFormatException ignored) {
            }
        }

        return max + 1;
    }

    private Rec parseRow(String[] row) {
        int idIndex = getIdIndex();
        int usernameIndex = getUsernameIndex();
        int passwordIndex = getPasswordIndex();
        int roleIndex = getRoleIndex();
        int employeeNoIndex = getEmployeeNoIndex();

        if (idIndex == -1 || usernameIndex == -1 || passwordIndex == -1
                || roleIndex == -1 || employeeNoIndex == -1) {
            return null;
        }

        Rec rec = new Rec();
        rec.id = safe(getValue(row, idIndex));
        rec.username = safe(getValue(row, usernameIndex));
        rec.password = safe(getValue(row, passwordIndex));
        rec.role = Role.from(safe(getValue(row, roleIndex)));
        rec.employeeNo = safe(getValue(row, employeeNoIndex));

        if (rec.username.isEmpty()) {
            return null;
        }

        return rec;
    }

    private String[] toRow(Rec rec) {
        String[] row = new String[headers.size()];

        setValue(row, getIdIndex(), rec.id);
        setValue(row, getUsernameIndex(), rec.username);
        setValue(row, getPasswordIndex(), rec.password);
        setValue(row, getRoleIndex(), rec.role == null ? Role.EMPLOYEE.name() : rec.role.name());
        setValue(row, getEmployeeNoIndex(), rec.employeeNo);

        return row;
    }

    private UserAccount toUserAccount(Rec rec) {
        return new UserAccount(
                safe(rec.id),
                safe(rec.username),
                rec.role == null ? Role.EMPLOYEE : rec.role,
                safe(rec.employeeNo)
        );
    }

    private int getIdIndex() {
        return findFirstMatchingHeader("ID", "Id", "UserID", "AccountID");
    }

    private int getUsernameIndex() {
        return findFirstMatchingHeader("Username", "User Name", "User");
    }

    private int getPasswordIndex() {
        return findFirstMatchingHeader("Password", "Pass", "UserPassword");
    }

    private int getRoleIndex() {
        return findFirstMatchingHeader("Role", "UserRole", "AccountRole");
    }

    private int getEmployeeNoIndex() {
        return findFirstMatchingHeader("EmployeeNo", "Employee No", "Employee #", "EmployeeID", "Employee Id");
    }

    private int findFirstMatchingHeader(String... candidates) {
        for (String candidate : candidates) {
            int index = findColumnIndex(candidate);
            if (index != -1) {
                return index;
            }
        }
        return -1;
    }

    private int findColumnIndex(String columnName) {
        if (columnName == null || headers.isEmpty()) {
            return -1;
        }

        String target = normalizeHeader(columnName);

        for (int i = 0; i < headers.size(); i++) {
            if (normalizeHeader(headers.get(i)).equals(target)) {
                return i;
            }
        }

        return -1;
    }

    private String normalizeHeader(String value) {
        return safe(value).replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
    }

    private String getValue(String[] row, int index) {
        if (row == null || index < 0 || index >= row.length) {
            return "";
        }
        return row[index];
    }

    private void setValue(String[] row, int index, String value) {
        if (row == null || index < 0 || index >= row.length) {
            return;
        }
        row[index] = safe(value);
    }

    private String[] adjustRowLength(String[] row, int expected) {
        if (row == null) {
            return new String[expected];
        }

        if (row.length < expected) {
            String[] padded = Arrays.copyOf(row, expected);
            Arrays.fill(padded, row.length, expected, "");
            return padded;
        }

        if (row.length > expected) {
            return Arrays.copyOf(row, expected);
        }

        return row;
    }

    private String[] cleanRow(String[] row) {
        String[] cleaned = new String[row.length];
        for (int i = 0; i < row.length; i++) {
            cleaned[i] = clean(row[i]);
        }
        return cleaned;
    }

    private boolean isBlankRow(String[] row) {
        if (row == null || row.length == 0) {
            return true;
        }

        for (String value : row) {
            if (value != null && !value.trim().isEmpty()) {
                return false;
            }
        }

        return true;
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String[] defaultHeaders() {
        return new String[]{
                "ID",
                "Username",
                "Password",
                "Role",
                "EmployeeNo"
        };
    }
}