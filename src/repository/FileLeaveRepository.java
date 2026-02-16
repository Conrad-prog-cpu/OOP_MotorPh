/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package repository;
import model.LeaveRequest;
import model.LeaveStatus;
import model.LeaveType;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
/**
 *
 * @author ca
 */
public class FileLeaveRepository implements LeaveRepository {

    private final String folderPath = "data";
    private final String FILE = folderPath + "/leaves.csv";

    private final List<String> headers = new ArrayList<>();
    private final List<String[]> rows = new ArrayList<>();

    public FileLeaveRepository() {
        ensureFileExists();
        readAll();
    }

    @Override
    public LeaveRequest create(LeaveRequest request) {
        rows.add(toRow(request));
        writeAll();
        return request;
    }

    @Override
    public Optional<LeaveRequest> findById(String leaveId) {
        int idx = headers.indexOf("LeaveId");
        if (idx < 0) return Optional.empty();
        for (String[] r : rows) {
            if (r.length > idx && r[idx].equals(leaveId)) return Optional.of(fromRow(r));
        }
        return Optional.empty();
    }

    @Override
    public List<LeaveRequest> findAll() {
        return rows.stream().map(this::fromRow).collect(Collectors.toList());
    }

    @Override
    public List<LeaveRequest> findByEmployeeId(String employeeId) {
        return findAll().stream()
                .filter(l -> l.getEmployeeId().equals(employeeId))
                .collect(Collectors.toList());
    }

    @Override
    public List<LeaveRequest> findByStatus(LeaveStatus status) {
        return findAll().stream()
                .filter(l -> l.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public LeaveRequest update(LeaveRequest request) {
        int idx = headers.indexOf("LeaveId");
        if (idx < 0) throw new IllegalStateException("Missing LeaveId header");

        for (int i = 0; i < rows.size(); i++) {
            String[] r = rows.get(i);
            if (r.length > idx && r[idx].equals(request.getLeaveId())) {
                rows.set(i, toRow(request));
                writeAll();
                return request;
            }
        }
        throw new IllegalStateException("Leave not found: " + request.getLeaveId());
    }

    @Override
    public boolean deleteById(String leaveId) {
        int idx = headers.indexOf("LeaveId");
        if (idx < 0) return false;

        boolean removed = rows.removeIf(r -> r.length > idx && r[idx].equals(leaveId));
        if (removed) writeAll();
        return removed;
    }

    // ---------------- File Ops ----------------

    private void ensureFileExists() {
        File folder = new File(folderPath);
        if (!folder.exists()) folder.mkdirs();

        File f = new File(FILE);
        if (!f.exists()) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE))) {
                bw.write(String.join(",", defaultHeaders()));
                bw.newLine();
            } catch (IOException e) {
                throw new RuntimeException("Failed to create leaves.csv: " + e.getMessage(), e);
            }
        }
    }

    private void readAll() {
        headers.clear();
        rows.clear();

        try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = splitCsv(line);

                if (first) {
                    headers.addAll(Arrays.asList(parts));
                    first = false;
                } else {
                    rows.add(parts);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read leaves.csv: " + e.getMessage(), e);
        }

        if (headers.isEmpty()) headers.addAll(Arrays.asList(defaultHeaders()));
    }

    private void writeAll() {
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE))) {

        // ✅ Fix: avoid mixing String[] and List<String> in ternary
        if (headers == null || headers.isEmpty()) {
            bw.write(String.join(",", defaultHeaders()));
        } else {
            bw.write(String.join(",", headers));
        }
        bw.newLine();

        for (String[] r : rows) {
            bw.write(joinCsv(r));
            bw.newLine();
        }

    } catch (IOException e) {
        throw new RuntimeException("Failed to write leaves.csv: " + e.getMessage(), e);
    }
}
    private String[] defaultHeaders() {
        return new String[]{
                "LeaveId","EmployeeId","EmployeeName","LeaveType",
                "StartDate","EndDate","Reason","Status",
                "RequestedAt","ReviewedBy","ReviewedAt"
        };
    }

    // ---------------- Mapping ----------------

    private LeaveRequest fromRow(String[] r) {
        return new LeaveRequest(
                get(r, "LeaveId"),
                get(r, "EmployeeId"),
                get(r, "EmployeeName"),
                LeaveType.from(get(r, "LeaveType")),
                LocalDate.parse(get(r, "StartDate")),
                LocalDate.parse(get(r, "EndDate")),
                get(r, "Reason"),
                LeaveStatus.from(get(r, "Status")),
                parseDateTime(get(r, "RequestedAt")),
                blankToNull(get(r, "ReviewedBy")),
                parseDateTimeNullable(get(r, "ReviewedAt"))
        );
    }

    private String[] toRow(LeaveRequest l) {
        String[] r = new String[headers.size()];
        set(r, "LeaveId", l.getLeaveId());
        set(r, "EmployeeId", l.getEmployeeId());
        set(r, "EmployeeName", l.getEmployeeName());
        set(r, "LeaveType", l.getLeaveType().name());
        set(r, "StartDate", l.getStartDate().toString());
        set(r, "EndDate", l.getEndDate().toString());
        set(r, "Reason", l.getReason());
        set(r, "Status", l.getStatus().name());
        set(r, "RequestedAt", l.getRequestedAt().toString());
        set(r, "ReviewedBy", l.getReviewedBy() == null ? "" : l.getReviewedBy());
        set(r, "ReviewedAt", l.getReviewedAt() == null ? "" : l.getReviewedAt().toString());
        return r;
    }

    private String get(String[] row, String col) {
        int idx = headers.indexOf(col);
        if (idx < 0 || idx >= row.length) return "";
        return row[idx] == null ? "" : row[idx].trim();
    }

    private void set(String[] row, String col, String val) {
        int idx = headers.indexOf(col);
        if (idx < 0 || idx >= row.length) return;
        row[idx] = escape(val == null ? "" : val.trim());
    }

    // ---------------- CSV helpers (safe enough for commas) ----------------

    private String escape(String s) {
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            s = s.replace("\"", "\"\"");
            return "\"" + s + "\"";
        }
        return s;
    }

    private String joinCsv(String[] parts) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(parts[i] == null ? "" : parts[i]);
        }
        return sb.toString();
    }

    private String[] splitCsv(String line) {
        // Minimal CSV parser supporting quotes
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    cur.append('"'); // escaped quote
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                out.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        out.add(cur.toString());
        return out.toArray(new String[0]);
    }

    private LocalDateTime parseDateTime(String s) {
        if (s == null || s.isBlank()) return LocalDateTime.now();
        return LocalDateTime.parse(unquote(s.trim()));
    }

    private LocalDateTime parseDateTimeNullable(String s) {
        if (s == null || s.isBlank()) return null;
        return LocalDateTime.parse(unquote(s.trim()));
    }

    private String unquote(String s) {
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            s = s.substring(1, s.length() - 1).replace("\"\"", "\"");
        }
        return s;
    }

    private String blankToNull(String s) {
        if (s == null) return null;
        s = unquote(s.trim());
        return s.isBlank() ? null : s;
    }
}
