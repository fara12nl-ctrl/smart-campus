package com.smartcampus.dao;

import java.util.List;
import java.util.Optional;

/** OOP: generic repository contract. */
public interface CrudRepository<T> {
    List<T> findAll();
    Optional<T> findById(long id);
    long create(T entity);
    boolean update(long id, T entity);
    boolean delete(long id);
}
