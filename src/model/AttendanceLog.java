/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;
import java.time.LocalDate;
import java.time.LocalTime;
/**
 *
 * @author ca
 */
public class AttendanceLog {
    private final String employeeId;
    private final LocalDate date;
    private final LocalTime timeIn;
    private final LocalTime timeOut;

    public AttendanceLog(String employeeId, LocalDate date, LocalTime timeIn, LocalTime timeOut) {
        this.employeeId = employeeId;
        this.date = date;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
    }

    public String getEmployeeId() { return employeeId; }
    public LocalDate getDate() { return date; }
    public LocalTime getTimeIn() { return timeIn; }
    public LocalTime getTimeOut() { return timeOut; }
}