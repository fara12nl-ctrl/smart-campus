package com.smartcampus.web;

import com.smartcampus.dao.FacultyRepository;
import com.smartcampus.model.Faculty;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/faculty")
public class FacultyController extends AbstractCrudController<Faculty> {

    public FacultyController(FacultyRepository repo) {
        super(repo);
    }

    @Override
    protected String searchText(Faculty f) {
        return f.getEmpId() + " " + f.getName() + " " + f.getEmail() + " " + f.getDepartment() + " " + f.getDesignation();
    }
}
