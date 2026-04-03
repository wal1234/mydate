package com.example.scheduleservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 日程事件实体，对应表 schedule_event。
 * 支持标题、描述、开始/结束时间、全天事件、颜色、重复规则等。
 */
@Data
@TableName("schedule_event")
public class ScheduleEvent {

    @TableId(type = IdType.AUTO)
    private Long id;
    /** 用户 ID，首版可选 */
    private Long userId;
    /** 标题 */
    private String title;
    /** 描述 */
    private String description;
    /** 开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", shape = JsonFormat.Shape.STRING)
    private LocalDateTime startTime;
    /** 结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", shape = JsonFormat.Shape.STRING)
    private LocalDateTime endTime;
    /** 是否全天：0-否，1-是 */
    private Integer isAllDay;
    /** 颜色/分类 */
    private String color;
    /** 重复规则：NONE/DAILY/WEEKLY/MONTHLY */
    private String repeatRule;
    /** 重复截止日期 */
    private LocalDate repeatUntil;
    /** 提前提醒分钟数（如 15、30），null 表示不提醒） */
    private Integer reminderMinutes;
    /** 是否发送微信通知 */
    private Boolean notifyWechat;
    /** 是否发送QQ通知 */
    private Boolean notifyQQ;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
