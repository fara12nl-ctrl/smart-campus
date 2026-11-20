package com.smartcampus.dao;

import com.smartcampus.model.Classroom;
import com.smartcampus.model.Laboratory;
import com.smartcampus.model.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/** Returns the right subclass (Classroom/Laboratory) based on the row's type. */
@Repository
public class RoomRepository extends AbstractJdbcRepository<Resource> {

    public RoomRepository(JdbcTemplate jdbc) { super(jdbc); }

    @Override
    protected String table() { return "rooms"; }

    @Override
    protected String selectSql() { return "SELECT t.* FROM rooms t"; }

    @Override
    protected RowMapper<Resource> mapper() {
        return (rs, i) -> {
            long id = rs.getLong("id");
            String type = rs.getString("type");
            String roomNo = rs.getString("room_no");
            int capacity = rs.getInt("capacity");
            String building = rs.getString("building");
            // OOP: polymorphism in action - concrete subclass chosen from DB type
            return "LAB".equalsIgnoreCase(type)
                    ? new Laboratory(id, roomNo, capacity, building)
                    : new Classroom(id, roomNo, capacity, building);
        };
    }

    @Override
    protected String insertSql() {
        return "INSERT INTO rooms (room_no, type, capacity, building) VALUES (?,?,?,?)";
    }

    @Override
    protected Object[] insertParams(Resource r) {
        return new Object[]{r.getRoomNo(), r.getType(), r.getCapacity(), r.getBuilding()};
    }

    @Override
    protected String updateSql() {
        return "UPDATE rooms SET room_no=?, type=?, capacity=?, building=? WHERE id=?";
    }

    @Override
    protected Object[] updateParams(long id, Resource r) {
        return new Object[]{r.getRoomNo(), r.getType(), r.getCapacity(), r.getBuilding(), id};
    }
}
