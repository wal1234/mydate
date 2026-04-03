package com.example.scheduleservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.scheduleservice.entity.LlmApiConfig;
import com.example.scheduleservice.mapper.LlmApiConfigMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LlmApiConfigService extends ServiceImpl<LlmApiConfigMapper, LlmApiConfig> {

    public LlmApiConfig getActiveConfig(Long userId) {
        QueryWrapper<LlmApiConfig> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .eq("is_active", 1)
                .eq("deleted", 0);
        return this.getOne(wrapper);
    }

    public List<LlmApiConfig> getConfigsByUser(Long userId) {
        QueryWrapper<LlmApiConfig> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .eq("deleted", 0)
                .orderByDesc("is_active")
                .orderByDesc("created_at");
        return this.list(wrapper);
    }

    public void setActiveConfig(Long userId, Long configId) {
        QueryWrapper<LlmApiConfig> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("deleted", 0);
        List<LlmApiConfig> configs = this.list(wrapper);

        for (LlmApiConfig config : configs) {
            if (config.getId().equals(configId)) {
                config.setIsActive(1);
            } else {
                config.setIsActive(0);
            }
            this.updateById(config);
        }
    }
}
