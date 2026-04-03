package com.example.scheduleservice.service;

import com.example.scheduleservice.dto.DailyViewDTO;
import com.example.scheduleservice.entity.DailyDocument;
import com.example.scheduleservice.entity.ScheduleEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 每日聚合视图服务：返回某日的日程 + 当日文档，供「一日一屏」使用。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DailyViewService {

    private final ScheduleEventService scheduleEventService;
    private final DailyDocumentService dailyDocumentService;

    /**
     * 获取某日的聚合视图：当日日程列表 + 当日文档（若有）。
     *
     * @param date 自然日
     * @return events 与 document（document 可能为 null）
     */
    public DailyViewDTO getDailyView(LocalDate date) {
        log.debug("获取每日聚合视图, date={}", date);
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59, 999_000_000);
        List<ScheduleEvent> events = scheduleEventService.getByDateRange(start, end);
        DailyDocument document = dailyDocumentService.getByDocDate(date);
        log.info("获取每日聚合视图完成, date={}, events={}, hasDoc={}", date, events.size(), document != null);
        return new DailyViewDTO(events, document);
    }
}
