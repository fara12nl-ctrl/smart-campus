package com.smartcampus.dao;

import com.smartcampus.model.UserAccount;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbc;

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Returns [UserAccount, passwordHash] for a raw login check done in AuthService. */
    public Optional<Object[]> findCredentials(String username) {
        List<Object[]> rows = jdbc.query(
                "SELECT id, username, `role`, display_name, password_hash FROM users WHERE username = ?",
                (rs, i) -> new Object[]{
                        new UserAccount(rs.getLong("id"), rs.getString("username"),
                                rs.getString("role"), rs.getString("display_name")),
                        rs.getString("password_hash")},
                username);
        return rows.stream().findFirst();
    }
}
