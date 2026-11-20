package com.smartcampus.model;

public class UserAccount {
    private long id;
    private String username;
    private String role;
    private String displayName;

    public UserAccount() {}

    public UserAccount(long id, String username, String role, String displayName) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.displayName = displayName;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
}
