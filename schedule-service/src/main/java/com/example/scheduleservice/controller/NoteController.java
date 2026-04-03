package com.example.scheduleservice.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.common.result.Result;
import com.example.scheduleservice.entity.Note;
import com.example.scheduleservice.service.NoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @GetMapping
    public Result<IPage<Note>> list(
            @RequestParam(required = false) String contentType,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Query notes, contentType={}, categoryId={}, keyword={}, page={}, size={}", 
                contentType, categoryId, keyword, page, size);
        IPage<Note> result = noteService.list(contentType, categoryId, keyword, page, size);
        return Result.ok(result);
    }

    @GetMapping("/type/{contentType}")
    public Result<?> findByType(@PathVariable String contentType) {
        log.info("Query notes by type, contentType={}", contentType);
        return Result.ok(noteService.findByContentType(contentType));
    }

    @GetMapping("/category/{categoryId}")
    public Result<?> findByCategory(@PathVariable Long categoryId) {
        log.info("Query notes by category, categoryId={}", categoryId);
        return Result.ok(noteService.findByCategory(categoryId));
    }

    @GetMapping("/{id}")
    public Result<Note> getById(@PathVariable Long id) {
        log.info("Query note by id, id={}", id);
        return Result.ok(noteService.getById(id));
    }

    @PostMapping
    public Result<Note> create(@RequestBody Note note) {
        log.info("Create note, title={}", note.getTitle());
        return Result.ok(noteService.create(note));
    }

    @PutMapping("/{id}")
    public Result<Note> update(@PathVariable Long id, @RequestBody Note note) {
        log.info("Update note, id={}", id);
        return Result.ok(noteService.update(id, note));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id, @RequestParam(required = false, defaultValue = "1") Long userId) {
        log.info("Delete note, id={}", id);
        noteService.delete(id, userId);
        return Result.ok(null);
    }

    @PostMapping("/{id}/pin")
    public Result<Note> togglePin(@PathVariable Long id) {
        log.info("Toggle note pin status, id={}", id);
        return Result.ok(noteService.togglePin(id));
    }
}
