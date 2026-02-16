/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package repository;

import com.opencsv.*;
import model.*;

import java.io.*;
import java.util.*;
/**
 *
 * @author ca
 */
public class FileEmployeeRepository implements EmployeeRepository {

    private final String folderPath = "data";
    private final String EMPLOYEE_FILE = folderPath + "/employee.txt";

    private final List<String> headers = new ArrayList<>();
    private final List<String[]> rows = new ArrayList<>();

    private final EmployeeFactory employeeFactory = new MotorPhEmployeeFactory();

    @Override
    public void load() {
        File file = new File(EMPLOYEE_FILE);
        if (!file.exists()) {
            System.out.println("❌ employee.txt not found.");
            return;
        }

        headers.clear();
        rows.clear();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean first = true;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] fields = line.split("(?=(?:[^\"]*\"[^\"]*\")*[^\"]*)\\;");
                for (int i = 0; i < fields.length; i++) {
                    fields[i] = fields[i].replaceAll("^\"|\"$", "").trim();
                }

                if (first) {
                    headers.addAll(Arrays.asList(fields));
                    first = false;
                } else {
                    rows.add(adjustRowLength(fields, headers.size()));
                }
            }

            System.out.println("✅ employee.txt loaded: " + rows.size() + " rows");
        } catch (IOException e) {
            System.out.println("❌ Error reading employee file: " + e.getMessage());
        }
    }

    @Override
    public List<String> getHeaders() { return headers; }

    @Override
    public List<String[]> getRows() { return rows; }

    @Override
    public String[] findRowByEmployeeNo(String employeeNo) {
        int idIndex = headers.indexOf("Employee #");
        if (idIndex == -1) return null;

        String target = normEmp(employeeNo);

        for (String[] r : rows) {
            if (r.length <= idIndex) continue;

            String rowId = normEmp(r[idIndex]);
            if (rowId.equals(target)) return r;
        }
        return null;
    }
    
     private String normEmp(String s) {
        if (s == null) return "";
        // remove surrounding quotes + trim
        String x = s.replaceAll("^\"|\"$", "").trim();

        // OPTIONAL: if your employee file stores "EMP10001"
        // and other parts use "10001", normalize by removing EMP.
        // If your employee file is purely numeric, this line won't hurt.
        x = x.replace("EMP", "").trim();

        return x;
    }


    public Employee findEmployeeByEmployeeNo(String employeeNo) {
        String[] r = findRowByEmployeeNo(employeeNo);
        if (r == null) return null;
        return employeeFactory.fromRow(headers, r);
    }

    public List<Employee> findAllEmployees() {
        List<Employee> list = new ArrayList<>();
        for (String[] r : rows) list.add(employeeFactory.fromRow(headers, r));
        return list;
    }
    
    @Override
    public Optional<Employee> findById(String employeeNo) {
        Employee emp = findEmployeeByEmployeeNo(employeeNo); // reuse existing logic
        return Optional.ofNullable(emp);
    }

    @Override
    public List<Employee> findAll() {
        return findAllEmployees();
    }    

    @Override
    public boolean addRow(String[] row) {
        ensureHeadersLoaded();
        try (ICSVWriter writer = new CSVWriterBuilder(new FileWriter(EMPLOYEE_FILE, true))
                .withSeparator(';')
                .withQuoteChar(CSVWriter.NO_QUOTE_CHARACTER)
                .build()) {

            writer.writeNext(row);
            rows.add(row);
            return true;
        } catch (IOException e) {
            System.out.println("❌ Error appending employee: " + e.getMessage());
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
        int idIndex = headers.indexOf("Employee #");
        if (idIndex == -1) return false;

        for (int i = 0; i < rows.size(); i++) {
            String[] r = rows.get(i);
            if (r.length > idIndex && normEmp(r[idIndex]).equals(normEmp(emp.getEmployeeID()))) {
                rows.set(i, employeeFactory.toRow(headers, emp));
                return writeAll();
            }
        }
        return false;
    }

    @Override
    public boolean updateField(String employeeNo, String column, String newValue) {
        int idIndex = headers.indexOf("Employee #");
        int colIndex = headers.indexOf(column);
        if (idIndex == -1 || colIndex == -1) return false;

        for (String[] r : rows) {
            if (r.length > idIndex && normEmp(r[idIndex]).equals(normEmp(employeeNo))) {
                r[colIndex] = newValue;
                return writeAll();
            }
        }
        return false;
    }

    @Override
    public boolean deleteByEmployeeNo(String employeeNo) {
        int idIndex = headers.indexOf("Employee #");
        if (idIndex == -1) return false;

        boolean removed = rows.removeIf(r ->
    r.length > idIndex && normEmp(r[idIndex]).equals(normEmp(employeeNo))
    );
        if (!removed) return false;
        return writeAll();
    }

    private boolean writeAll() {
        ensureHeadersLoaded();

        try (ICSVWriter writer = new CSVWriterBuilder(new FileWriter(EMPLOYEE_FILE))
                .withSeparator(';')
                .withQuoteChar(CSVWriter.NO_QUOTE_CHARACTER)
                .build()) {

            writer.writeNext(headers.toArray(String[]::new));
            for (String[] r : rows) writer.writeNext(r);
            return true;

        } catch (IOException e) {
            System.out.println("❌ Error writing employee.txt: " + e.getMessage());
            return false;
        }
    }

    private void ensureHeadersLoaded() {
        if (!headers.isEmpty()) return;

        File file = new File(EMPLOYEE_FILE);
        if (!file.exists()) {
            headers.addAll(Arrays.asList(defaultHeaders()));
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String first = br.readLine();
            if (first != null && !first.trim().isEmpty()) {
                String[] h = first.split("(?=(?:[^\"]*\"[^\"]*\")*[^\"]*)\\;");
                for (int i = 0; i < h.length; i++) h[i] = h[i].replaceAll("^\"|\"$", "").trim();
                headers.addAll(Arrays.asList(h));
            } else {
                headers.addAll(Arrays.asList(defaultHeaders()));
            }
        } catch (IOException e) {
            headers.addAll(Arrays.asList(defaultHeaders()));
        }
    }

    private String[] defaultHeaders() {
        return new String[]{
                "Employee #", "Last Name", "First Name", "Birthday", "Address", "Phone Number",
                "SSS #", "Philhealth #", "TIN #", "Pag-ibig #", "Status", "Position",
                "Immediate Supervisor", "Basic Salary", "Rice Subsidy", "Phone Allowance",
                "Clothing Allowance", "Gross Semi-monthly Rate", "Hourly Rate"
        };
    }
    
    private String[] adjustRowLength(String[] fields, int expected) {
        if (fields.length < expected) {
            String[] padded = Arrays.copyOf(fields, expected);
            Arrays.fill(padded, fields.length, expected, "");
            return padded;
        }
        if (fields.length > expected) return Arrays.copyOf(fields, expected);
        return fields;
    }
}