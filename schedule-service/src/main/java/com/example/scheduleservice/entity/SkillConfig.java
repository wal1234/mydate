package com.example.scheduleservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_skill_config")
public class SkillConfig {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long skillId;

    private String configKey;

    private String configValue;

    private String configType;

    private String description;

    private Integer required;

    private Integer isActive;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
