package com.example.scheduleservice.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.scheduleservice.entity.UserNotificationConfig;
import com.example.scheduleservice.mapper.UserNotificationConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserNotificationConfigService {

    private final UserNotificationConfigMapper configMapper;

    public UserNotificationConfig getByUserId(Long userId) {
        LambdaQueryWrapper<UserNotificationConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserNotificationConfig::getUserId, userId);
        wrapper.eq(UserNotificationConfig::getDeleted, 0);
        return configMapper.selectOne(wrapper);
    }

    public UserNotificationConfig getDefaultConfig() {
        LambdaQueryWrapper<UserNotificationConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(UserNotificationConfig::getUserId);
        wrapper.eq(UserNotificationConfig::getDeleted, 0);
        return configMapper.selectOne(wrapper);
    }

    public UserNotificationConfig getConfig(Long userId) {
        UserNotificationConfig config = getByUserId(userId);
        if (config == null) {
            config = getDefaultConfig();
        }
        return config;
    }

    public UserNotificationConfig save(UserNotificationConfig config) {
        if (config.getId() != null) {
            configMapper.updateById(config);
            log.info("Update notification config, id={}", config.getId());
        } else {
            configMapper.insert(config);
            log.info("Create notification config, id={}", config.getId());
        }
        return config;
    }
}
