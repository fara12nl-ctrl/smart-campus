package com.smartcampus.dao;

import com.smartcampus.model.CampusEvent;
import com.smartcampus.util.Dates;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

@Repository
public class EventRepository extends AbstractJdbcRepository<CampusEvent> {

    public EventRepository(JdbcTemplate jdbc) { super(jdbc); }

    @Override
    protected String table() { return "events"; }

    @Override
    protected String selectSql() { return "SELECT t.* FROM events t"; }

    @Override
    protected RowMapper<CampusEvent> mapper() {
        return (rs, i) -> new CampusEvent(
                rs.getLong("id"), rs.getString("title"), rs.getString("description"),
                Dates.iso(rs.getTimestamp("event_date").toLocalDateTime()),
                rs.getString("venue"), rs.getString("organizer"));
    }

    @Override
    protected String insertSql() {
        return "INSERT INTO events (title, description, event_date, venue, organizer) VALUES (?,?,?,?,?)";
    }

    @Override
    protected Object[] insertParams(CampusEvent e) {
        return new Object[]{e.getTitle(), e.getDescription(),
                Timestamp.valueOf(Dates.parse(e.getEventDate())), e.getVenue(), e.getOrganizer()};
    }

    @Override
    protected String updateSql() {
        return "UPDATE events SET title=?, description=?, event_date=?, venue=?, organizer=? WHERE id=?";
    }

    @Override
    protected Object[] updateParams(long id, CampusEvent e) {
        return new Object[]{e.getTitle(), e.getDescription(),
                Timestamp.valueOf(Dates.parse(e.getEventDate())), e.getVenue(), e.getOrganizer(), id};
    }
}
