package com.smartcampus.model;

public class Student extends Person {
    private String rollNo;
    private String department;
    private int year;

    public Student() {}

    public Student(long id, String rollNo, String name, String email, String phone, String department, int year) {
        super(name, email, phone);
        this.id = id;
        this.rollNo = rollNo;
        this.department = department;
        this.year = year;
    }

    @Override
    public String getRole() { return "STUDENT"; }

    public String getRollNo() { return rollNo; }
    public void setRollNo(String rollNo) { this.rollNo = rollNo; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
}
