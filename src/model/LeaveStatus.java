/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author ca
 */
public enum LeaveStatus {
    PENDING,
    APPROVED,
    DENIED;

    public static LeaveStatus from(String s) {
        if (s == null) return PENDING;
        try {
            return LeaveStatus.valueOf(s.trim().toUpperCase());
        } catch (Exception e) {
            return PENDING;
        }
    }
}
