package com.smartcampus.model;

public class Faculty extends Person {
    private String empId;
    private String department;
    private String designation;

    public Faculty() {}

    public Faculty(long id, String empId, String name, String email, String phone, String department, String designation) {
        super(name, email, phone);
        this.id = id;
        this.empId = empId;
        this.department = department;
        this.designation = designation;
    }

    @Override
    public String getRole() { return "FACULTY"; }

    public String getEmpId() { return empId; }
    public void setEmpId(String empId) { this.empId = empId; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
}
