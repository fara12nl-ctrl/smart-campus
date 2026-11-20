package com.smartcampus.dao;

import com.smartcampus.model.Equipment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class EquipmentRepository extends AbstractJdbcRepository<Equipment> {

    public EquipmentRepository(JdbcTemplate jdbc) { super(jdbc); }

    @Override
    protected String table() { return "equipment"; }

    @Override
    protected String selectSql() { return "SELECT t.* FROM equipment t"; }

    @Override
    protected RowMapper<Equipment> mapper() {
        return (rs, i) -> new Equipment(
                rs.getLong("id"), rs.getString("asset_tag"), rs.getString("name"),
                rs.getString("category"), rs.getObject("room_id", Long.class),
                rs.getString("status"));
    }

    @Override
    protected String insertSql() {
        return "INSERT INTO equipment (asset_tag, name, category, room_id, status) VALUES (?,?,?,?,?)";
    }

    @Override
    protected Object[] insertParams(Equipment e) {
        return new Object[]{e.getAssetTag(), e.getName(), e.getCategory(), e.getRoomId(), e.getStatus()};
    }

    @Override
    protected String updateSql() {
        return "UPDATE equipment SET asset_tag=?, name=?, category=?, room_id=?, status=? WHERE id=?";
    }

    @Override
    protected Object[] updateParams(long id, Equipment e) {
        return new Object[]{e.getAssetTag(), e.getName(), e.getCategory(), e.getRoomId(), e.getStatus(), id};
    }
}
