package com.smartcampus.dao;

import com.smartcampus.model.Course;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class CourseRepository extends AbstractJdbcRepository<Course> {

    public CourseRepository(JdbcTemplate jdbc) { super(jdbc); }

    @Override
    protected String table() { return "courses"; }

    @Override
    protected String selectSql() {
        return "SELECT t.*, f.name AS faculty_name FROM courses t LEFT JOIN faculty f ON f.id = t.faculty_id";
    }

    @Override
    protected RowMapper<Course> mapper() {
        return (rs, i) -> {
            Long facultyId = rs.getObject("faculty_id", Long.class);
            return new Course(
                    rs.getLong("id"), rs.getString("code"), rs.getString("title"),
                    rs.getInt("credits"), rs.getInt("semester"),
                    facultyId, rs.getString("faculty_name"));
        };
    }

    @Override
    protected String insertSql() {
        return "INSERT INTO courses (code, title, credits, semester, faculty_id) VALUES (?,?,?,?,?)";
    }

    @Override
    protected Object[] insertParams(Course c) {
        return new Object[]{c.getCode(), c.getTitle(), c.getCredits(), c.getSemester(), c.getFacultyId()};
    }

    @Override
    protected String updateSql() {
        return "UPDATE courses SET code=?, title=?, credits=?, semester=?, faculty_id=? WHERE id=?";
    }

    @Override
    protected Object[] updateParams(long id, Course c) {
        return new Object[]{c.getCode(), c.getTitle(), c.getCredits(), c.getSemester(), c.getFacultyId(), id};
    }
}
