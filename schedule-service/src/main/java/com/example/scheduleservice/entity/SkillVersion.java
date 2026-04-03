package com.example.scheduleservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_skill_version")
public class SkillVersion {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long skillId;

    private Integer versionNumber;

    private String versionName;

    private String promptTemplate;

    private String parameters;

    private String releaseNotes;

    private Integer isActive;

    private Integer usageCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
