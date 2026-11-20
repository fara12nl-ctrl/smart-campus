package com.smartcampus.model;

import java.time.LocalDateTime;

public class Equipment extends CampusEntity implements Bookable {
    private String assetTag;
    private String name;
    private String category;
    private Long roomId;
    private String status;

    public Equipment() {}

    public Equipment(long id, String assetTag, String name, String category, Long roomId, String status) {
        this.id = id;
        this.assetTag = assetTag;
        this.name = name;
        this.category = category;
        this.roomId = roomId;
        this.status = status;
    }

    @Override
    public String getKind() { return "EQUIPMENT"; }

    @Override
    public boolean isBookable(LocalDateTime start, LocalDateTime end) {
        return "AVAILABLE".equalsIgnoreCase(status)
                && start != null && end != null
                && end.isAfter(start)
                && !end.isBefore(LocalDateTime.now());
    }

    public String getAssetTag() { return assetTag; }
    public void setAssetTag(String assetTag) { this.assetTag = assetTag; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
