/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;
import service.PayslipRepository;
import com.opencsv.*;
import com.opencsv.exceptions.CsvValidationException;


import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;
/**
 *
 * @author ca
 */
public class FilePayslipRepository implements PayslipRepository {

    private final String folderPath = "data";
    private final String PAYSLIP_FILE = folderPath + "/payslips.csv";

    private final List<String> headers = new ArrayList<>();
    private final List<String[]> rows = new ArrayList<>();

    public FilePayslipRepository() {
        ensureFileExists();
        readFile();
    }

    // ---------------- CRUD ----------------

    @Override
    public Payslip create(Payslip entity) {
        ensureHeadersLoaded();

        String[] row = toRow(entity);
        rows.add(row);
        writeFile();
        return entity;
    }

    @Override
    public Optional<Payslip> findById(String payslipId) {
        int idIndex = headers.indexOf("PayslipId");
        if (idIndex == -1) return Optional.empty();

        for (String[] row : rows) {
            if (row.length > idIndex && row[idIndex].equals(payslipId)) {
                return Optional.of(fromRow(row));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Payslip> findAll() {
        return rows.stream().map(this::fromRow).collect(Collectors.toList());
    }

    @Override
    public Payslip update(Payslip entity) {
        int idIndex = headers.indexOf("PayslipId");
        if (idIndex == -1) throw new IllegalStateException("PayslipId header missing.");

        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i).length > idIndex && rows.get(i)[idIndex].equals(entity.getPayslipId())) {
                rows.set(i, toRow(entity));
                writeFile();
                return entity;
            }
        }
        throw new IllegalStateException("Payslip not found: " + entity.getPayslipId());
    }

    @Override
    public boolean deleteById(String payslipId) {
        int idIndex = headers.indexOf("PayslipId");
        if (idIndex == -1) return false;

        boolean removed = rows.removeIf(r -> r.length > idIndex && r[idIndex].equals(payslipId));
        if (removed) writeFile();
        return removed;
    }

    // ---------------- Queries ----------------

    @Override
    public List<Payslip> findByEmployeeAndMonth(String employeeId, YearMonth month) {
        return findAll().stream()
                .filter(p -> p.getEmployeeId().equals(employeeId))
                .filter(p -> p.getYearMonth().equals(month))
                .collect(Collectors.toList());
    }

    @Override
    public List<Payslip> findByMonth(YearMonth month) {
        return findAll().stream()
                .filter(p -> p.getYearMonth().equals(month))
                .collect(Collectors.toList());
    }

    // ---------------- File IO ----------------

    private void ensureFileExists() {
        File folder = new File(folderPath);
        if (!folder.exists()) folder.mkdirs();

        File f = new File(PAYSLIP_FILE);
        if (!f.exists()) {
            // create with header
            headers.clear();
            headers.addAll(Arrays.asList(defaultHeaders()));
            try (ICSVWriter writer = new CSVWriterBuilder(new FileWriter(PAYSLIP_FILE))
                    .withSeparator(',')
                    .withQuoteChar(CSVWriter.NO_QUOTE_CHARACTER)
                    .build()) {

                writer.writeNext(headers.toArray(String[]::new));

            } catch (IOException e) {
                throw new RuntimeException("Failed to create payslips.csv: " + e.getMessage(), e);
            }
        }
    }

    private void readFile() {
        headers.clear();
        rows.clear();

        try (CSVReader reader = new CSVReaderBuilder(new FileReader(PAYSLIP_FILE))
                .withCSVParser(new CSVParserBuilder().withSeparator(',').build())
                .build()) {

            String[] line;
            boolean first = true;
            while ((line = reader.readNext()) != null) {
                if (line.length == 0) continue;

                if (first) {
                    headers.addAll(Arrays.asList(line));
                    first = false;
                    continue;
                }
                if (line[0].trim().isEmpty()) continue;
                rows.add(line);
            }

        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException("Failed to read payslips.csv: " + e.getMessage(), e);
        }
    }

    private void writeFile() {
        ensureHeadersLoaded();

        try (ICSVWriter writer = new CSVWriterBuilder(new FileWriter(PAYSLIP_FILE))
                .withSeparator(',')
                .withQuoteChar(CSVWriter.NO_QUOTE_CHARACTER)
                .build()) {

            writer.writeNext(headers.toArray(String[]::new));
            for (String[] r : rows) writer.writeNext(r);

        } catch (IOException e) {
            throw new RuntimeException("Failed to write payslips.csv: " + e.getMessage(), e);
        }
    }

    private void ensureHeadersLoaded() {
        if (headers.isEmpty()) headers.addAll(Arrays.asList(defaultHeaders()));
    }

    private String[] defaultHeaders() {
        return new String[]{
                "PayslipId",
                "EmployeeId",
                "EmployeeName",
                "YearMonth",
                "PeriodStart",
                "PeriodEnd",
                "HoursWorked",
                "GrossPay",
                "SSS",
                "PhilHealth",
                "PagIbig",
                "WithholdingTax",
                "NetPay"
        };
    }

    // ---------------- Mapping ----------------

    private Payslip fromRow(String[] r) {
        // Use header-based index lookup so your file can reorder columns safely.
        return new Payslip(
                get(r, "PayslipId"),
                get(r, "EmployeeId"),
                get(r, "EmployeeName"),
                YearMonth.parse(get(r, "YearMonth")),
                LocalDate.parse(get(r, "PeriodStart")),
                LocalDate.parse(get(r, "PeriodEnd")),
                money(get(r, "HoursWorked")),
                money(get(r, "GrossPay")),
                money(get(r, "SSS")),
                money(get(r, "PhilHealth")),
                money(get(r, "PagIbig")),
                money(get(r, "WithholdingTax")),
                money(get(r, "NetPay"))
        );
    }

    private String[] toRow(Payslip p) {
        String[] r = new String[headers.size()];
        set(r, "PayslipId", p.getPayslipId());
        set(r, "EmployeeId", p.getEmployeeId());
        set(r, "EmployeeName", p.getEmployeeName());
        set(r, "YearMonth", p.getYearMonth().toString());
        set(r, "PeriodStart", p.getPeriodStart().toString());
        set(r, "PeriodEnd", p.getPeriodEnd().toString());
        set(r, "HoursWorked", safeMoney(p.getHoursWorked()));
        set(r, "GrossPay", safeMoney(p.getGrossPay()));
        set(r, "SSS", safeMoney(p.getSss()));
        set(r, "PhilHealth", safeMoney(p.getPhilHealth()));
        set(r, "PagIbig", safeMoney(p.getPagIbig()));
        set(r, "WithholdingTax", safeMoney(p.getWithholdingTax()));
        set(r, "NetPay", safeMoney(p.getNetPay()));
        return r;
    }

    private String get(String[] row, String col) {
        int idx = headers.indexOf(col);
        if (idx < 0 || idx >= row.length) return "";
        return row[idx] == null ? "" : row[idx].trim();
    }

    private void set(String[] row, String col, String val) {
        int idx = headers.indexOf(col);
        if (idx >= 0 && idx < row.length) row[idx] = val == null ? "" : val.trim();
    }

    private BigDecimal money(String s) {
        if (s == null || s.isBlank()) return BigDecimal.ZERO;
        String clean = s.replace("\"", "").replace(",", "").trim();
        try { return new BigDecimal(clean); }
        catch (Exception ex) { return BigDecimal.ZERO; }
    }

    private String safeMoney(BigDecimal bd) {
        return bd == null ? "0" : bd.toPlainString();
    }
}