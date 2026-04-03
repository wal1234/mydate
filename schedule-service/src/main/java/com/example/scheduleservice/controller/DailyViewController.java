package com.example.scheduleservice.controller;

import com.example.common.result.Result;
import com.example.scheduleservice.dto.DailyViewDTO;
import com.example.scheduleservice.service.DailyViewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/daily-view")
@RequiredArgsConstructor
public class DailyViewController {

    private final DailyViewService dailyViewService;

    @GetMapping
    public Result<DailyViewDTO> getDailyView(@RequestParam String date) {
        log.info("Get daily view, date={}", date);
        LocalDate docDate = LocalDate.parse(date);
        DailyViewDTO view = dailyViewService.getDailyView(docDate);
        return Result.ok(view);
    }
}
