package com.example.scheduleservice.controller;

import com.example.common.result.Result;
import com.example.scheduleservice.entity.UserNotificationConfig;
import com.example.scheduleservice.service.UserNotificationConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/notification-config")
@RequiredArgsConstructor
public class NotificationConfigController {

    private final UserNotificationConfigService configService;

    @GetMapping
    public Result<UserNotificationConfig> getConfig(@RequestParam(required = false) Long userId) {
        log.info("Get notification config, userId={}", userId);
        try {
            UserNotificationConfig config = configService.getConfig(userId);
            if (config == null) {
                config = new UserNotificationConfig();
                config.setWechatEnabled(false);
                config.setQqEnabled(false);
            }
            return Result.ok(config);
        } catch (Exception e) {
            log.error("Failed to get notification config", e);
            UserNotificationConfig config = new UserNotificationConfig();
            config.setWechatEnabled(false);
            config.setQqEnabled(false);
            return Result.ok(config);
        }
    }

    @PostMapping
    public Result<UserNotificationConfig> save(@RequestBody UserNotificationConfig config) {
        log.info("Save notification config, wechatEnabled={}, qqEnabled={}", config.getWechatEnabled(), config.getQqEnabled());
        try {
            return Result.ok(configService.save(config));
        } catch (Exception e) {
            log.error("Failed to save notification config", e);
            return Result.fail(500, "保存失败: " + e.getMessage());
        }
    }
}
