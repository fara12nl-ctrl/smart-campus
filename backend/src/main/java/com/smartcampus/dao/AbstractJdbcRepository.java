package com.smartcampus.dao;

import com.smartcampus.exception.NotFoundException;
import com.smartcampus.model.Identifiable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

/**
 * OOP: generics + abstract class - all JDBC CRUD logic lives here once and is
 * reused by every concrete repository. Uses PreparedStatement (JDBC) under the hood.
 */
public abstract class AbstractJdbcRepository<T extends Identifiable> implements CrudRepository<T> {

    protected final JdbcTemplate jdbc;

    protected AbstractJdbcRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    protected abstract String table();

    /** SELECT ... FROM table (may include JOINs). */
    protected abstract String selectSql();

    protected abstract RowMapper<T> mapper();

    /** INSERT statement params in order; returns SQL with placeholders. */
    protected abstract String insertSql();

    protected abstract Object[] insertParams(T entity);

    protected abstract String updateSql();

    protected abstract Object[] updateParams(long id, T entity);

    @Override
    public List<T> findAll() {
        return jdbc.query(selectSql(), mapper());
    }

    @Override
    public Optional<T> findById(long id) {
        List<T> rows = jdbc.query(selectSql() + " WHERE t.id = ?", mapper(), id);
        return rows.stream().findFirst();
    }

    public T getOrThrow(long id) {
        return findById(id).orElseThrow(() -> new NotFoundException(table(), id));
    }

    @Override
    public long create(T entity) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(insertSql(), Statement.RETURN_GENERATED_KEYS);
            Object[] params = insertParams(entity);
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            return ps;
        }, keyHolder);
        long id = keyHolder.getKey() == null ? -1 : keyHolder.getKey().longValue();
        entity.setId(id);
        return id;
    }

    @Override
    public boolean update(long id, T entity) {
        getOrThrow(id);
        Object[] params = updateParams(id, entity);
        return jdbc.update(updateSql(), params) > 0;
    }

    @Override
    public boolean delete(long id) {
        getOrThrow(id);
        return jdbc.update("DELETE FROM " + table() + " WHERE id = ?", id) > 0;
    }
}
