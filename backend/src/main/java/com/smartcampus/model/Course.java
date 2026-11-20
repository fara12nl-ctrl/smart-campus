package com.smartcampus.model;

public class Course extends CampusEntity {
    private String code;
    private String title;
    private int credits;
    private int semester;
    private Long facultyId;
    private String facultyName;

    public Course() {}

    public Course(long id, String code, String title, int credits, int semester, Long facultyId, String facultyName) {
        this.id = id;
        this.code = code;
        this.title = title;
        this.credits = credits;
        this.semester = semester;
        this.facultyId = facultyId;
        this.facultyName = facultyName;
    }

    @Override
    public String getKind() { return "COURSE"; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }
    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }
    public Long getFacultyId() { return facultyId; }
    public void setFacultyId(Long facultyId) { this.facultyId = facultyId; }
    public String getFacultyName() { return facultyName; }
    public void setFacultyName(String facultyName) { this.facultyName = facultyName; }
}
