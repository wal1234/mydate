package com.example.scheduleservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_skill")
public class Skill {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long categoryId;

    private String skillName;

    private String skillCode;

    private String description;

    private String skillType;

    private String icon;

    private String color;

    private Integer status;

    private Integer isDefault;

    private Integer priority;

    private String parameters;

    private String promptTemplate;

    private Integer maxTokens;

    private Double temperature;

    private String modelName;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
