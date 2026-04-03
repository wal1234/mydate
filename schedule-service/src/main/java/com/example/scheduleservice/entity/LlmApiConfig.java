package com.example.scheduleservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("llm_api_config")
public class LlmApiConfig {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String configName;

    private String apiUrl;

    private String apiKey;

    private String modelName;

    private String providerType;

    private Integer streamEnabled;

    private BigDecimal temperature;

    private Integer maxTokens;

    private String headers;

    private Integer isActive;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
