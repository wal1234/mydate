package com.example.scheduleservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_notification_config")
public class UserNotificationConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String wechatServerChanKey;

    private String qqNumber;

    private String qmsgKey;

    private Boolean wechatEnabled;

    private Boolean qqEnabled;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
