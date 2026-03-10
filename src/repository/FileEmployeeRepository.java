package repository;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import model.Employee;
import model.EmployeeFactory;
import model.MotorPhEmployeeFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class FileEmployeeRepository implements EmployeeRepository {

    private static final String FOLDER_PATH = "data";
    private static final String EMPLOYEE_FILE = FOLDER_PATH + "/employee.csv";

    private static final String HEADER_EMPLOYEE_ID_OLD = "Employee #";
    private static final String HEADER_EMPLOYEE_ID_NEW = "EmployeeID";

    private final List<String> headers = new ArrayList<>();
    private final List<String[]> rows = new ArrayList<>();
    private final EmployeeFactory employeeFactory = new MotorPhEmployeeFactory();

    @Override
    public void load() {
        File file = new File(EMPLOYEE_FILE);
        if (!file.exists()) {
            System.out.println("❌ employee.csv not found.");
            headers.clear();
            rows.clear();
            return;
        }

        headers.clear();
        rows.clear();

        try (CSVReader reader = new CSVReaderBuilder(new FileReader(file)).build()) {
            String[] headerRow = reader.readNext();

            if (headerRow == null || headerRow.length == 0) {
                headers.addAll(Arrays.asList(defaultHeaders()));
                System.out.println("⚠ employee.csv is empty. Default headers loaded.");
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

                String[] cleaned = cleanRow(row);
                rows.add(adjustRowLength(cleaned, headers.size()));
            }

            System.out.println("✅ employee.csv loaded: " + rows.size() + " rows");
        } catch (IOException | CsvValidationException e) {
            System.out.println("❌ Error reading employee.csv: " + e.getMessage());
        }
    }

    @Override
    public List<String> getHeaders() {
        return headers;
    }

    @Override
    public List<String[]> getRows() {
        return rows;
    }

    @Override
    public String[] findRowByEmployeeNo(String employeeNo) {
        int idIndex = getEmployeeIdIndex();
        if (idIndex == -1) {
            return null;
        }

        String target = normEmp(employeeNo);

        for (String[] row : rows) {
            if (row.length <= idIndex) {
                continue;
            }

            String rowId = normEmp(row[idIndex]);
            if (rowId.equals(target)) {
                return row;
            }
        }
        return null;
    }

    public Employee findEmployeeByEmployeeNo(String employeeNo) {
        String[] row = findRowByEmployeeNo(employeeNo);
        if (row == null) {
            return null;
        }
        return employeeFactory.fromRow(headers, row);
    }

    public List<Employee> findAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        for (String[] row : rows) {
            employees.add(employeeFactory.fromRow(headers, row));
        }
        return employees;
    }

    @Override
    public Optional<Employee> findById(String employeeNo) {
        return Optional.ofNullable(findEmployeeByEmployeeNo(employeeNo));
    }

    @Override
    public List<Employee> findAll() {
        return findAllEmployees();
    }

    @Override
    public boolean addRow(String[] row) {
        ensureHeadersLoaded();

        String[] normalizedRow = adjustRowLength(row, headers.size());

        try (ICSVWriter writer = new CSVWriterBuilder(new FileWriter(EMPLOYEE_FILE, true))
                .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
                .withQuoteChar(CSVWriter.DEFAULT_QUOTE_CHARACTER)
                .build()) {

            writer.writeNext(normalizedRow);
            rows.add(normalizedRow);
            return true;

        } catch (IOException e) {
            System.out.println("❌ Error appending employee.csv: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean addEmployee(Employee emp) {
        ensureHeadersLoaded();
        return addRow(employeeFactory.toRow(headers, emp));
    }

    @Override
    public boolean updateEmployee(Employee emp) {
        int idIndex = getEmployeeIdIndex();
        if (idIndex == -1) {
            return false;
        }

        for (int i = 0; i < rows.size(); i++) {
            String[] row = rows.get(i);
            if (row.length > idIndex && normEmp(row[idIndex]).equals(normEmp(emp.getEmployeeID()))) {
                rows.set(i, adjustRowLength(employeeFactory.toRow(headers, emp), headers.size()));
                return writeAll();
            }
        }
        return false;
    }

    @Override
    public boolean updateField(String employeeNo, String column, String newValue) {
        int idIndex = getEmployeeIdIndex();
        int colIndex = findColumnIndex(column);

        if (idIndex == -1 || colIndex == -1) {
            return false;
        }

        for (String[] row : rows) {
            if (row.length > idIndex && normEmp(row[idIndex]).equals(normEmp(employeeNo))) {
                row[colIndex] = newValue;
                return writeAll();
            }
        }
        return false;
    }

    @Override
    public boolean deleteByEmployeeNo(String employeeNo) {
        int idIndex = getEmployeeIdIndex();
        if (idIndex == -1) {
            return false;
        }

        boolean removed = rows.removeIf(row ->
                row.length > idIndex && normEmp(row[idIndex]).equals(normEmp(employeeNo)));

        return removed && writeAll();
    }

    private boolean writeAll() {
        ensureHeadersLoaded();

        try (ICSVWriter writer = new CSVWriterBuilder(new FileWriter(EMPLOYEE_FILE))
                .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
                .withQuoteChar(CSVWriter.DEFAULT_QUOTE_CHARACTER)
                .build()) {

            writer.writeNext(headers.toArray(new String[0]));
            for (String[] row : rows) {
                writer.writeNext(adjustRowLength(row, headers.size()));
            }
            return true;

        } catch (IOException e) {
            System.out.println("❌ Error writing employee.csv: " + e.getMessage());
            return false;
        }
    }

    private void ensureHeadersLoaded() {
        if (!headers.isEmpty()) {
            return;
        }

        File file = new File(EMPLOYEE_FILE);
        if (!file.exists()) {
            headers.addAll(Arrays.asList(defaultHeaders()));
            return;
        }

        try (CSVReader reader = new CSVReaderBuilder(new FileReader(file)).build()) {
            String[] headerRow = reader.readNext();

            if (headerRow != null && headerRow.length > 0) {
                for (String header : headerRow) {
                    headers.add(clean(header));
                }
            } else {
                headers.addAll(Arrays.asList(defaultHeaders()));
            }
        } catch (IOException | CsvValidationException e) {
            headers.addAll(Arrays.asList(defaultHeaders()));
        }
    }

    private int getEmployeeIdIndex() {
        int index = findColumnIndex(HEADER_EMPLOYEE_ID_NEW);
        if (index != -1) {
            return index;
        }
        return findColumnIndex(HEADER_EMPLOYEE_ID_OLD);
    }

    private int findColumnIndex(String columnName) {
        if (columnName == null || headers.isEmpty()) {
            return -1;
        }

        for (int i = 0; i < headers.size(); i++) {
            if (normalizeHeader(headers.get(i)).equals(normalizeHeader(columnName))) {
                return i;
            }
        }
        return -1;
    }

    private String normalizeHeader(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("[^a-zA-Z0-9]", "").trim().toLowerCase();
    }

    private String normEmp(String value) {
        if (value == null) {
            return "";
        }

        String normalized = value.trim().replace("\"", "");
        normalized = normalized.replace("EMP", "").trim();
        return normalized;
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
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

    private String[] defaultHeaders() {
        return new String[]{
                "EmployeeID",
                "LastName",
                "FirstName",
                "Birthday",
                "Address",
                "PhoneNumber",
                "SSS",
                "Philhealth",
                "TIN",
                "PagIbig",
                "Status",
                "Position",
                "ImmediateSupervisor",
                "BasicSalary",
                "RiceSubsidy",
                "PhoneAllowance",
                "ClothingAllowance",
                "GrossSemiMonthlyRate",
                "HourlyRate"
        };
    }

    private String[] adjustRowLength(String[] fields, int expected) {
        if (fields == null) {
            return new String[expected];
        }

        if (fields.length < expected) {
            String[] padded = Arrays.copyOf(fields, expected);
            Arrays.fill(padded, fields.length, expected, "");
            return padded;
        }

        if (fields.length > expected) {
            return Arrays.copyOf(fields, expected);
        }

        return fields;
    }
}