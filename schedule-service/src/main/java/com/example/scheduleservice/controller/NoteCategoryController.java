package com.example.scheduleservice.controller;

import com.example.common.result.Result;
import com.example.scheduleservice.entity.NoteCategory;
import com.example.scheduleservice.service.NoteCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/note-categories")
@RequiredArgsConstructor
public class NoteCategoryController {

    private final NoteCategoryService categoryService;

    @GetMapping
    public Result<List<NoteCategory>> list() {
        log.info("Query note category list");
        return Result.ok(categoryService.list(null));
    }

    @GetMapping("/{id}")
    public Result<NoteCategory> getById(@PathVariable Long id) {
        log.info("Query note category by id, id={}", id);
        return Result.ok(categoryService.getById(id));
    }

    @PostMapping
    public Result<NoteCategory> create(@RequestBody NoteCategory category) {
        log.info("Create note category, name={}", category.getName());
        return Result.ok(categoryService.create(category));
    }

    @PutMapping("/{id}")
    public Result<NoteCategory> update(@PathVariable Long id, @RequestBody NoteCategory category) {
        log.info("Update note category, id={}", id);
        return Result.ok(categoryService.update(id, category));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        log.info("Delete note category, id={}", id);
        categoryService.delete(id);
        return Result.ok(null);
    }
}
