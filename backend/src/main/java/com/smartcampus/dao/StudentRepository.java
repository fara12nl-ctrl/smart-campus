package com.smartcampus.dao;

import com.smartcampus.model.Student;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class StudentRepository extends AbstractJdbcRepository<Student> {

    public StudentRepository(JdbcTemplate jdbc) { super(jdbc); }

    @Override
    protected String table() { return "students"; }

    @Override
    protected String selectSql() { return "SELECT t.* FROM students t"; }

    @Override
    protected RowMapper<Student> mapper() {
        return (rs, i) -> new Student(
                rs.getLong("id"), rs.getString("roll_no"), rs.getString("name"),
                rs.getString("email"), rs.getString("phone"),
                rs.getString("department"), rs.getInt("year"));
    }

    @Override
    protected String insertSql() {
        return "INSERT INTO students (roll_no, name, email, phone, department, year) VALUES (?,?,?,?,?,?)";
    }

    @Override
    protected Object[] insertParams(Student s) {
        return new Object[]{s.getRollNo(), s.getName(), s.getEmail(), s.getPhone(), s.getDepartment(), s.getYear()};
    }

    @Override
    protected String updateSql() {
        return "UPDATE students SET roll_no=?, name=?, email=?, phone=?, department=?, year=? WHERE id=?";
    }

    @Override
    protected Object[] updateParams(long id, Student s) {
        return new Object[]{s.getRollNo(), s.getName(), s.getEmail(), s.getPhone(), s.getDepartment(), s.getYear(), id};
    }
}
