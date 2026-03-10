package repository;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import model.AttendanceLog;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileAttendanceRepository implements AttendanceRepository {

    private static final String ATTENDANCE_FILE = "data/attendance.csv";

    private final List<String> headers = new ArrayList<>();
    private final List<String[]> rows = new ArrayList<>();

    @Override
    public void load() {
        rows.clear();
        headers.clear();

        File file = new File(ATTENDANCE_FILE);
        if (!file.exists()) {
            System.err.println("❌ attendance.csv not found.");
            return;
        }

        try (CSVReader reader = new CSVReaderBuilder(new FileReader(file)).build()) {
            String[] headerRow = reader.readNext();

            if (headerRow == null || headerRow.length == 0) {
                System.err.println("⚠ attendance.csv is empty.");
                headers.addAll(Arrays.asList(defaultHeaders()));
                return;
            }

            for (String h : headerRow) {
                headers.add(clean(h));
            }

            String[] line;
            while ((line = reader.readNext()) != null) {
                if (isBlankRow(line)) {
                    continue;
                }
                rows.add(adjustRowLength(cleanRow(line), headers.size()));
            }

            System.out.println("✅ attendance.csv loaded: " + rows.size() + " rows");

        } catch (IOException | CsvValidationException e) {
            System.err.println("❌ Error reading attendance.csv: " + e.getMessage());
        }
    }

    @Override
    public List<AttendanceLog> findByEmployeeAndDateRange(String employeeNo, LocalDate start, LocalDate end) {
        List<AttendanceLog> result = new ArrayList<>();
        String target = normalizeEmp(employeeNo);

        int empIndex = getEmployeeIdIndex();
        int dateIndex = getDateIndex();
        int timeInIndex = getTimeInIndex();
        int timeOutIndex = getTimeOutIndex();

        if (empIndex == -1 || dateIndex == -1) {
            return result;
        }

        for (String[] row : rows) {
            if (row == null || row.length <= Math.max(empIndex, dateIndex)) {
                continue;
            }

            String rowEmp = normalizeEmp(safe(row[empIndex]));
            if (!rowEmp.equals(target)) {
                continue;
            }

            LocalDate date = parseDateFlexible(safe(row[dateIndex]));
            if (date == null) {
                continue;
            }

            if (date.isBefore(start) || date.isAfter(end)) {
                continue;
            }

            LocalTime timeIn = timeInIndex >= 0 && row.length > timeInIndex
                    ? parseTimeFlexible(row[timeInIndex])
                    : null;

            LocalTime timeOut = timeOutIndex >= 0 && row.length > timeOutIndex
                    ? parseTimeFlexible(row[timeOutIndex])
                    : null;

            result.add(new AttendanceLog(employeeNo.trim(), date, timeIn, timeOut));
        }

        return result;
    }

    @Override
    public boolean updateAttendance(String employeeNo, String date, String[] newRow) {
        int empIndex = getEmployeeIdIndex();
        int dateIndex = getDateIndex();

        if (empIndex == -1 || dateIndex == -1) {
            return false;
        }

        String targetEmp = normalizeEmp(employeeNo);
        String targetDate = normalizeDateString(date);

        for (int i = 0; i < rows.size(); i++) {
            String[] row = rows.get(i);

            if (row.length <= Math.max(empIndex, dateIndex)) {
                continue;
            }

            String rowEmp = normalizeEmp(row[empIndex]);
            String rowDate = normalizeDateString(row[dateIndex]);

            if (rowEmp.equals(targetEmp) && rowDate.equals(targetDate)) {
                rows.set(i, adjustRowLength(cleanRow(newRow), headers.size()));
                return writeAll();
            }
        }

        return false;
    }

    @Override
    public boolean deleteAttendance(String employeeNo, String date) {
        int empIndex = getEmployeeIdIndex();
        int dateIndex = getDateIndex();

        if (empIndex == -1 || dateIndex == -1) {
            return false;
        }

        String targetEmp = normalizeEmp(employeeNo);
        String targetDate = normalizeDateString(date);

        boolean removed = rows.removeIf(row ->
                row.length > Math.max(empIndex, dateIndex)
                        && normalizeEmp(row[empIndex]).equals(targetEmp)
                        && normalizeDateString(row[dateIndex]).equals(targetDate)
        );

        return removed && writeAll();
    }

    private boolean writeAll() {
        ensureHeadersLoaded();

        try (ICSVWriter writer = new CSVWriterBuilder(new FileWriter(ATTENDANCE_FILE))
                .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
                .withQuoteChar(CSVWriter.DEFAULT_QUOTE_CHARACTER)
                .build()) {

            writer.writeNext(headers.toArray(new String[0]));
            for (String[] row : rows) {
                writer.writeNext(adjustRowLength(row, headers.size()));
            }
            return true;

        } catch (IOException e) {
            System.err.println("❌ Error writing attendance.csv: " + e.getMessage());
            return false;
        }
    }

    private void ensureHeadersLoaded() {
        if (!headers.isEmpty()) {
            return;
        }

        File file = new File(ATTENDANCE_FILE);
        if (!file.exists()) {
            headers.addAll(Arrays.asList(defaultHeaders()));
            return;
        }

        try (CSVReader reader = new CSVReaderBuilder(new FileReader(file)).build()) {
            String[] headerRow = reader.readNext();

            if (headerRow != null && headerRow.length > 0) {
                for (String h : headerRow) {
                    headers.add(clean(h));
                }
            } else {
                headers.addAll(Arrays.asList(defaultHeaders()));
            }

        } catch (IOException | CsvValidationException e) {
            headers.addAll(Arrays.asList(defaultHeaders()));
        }
    }

    private int getEmployeeIdIndex() {
        return findFirstMatchingHeader(
                "EmployeeID",
                "Employee #",
                "Employee No",
                "Employee Number",
                "EmpID",
                "Emp No"
        );
    }

    private int getDateIndex() {
        return findFirstMatchingHeader(
                "Date",
                "Attendance Date",
                "Log Date",
                "Work Date"
        );
    }

    private int getTimeInIndex() {
        return findFirstMatchingHeader(
                "Time In",
                "Login",
                "Log In",
                "In",
                "Clock In"
        );
    }

    private int getTimeOutIndex() {
        return findFirstMatchingHeader(
                "Time Out",
                "Logout",
                "Log Out",
                "Out",
                "Clock Out"
        );
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
        if (value == null) {
            return "";
        }
        return value.replaceAll("[^a-zA-Z0-9]", "").trim().toLowerCase();
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private String clean(String s) {
        return s == null ? "" : s.trim();
    }

    private String normalizeEmp(String raw) {
        String s = safe(raw).replace("\"", "");
        s = s.replace("EMP", "").trim();
        return s;
    }

    private String normalizeDateString(String raw) {
        LocalDate parsed = parseDateFlexible(raw);
        return parsed == null ? safe(raw) : parsed.toString();
    }

    private LocalDate parseDateFlexible(String raw) {
        String s = safe(raw);
        if (s.isEmpty()) {
            return null;
        }

        DateTimeFormatter[] formats = new DateTimeFormatter[] {
                DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ofPattern("M/d/yyyy"),
                DateTimeFormatter.ofPattern("MM/dd/yyyy"),
                DateTimeFormatter.ofPattern("d/M/yyyy"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("yyyy/M/d"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd")
        };

        for (DateTimeFormatter f : formats) {
            try {
                return LocalDate.parse(s, f);
            } catch (DateTimeParseException ignored) {
            }
        }

        return null;
    }

    private LocalTime parseTimeFlexible(String raw) {
        if (raw == null) return null;

        String s = raw.trim();
        if (s.isEmpty()) return null;

        // Normalize common bad formats
        s = s.replace(".", ":");
        s = s.replace(" ", ":");

        // Convert HHmm → HH:mm
        if (s.matches("\\d{4}")) {
            s = s.substring(0,2) + ":" + s.substring(2);
        }

        DateTimeFormatter[] fmts = {
            DateTimeFormatter.ofPattern("H:mm"),
            DateTimeFormatter.ofPattern("HH:mm"),
            DateTimeFormatter.ofPattern("H:mm:ss"),
            DateTimeFormatter.ofPattern("HH:mm:ss"),
            DateTimeFormatter.ofPattern("h:mm a"),
            DateTimeFormatter.ofPattern("hh:mm a")
        };

        for (DateTimeFormatter f : fmts) {
            try {
                return LocalTime.parse(s, f);
            } catch (DateTimeParseException ignored) {}
        }

        return null;
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

    private String[] cleanRow(String[] row) {
        String[] cleaned = new String[row.length];
        for (int i = 0; i < row.length; i++) {
            cleaned[i] = clean(row[i]);
        }
        return cleaned;
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

    private String[] defaultHeaders() {
        return new String[] {
                "EmployeeID",
                "Date",
                "TimeIn",
                "TimeOut"
        };
    }
}