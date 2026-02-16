/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
/**
 *
 * @author ca
 */
public class MotorPhAttendanceFactory implements AttendanceFactory {

    @Override
    public AttendanceLog fromRow(List<String> headers, String[] row) {
        // Header-aware if provided; fallback to fixed positions
        String empId = get(headers, row, "Employee #", 0);
        LocalDate date = LocalDate.parse(get(headers, row, "Date", 1));
        LocalTime timeIn = parseTime(get(headers, row, "Time In", 2));
        LocalTime timeOut = parseTime(get(headers, row, "Time Out", 3));

        return new AttendanceLog(empId, date, timeIn, timeOut);
    }

    @Override
    public String[] toRow(List<String> headers, AttendanceLog log, String[] existingRowForExtraColumns) {
        // Preserve extra columns by cloning existing row; otherwise create minimal row
        String[] row;
        if (existingRowForExtraColumns != null && existingRowForExtraColumns.length > 0) {
            row = Arrays.copyOf(existingRowForExtraColumns, existingRowForExtraColumns.length);
        } else {
            row = new String[Math.max(headers.size(), 4)];
        }

        set(headers, row, "Employee #", 0, log.getEmployeeId());
        set(headers, row, "Date", 1, log.getDate().toString());
        set(headers, row, "Time In", 2, log.getTimeIn().toString());
        set(headers, row, "Time Out", 3, log.getTimeOut().toString());

        return row;
    }

    // ---------- helpers ----------
    private String get(List<String> headers, String[] row, String colName, int fallbackIndex) {
        int idx = (headers != null && !headers.isEmpty()) ? headers.indexOf(colName) : -1;
        if (idx == -1) idx = fallbackIndex;
        return (idx >= 0 && idx < row.length && row[idx] != null) ? row[idx].trim() : "";
    }

    private void set(List<String> headers, String[] row, String colName, int fallbackIndex, String value) {
        int idx = (headers != null && !headers.isEmpty()) ? headers.indexOf(colName) : -1;
        if (idx == -1) idx = fallbackIndex;
        if (idx >= 0 && idx < row.length) row[idx] = value == null ? "" : value.trim();
    }

    private LocalTime parseTime(String s) {
        if (s == null || s.isBlank()) return LocalTime.MIDNIGHT;
        // Supports HH:mm or HH:mm:ss
        return LocalTime.parse(s.trim());
    }
}
