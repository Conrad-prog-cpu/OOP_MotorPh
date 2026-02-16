/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package model;
import java.util.List;
/**
 *
 * @author ca
 */
public interface EmployeeFactory {
    Employee fromRow(List<String> headers, String[] row);
    String[] toRow(List<String> headers, Employee employee);
}
