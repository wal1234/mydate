package com.example.scheduleservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_skill_category")
public class SkillCategory {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String categoryName;

    private String categoryCode;

    private String description;

    private String icon;

    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
