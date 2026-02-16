/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author ca
 */
public enum Role {
    EMPLOYEE,
    HRADMIN,
    IT;

    public static Role from(String s) {
        if (s == null) return EMPLOYEE;
        try {
            return Role.valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return EMPLOYEE;
        }
    }
}