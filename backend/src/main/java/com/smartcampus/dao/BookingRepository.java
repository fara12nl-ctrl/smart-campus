package com.smartcampus.dao;

import com.smartcampus.model.Booking;
import com.smartcampus.util.Dates;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class BookingRepository extends AbstractJdbcRepository<Booking> {

    public BookingRepository(JdbcTemplate jdbc) { super(jdbc); }

    @Override
    protected String table() { return "bookings"; }

    @Override
    protected String selectSql() { return "SELECT t.* FROM bookings t"; }

    @Override
    protected RowMapper<Booking> mapper() {
        return (rs, i) -> new Booking(
                rs.getLong("id"), rs.getString("resource_type"), rs.getLong("resource_id"),
                rs.getString("booked_by"), rs.getString("purpose"),
                Dates.iso(rs.getTimestamp("start_time").toLocalDateTime()),
                Dates.iso(rs.getTimestamp("end_time").toLocalDateTime()),
                rs.getString("status"));
    }

    @Override
    protected String insertSql() {
        return "INSERT INTO bookings (resource_type, resource_id, booked_by, purpose, start_time, end_time, status) "
                + "VALUES (?,?,?,?,?,?,?)";
    }

    @Override
    protected Object[] insertParams(Booking b) {
        return new Object[]{b.getResourceType(), b.getResourceId(), b.getBookedBy(), b.getPurpose(),
                Timestamp.valueOf(Dates.parse(b.getStartTime())),
                Timestamp.valueOf(Dates.parse(b.getEndTime())),
                b.getStatus() == null ? "CONFIRMED" : b.getStatus()};
    }

    @Override
    protected String updateSql() {
        return "UPDATE bookings SET resource_type=?, resource_id=?, booked_by=?, purpose=?, start_time=?, end_time=?, status=? WHERE id=?";
    }

    @Override
    protected Object[] updateParams(long id, Booking b) {
        return new Object[]{b.getResourceType(), b.getResourceId(), b.getBookedBy(), b.getPurpose(),
                Timestamp.valueOf(Dates.parse(b.getStartTime())),
                Timestamp.valueOf(Dates.parse(b.getEndTime())),
                b.getStatus() == null ? "CONFIRMED" : b.getStatus(), id};
    }

    /** Overlap check used by BookingService before inserting. */
    public boolean hasOverlap(String resourceType, long resourceId, LocalDateTime start, LocalDateTime end) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM bookings WHERE resource_type=? AND resource_id=? AND status='CONFIRMED' "
                        + "AND start_time < ? AND end_time > ?",
                Integer.class,
                resourceType, resourceId, Timestamp.valueOf(end), Timestamp.valueOf(start));
        return count != null && count > 0;
    }

    public List<Booking> findByResource(String resourceType, long resourceId) {
        return jdbc.query(selectSql() + " WHERE t.resource_type=? AND t.resource_id=? ORDER BY t.start_time",
                mapper(), resourceType, resourceId);
    }
}
