package com.example.scheduleservice.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.common.result.PageResult;
import com.example.common.result.Result;
import com.example.scheduleservice.entity.ScheduleEvent;
import com.example.scheduleservice.service.ScheduleEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleEventService scheduleEventService;

    @GetMapping
    public Result<PageResult<ScheduleEvent>> list(
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(required = false) String view,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Query schedules, start={}, end={}, view={}, page={}, size={}", start, end, view, page, size);
        LocalDateTime startTime = LocalDateTime.parse(start + "T00:00:00");
        LocalDateTime endTime = LocalDateTime.parse(end + "T23:59:59");
        PageResult<ScheduleEvent> result = scheduleEventService.listByTimeRangePaged(startTime, endTime, page, size);
        return Result.ok(result);
    }

    @GetMapping("/today")
    public Result<List<ScheduleEvent>> getTodaySchedules() {
        log.info("Get today's schedules");
        LocalDate today = LocalDate.now();
        LocalDateTime startTime = today.atStartOfDay();
        LocalDateTime endTime = today.plusDays(1).atStartOfDay();
        List<ScheduleEvent> events = scheduleEventService.listByTimeRange(startTime, endTime);
        return Result.ok(events);
    }

    @PostMapping
    public Result<ScheduleEvent> create(@RequestBody ScheduleEvent event) {
        log.info("Create schedule, title={}", event.getTitle());
        return Result.ok(scheduleEventService.create(event));
    }

    @PutMapping("/{id}")
    public Result<ScheduleEvent> update(@PathVariable Long id, @RequestBody ScheduleEvent event) {
        log.info("Update schedule, id={}", id);
        return Result.ok(scheduleEventService.update(id, event));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id, @RequestParam(required = false, defaultValue = "1") Long userId) {
        log.info("Delete schedule, id={}", id);
        scheduleEventService.deleteById(id, userId);
        return Result.ok(null);
    }
}
