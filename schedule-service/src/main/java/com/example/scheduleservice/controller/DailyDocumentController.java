package com.example.scheduleservice.controller;

import com.example.common.result.PageResult;
import com.example.common.result.Result;
import com.example.common.result.ResultCode;
import com.example.scheduleservice.entity.DailyDocument;
import com.example.scheduleservice.service.DailyDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/daily-docs")
@RequiredArgsConstructor
@Slf4j
public class DailyDocumentController {

    private final DailyDocumentService dailyDocumentService;

    @GetMapping("/id/{id}")
    public Result<DailyDocument> getById(@PathVariable Long id) {
        log.info("Query daily doc by id, id={}", id);
        DailyDocument doc = dailyDocumentService.getById(id);
        if (doc == null) {
            return Result.fail(ResultCode.NOT_FOUND.getCode(), "Document not found");
        }
        return Result.ok(doc);
    }

    @GetMapping("/{date}")
    public Result<DailyDocument> getByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("Query daily doc by date, date={}", date);
        DailyDocument doc = dailyDocumentService.getByDocDate(date);
        if (doc == null) {
            return Result.fail(ResultCode.NOT_FOUND.getCode(), "No document for this date");
        }
        return Result.ok(doc);
    }

    @PostMapping
    public Result<DailyDocument> create(@RequestBody DailyDocument doc) {
        log.info("Create daily doc, date={}", doc.getDocDate());
        return Result.ok(dailyDocumentService.create(doc));
    }

    @PutMapping("/{id}")
    public Result<DailyDocument> update(@PathVariable Long id, @RequestBody DailyDocument doc) {
        log.info("Update daily doc, id={}", id);
        return Result.ok(dailyDocumentService.update(id, doc));
    }

    @GetMapping
    public Result<PageResult<DailyDocument>> list(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String date,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Query daily docs, from={}, to={}, date={}, page={}, size={}", from, to, date, page, size);
        if (date != null) {
            LocalDate docDate = LocalDate.parse(date);
            DailyDocument doc = dailyDocumentService.getByDocDate(docDate);
            return Result.ok(new PageResult<>(doc != null ? List.of(doc) : List.of(), 1L, 1, 10));
        }
        LocalDate fromDate = from != null ? LocalDate.parse(from) : LocalDate.now().minusMonths(1);
        LocalDate toDate = to != null ? LocalDate.parse(to) : LocalDate.now();
        PageResult<DailyDocument> result = dailyDocumentService.listByDateRangePaged(fromDate, toDate, page, size);
        return Result.ok(result);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id, @RequestParam(required = false, defaultValue = "1") Long userId) {
        log.info("Delete daily doc, id={}", id);
        dailyDocumentService.deleteById(id, userId);
        return Result.ok(null);
    }
}
