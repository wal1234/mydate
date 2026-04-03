package com.example.scheduleservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("conversation_context")
public class ConversationContext {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long sessionId;

    private Integer maxTokens;

    private Integer contextWindow;

    private Integer preserveSystemMessages;

    private Integer summarizeEnabled;

    private Double summarizeThreshold;

    private String currentSummary;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
