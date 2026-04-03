package com.example.scheduleservice.dto;

import com.example.scheduleservice.entity.DailyDocument;
import com.example.scheduleservice.entity.ScheduleEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 每日聚合视图：某日的日程列表 + 当日文档（若有），便于前端「一日一屏」展示。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyViewDTO {

    /** 该日的日程事件列表（按开始时间排序） */
    private List<ScheduleEvent> events;
    /** 该日的每日文档，无则为 null */
    private DailyDocument document;
}
