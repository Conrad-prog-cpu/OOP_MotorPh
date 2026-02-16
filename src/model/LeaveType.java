/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author ca
 */
public enum LeaveType {
    SICK,
    VACATION,
    EMERGENCY,
    UNPAID,
    OTHER;

    public static LeaveType from(String s) {
        if (s == null) return OTHER;
        try {
            return LeaveType.valueOf(s.trim().toUpperCase());
        } catch (Exception e) {
            return OTHER;
        }
    }
}
