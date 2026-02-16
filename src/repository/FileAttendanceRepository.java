/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package repository;
import com.opencsv.*;
import com.opencsv.exceptions.CsvValidationException;
import model.AttendanceLog;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
/**
 *
 * @author ca
 */
public class FileAttendanceRepository implements AttendanceRepository {

    private final String ATTENDANCE_FILE = "data/attendance.txt";
    private final List<String[]> rows = new ArrayList<>();

    @Override
    public void load() {
        rows.clear();
        File file = new File(ATTENDANCE_FILE);
        if (!file.exists()) return;

        try (CSVReader reader = new CSVReaderBuilder(new FileReader(file))
                .withCSVParser(new CSVParserBuilder().withSeparator(',').build())
                .build()) {

            String[] line;
            while ((line = reader.readNext()) != null) {
                if (line.length == 0 || line[0].trim().isEmpty()) continue;
                rows.add(line);
            }

        } catch (IOException | CsvValidationException e) {
            System.err.println("Error reading attendance: " + e.getMessage());
        }
    }

    @Override
public List<AttendanceLog> findByEmployeeAndDateRange(String employeeNo, LocalDate start, LocalDate end) {
    List<AttendanceLog> result = new ArrayList<>();
    String target = normalizeEmp(employeeNo);

    for (String[] r : rows) {
        if (r == null || r.length < 2) continue;

        String rowEmp = normalizeEmp(r[0]);
        if (!rowEmp.equals(target)) continue;

        LocalDate d = parseDateFlexible(safe(r[1]));
        if (d == null) continue;

        if (d.isBefore(start) || d.isAfter(end)) continue;

        LocalTime in  = parseTimeFlexible(r.length > 2 ? r[2] : null);
        LocalTime out = parseTimeFlexible(r.length > 3 ? r[3] : null);

        result.add(new AttendanceLog(employeeNo.trim(), d, in, out));
    }

    return result;
        }

        private String safe(String s) {
            return s == null ? "" : s.trim();
        }

        /**
         * Normalizes IDs so "001", " 001 ", "EMP001" can be matched consistently if needed.
         * Adjust this to match how your system stores employee IDs.
         */
        private String normalizeEmp(String raw) {
            String s = safe(raw);
            // If you DO NOT want EMP prefix normalization, remove the next line.
            s = s.replace("EMP", "").trim();
            return s;
        }

        private LocalDate parseDateFlexible(String raw) {
            String s = safe(raw);
            if (s.isEmpty()) return null;

            // Try common formats (add/remove based on your actual attendance.txt format)
            DateTimeFormatter[] fmts = new DateTimeFormatter[] {
                    DateTimeFormatter.ISO_LOCAL_DATE,                 // 2025-01-15
                    DateTimeFormatter.ofPattern("M/d/yyyy"),          // 1/15/2025
                    DateTimeFormatter.ofPattern("MM/dd/yyyy"),        // 01/15/2025
                    DateTimeFormatter.ofPattern("d/M/yyyy"),          // 15/1/2025
                    DateTimeFormatter.ofPattern("dd/MM/yyyy"),        // 15/01/2025
                    DateTimeFormatter.ofPattern("yyyy/M/d"),          // 2025/1/15
                    DateTimeFormatter.ofPattern("yyyy/MM/dd")         // 2025/01/15
            };

            for (DateTimeFormatter f : fmts) {
                try {
                    return LocalDate.parse(s, f);
                } catch (DateTimeParseException ignored) {}
            }

            return null;
        }

        private LocalTime parseTimeFlexible(String raw) {
            if (raw == null) return null;
            String s = raw.trim();
            if (s.isEmpty()) return null;

                DateTimeFormatter[] fmts = {
                DateTimeFormatter.ofPattern("H:mm"),     // 8:59 ✅
                DateTimeFormatter.ofPattern("HH:mm"),    // 08:59
                DateTimeFormatter.ofPattern("H:mm:ss"),
                DateTimeFormatter.ofPattern("HH:mm:ss"),
                DateTimeFormatter.ofPattern("h:mm a"),
                DateTimeFormatter.ofPattern("hh:mm a"),
            };

            for (DateTimeFormatter f : fmts) {
                try { return LocalTime.parse(s, f); }
                catch (DateTimeParseException ignored) {}
            }
            return null;
        }

    @Override
    public boolean updateAttendance(String employeeNo, String date, String[] newRow) {
        for (int i = 0; i < rows.size(); i++) {
            String[] r = rows.get(i);
            if (r.length >= 2 && r[0].equals(employeeNo) && r[1].equals(date)) {
                rows.set(i, newRow);
                return writeAll();
            }
        }
        return false;
    }

    @Override
    public boolean deleteAttendance(String employeeNo, String date) {
        boolean removed = rows.removeIf(r -> r.length >= 2 && r[0].equals(employeeNo) && r[1].equals(date));
        return removed && writeAll();
    }

    private boolean writeAll() {
        try (ICSVWriter writer = new CSVWriterBuilder(new FileWriter(ATTENDANCE_FILE))
                .withSeparator(',')
                .withQuoteChar(CSVWriter.NO_QUOTE_CHARACTER)
                .build()) {

            for (String[] r : rows) writer.writeNext(r);
            return true;

        } catch (IOException e) {
            System.err.println("Error writing attendance: " + e.getMessage());
            return false;
        }
    }
}