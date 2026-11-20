package com.smartcampus.web;

import com.smartcampus.dao.EquipmentRepository;
import com.smartcampus.model.Equipment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/equipment")
public class EquipmentController extends AbstractCrudController<Equipment> {

    public EquipmentController(EquipmentRepository repo) {
        super(repo);
    }

    @Override
    protected String searchText(Equipment e) {
        return e.getAssetTag() + " " + e.getName() + " " + e.getCategory() + " " + e.getStatus();
    }
}
