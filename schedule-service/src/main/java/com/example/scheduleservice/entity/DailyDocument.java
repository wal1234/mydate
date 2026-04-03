package com.example.scheduleservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 每日文档实体，对应表 daily_document。
 * 按自然日维度，每日期一篇文档（日记/笔记），支持 plain / markdown。
 */
@Data
@TableName("daily_document")
public class DailyDocument {

    @TableId(type = IdType.AUTO)
    private Long id;
    /** 用户 ID，首版可选 */
    private Long userId;
    /** 文档日期（按用户唯一） */
    private LocalDate docDate;
    /** 标题 */
    private String title;
    /** 正文 */
    private String content;
    /** 内容类型：plain / markdown */
    private String contentType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
