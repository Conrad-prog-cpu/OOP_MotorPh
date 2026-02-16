/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package model;

import java.util.List;
import java.util.Optional;
/**
 *
 * @author ca
 * @param <T>
 * @param <ID>
 */
public interface CrudRepository<T, ID> {
    T create(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
    T update(T entity);
    boolean deleteById(ID id);
}
