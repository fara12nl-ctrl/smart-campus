package com.smartcampus.dao;

import com.smartcampus.model.Faculty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class FacultyRepository extends AbstractJdbcRepository<Faculty> {

    public FacultyRepository(JdbcTemplate jdbc) { super(jdbc); }

    @Override
    protected String table() { return "faculty"; }

    @Override
    protected String selectSql() { return "SELECT t.* FROM faculty t"; }

    @Override
    protected RowMapper<Faculty> mapper() {
        return (rs, i) -> new Faculty(
                rs.getLong("id"), rs.getString("emp_id"), rs.getString("name"),
                rs.getString("email"), rs.getString("phone"),
                rs.getString("department"), rs.getString("designation"));
    }

    @Override
    protected String insertSql() {
        return "INSERT INTO faculty (emp_id, name, email, phone, department, designation) VALUES (?,?,?,?,?,?)";
    }

    @Override
    protected Object[] insertParams(Faculty f) {
        return new Object[]{f.getEmpId(), f.getName(), f.getEmail(), f.getPhone(), f.getDepartment(), f.getDesignation()};
    }

    @Override
    protected String updateSql() {
        return "UPDATE faculty SET emp_id=?, name=?, email=?, phone=?, department=?, designation=? WHERE id=?";
    }

    @Override
    protected Object[] updateParams(long id, Faculty f) {
        return new Object[]{f.getEmpId(), f.getName(), f.getEmail(), f.getPhone(), f.getDepartment(), f.getDesignation(), id};
    }
}
