package com.smartcampus.web;

import com.smartcampus.dao.CourseRepository;
import com.smartcampus.model.Course;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses")
public class CourseController extends AbstractCrudController<Course> {

    public CourseController(CourseRepository repo) {
        super(repo);
    }

    @Override
    protected String searchText(Course c) {
        return c.getCode() + " " + c.getTitle() + " " + c.getSemester() + " " + (c.getFacultyName() == null ? "" : c.getFacultyName());
    }
}
