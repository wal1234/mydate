package com.example.scheduleservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("trash_item")
public class TrashItem {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String itemType;

    private Long originalId;

    private String itemTitle;

    private String itemContent;

    private String originalData;

    private LocalDateTime deletedAt;

    private LocalDateTime expireAt;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
