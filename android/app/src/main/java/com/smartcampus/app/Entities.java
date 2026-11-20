package com.smartcampus.app;

import java.util.LinkedHashMap;
import java.util.Map;

/** Declarative field/entity configuration shared by list + form screens. */
public final class Entities {

    public static final int TEXT = 0;
    public static final int NUMBER = 1;
    public static final int DATETIME = 2;
    public static final int SELECT = 3;

    public static class Field {
        public final String key;
        public final String label;
        public final int type;
        public final String[] options;
        public final boolean required;

        Field(String key, String label, int type, String[] options, boolean required) {
            this.key = key;
            this.label = label;
            this.type = type;
            this.options = options;
            this.required = required;
        }
    }

    public static class Entity {
        public final String key;
        public final String title;
        public final String api;
        public final String titleField;
        public final String[] subFields;
        public final Field[] fields;
        public final boolean editable;

        Entity(String key, String title, String api, String titleField, String[] subFields,
               Field[] fields, boolean editable) {
            this.key = key;
            this.title = title;
            this.api = api;
            this.titleField = titleField;
            this.subFields = subFields;
            this.fields = fields;
            this.editable = editable;
        }
    }

    public static final Map<String, Entity> ALL = new LinkedHashMap<>();

    private static Field t(String key, String label, boolean required) {
        return new Field(key, label, TEXT, null, required);
    }

    private static Field num(String key, String label) {
        return new Field(key, label, NUMBER, null, false);
    }

    private static Field numReq(String key, String label) {
        return new Field(key, label, NUMBER, null, true);
    }

    private static Field dt(String key, String label, boolean required) {
        return new Field(key, label, DATETIME, null, required);
    }

    private static Field sel(String key, String label, String... options) {
        return new Field(key, label, SELECT, options, false);
    }

    static {
        ALL.put("students", new Entity("students", "Students", "/api/students", "name",
                new String[]{"rollNo", "department", "year"},
                new Field[]{
                        t("rollNo", "Roll No", true), t("name", "Name", true),
                        t("email", "Email", true), t("phone", "Phone", false),
                        t("department", "Department", true), numReq("year", "Year")
                }, true));

        ALL.put("faculty", new Entity("faculty", "Faculty", "/api/faculty", "name",
                new String[]{"empId", "department", "designation"},
                new Field[]{
                        t("empId", "Employee ID", true), t("name", "Name", true),
                        t("email", "Email", true), t("phone", "Phone", false),
                        t("department", "Department", true), t("designation", "Designation", true)
                }, true));

        ALL.put("courses", new Entity("courses", "Courses", "/api/courses", "title",
                new String[]{"code", "semester", "facultyName"},
                new Field[]{
                        t("code", "Course Code", true), t("title", "Title", true),
                        num("credits", "Credits"), numReq("semester", "Semester"),
                        num("facultyId", "Faculty ID (optional)")
                }, true));

        ALL.put("resources", new Entity("resources", "Classrooms & Labs", "/api/resources", "roomNo",
                new String[]{"type", "capacity", "building"},
                new Field[]{
                        t("roomNo", "Room No", true),
                        sel("type", "Type", "CLASSROOM", "LAB"),
                        numReq("capacity", "Capacity"), t("building", "Building", true)
                }, true));

        ALL.put("equipment", new Entity("equipment", "Equipment", "/api/equipment", "name",
                new String[]{"assetTag", "category", "status"},
                new Field[]{
                        t("assetTag", "Asset Tag", true), t("name", "Name", true),
                        t("category", "Category", true), num("roomId", "Room ID (optional)"),
                        sel("status", "Status", "AVAILABLE", "IN_USE", "MAINTENANCE")
                }, true));

        ALL.put("events", new Entity("events", "Campus Events", "/api/events", "title",
                new String[]{"eventDate", "venue", "organizer"},
                new Field[]{
                        t("title", "Title", true), dt("eventDate", "Date & Time", true),
                        t("venue", "Venue", true), t("organizer", "Organizer", true),
                        t("description", "Description", false)
                }, true));

        ALL.put("bookings", new Entity("bookings", "Resource Booking", "/api/bookings", "purpose",
                new String[]{"resourceType", "resourceId", "startTime", "endTime"},
                new Field[]{
                        sel("resourceType", "Resource Type", "ROOM", "EQUIPMENT"),
                        numReq("resourceId", "Resource ID"),
                        t("purpose", "Purpose", true),
                        dt("startTime", "Start Time", true), dt("endTime", "End Time", true)
                }, false));
    }

    public static Entity get(String key) {
        return ALL.get(key);
    }
}
