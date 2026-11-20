package com.smartcampus.web;

import com.smartcampus.dao.EventRepository;
import com.smartcampus.model.CampusEvent;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
public class EventController extends AbstractCrudController<CampusEvent> {

    public EventController(EventRepository repo) {
        super(repo);
    }

    @Override
    protected String searchText(CampusEvent e) {
        return e.getTitle() + " " + e.getVenue() + " " + e.getOrganizer() + " " + (e.getDescription() == null ? "" : e.getDescription());
    }
}
