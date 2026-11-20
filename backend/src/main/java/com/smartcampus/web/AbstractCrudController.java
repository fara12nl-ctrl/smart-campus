package com.smartcampus.web;

import com.smartcampus.dao.CrudRepository;
import com.smartcampus.dto.ApiResponse;
import com.smartcampus.model.Identifiable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Generic REST base: one implementation of list/search/get/create/update/delete
 * works for every entity. Generics + interface programming in action.
 */
public abstract class AbstractCrudController<T extends Identifiable> {

    protected final CrudRepository<T> repo;

    protected AbstractCrudController(CrudRepository<T> repo) {
        this.repo = repo;
    }

    @GetMapping
    public ApiResponse<List<T>> list(@RequestParam(required = false) String q) {
        List<T> all = repo.findAll();
        if (q != null && !q.isBlank()) {
            String needle = q.trim().toLowerCase();
            all = all.stream()
                    .filter(e -> searchText(e).toLowerCase().contains(needle))
                    .collect(Collectors.toList());
        }
        return ApiResponse.ok(all);
    }

    @GetMapping("/{id}")
    public ApiResponse<T> get(@PathVariable long id) {
        return ApiResponse.ok(repo.findById(id)
                .orElseThrow(() -> new com.smartcampus.exception.NotFoundException("Record", id)));
    }

    @PostMapping
    public ApiResponse<T> create(@RequestBody T entity) {
        long id = repo.create(entity);
        return ApiResponse.ok("Created", repo.findById(id).orElse(entity));
    }

    @PutMapping("/{id}")
    public ApiResponse<T> update(@PathVariable long id, @RequestBody T entity) {
        repo.update(id, entity);
        return ApiResponse.ok("Updated", repo.findById(id).orElse(entity));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable long id) {
        repo.delete(id);
        return ApiResponse.ok("Deleted", null);
    }

    /** Each entity decides what the searchable text is. */
    protected String searchText(T entity) {
        return String.valueOf(entity);
    }
}
