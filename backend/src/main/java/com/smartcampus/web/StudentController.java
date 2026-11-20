package com.smartcampus.web;

import com.smartcampus.dao.StudentRepository;
import com.smartcampus.model.Student;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/students")
public class StudentController extends AbstractCrudController<Student> {

    public StudentController(StudentRepository repo) {
        super(repo);
    }

    @Override
    protected String searchText(Student s) {
        return s.getRollNo() + " " + s.getName() + " " + s.getEmail() + " " + s.getDepartment() + " " + s.getYear();
    }
}
