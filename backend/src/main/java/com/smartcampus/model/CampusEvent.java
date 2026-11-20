package com.smartcampus.model;

public class CampusEvent extends CampusEntity {
    private String title;
    private String description;
    private String eventDate; // ISO local date-time, e.g. 2026-11-25T09:00:00
    private String venue;
    private String organizer;

    public CampusEvent() {}

    public CampusEvent(long id, String title, String description, String eventDate, String venue, String organizer) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.eventDate = eventDate;
        this.venue = venue;
        this.organizer = organizer;
    }

    @Override
    public String getKind() { return "EVENT"; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getEventDate() { return eventDate; }
    public void setEventDate(String eventDate) { this.eventDate = eventDate; }
    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }
    public String getOrganizer() { return organizer; }
    public void setOrganizer(String organizer) { this.organizer = organizer; }
}
