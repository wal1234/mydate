package com.example.scheduleservice.controller;

import com.example.common.result.Result;
import com.example.scheduleservice.entity.LlmApiConfig;
import com.example.scheduleservice.service.LlmApiConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/llm-config")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LlmApiConfigController {

    private final LlmApiConfigService llmApiConfigService;

    @GetMapping
    public Result<List<LlmApiConfig>> getConfigs(@RequestParam(required = false) Long userId) {
        try {
            if (userId == null) {
                userId = 1L;
            }
            List<LlmApiConfig> configs = llmApiConfigService.getConfigsByUser(userId);
            return Result.ok(configs);
        } catch (Exception e) {
            log.error("获取LLM配置失败", e);
            return Result.fail("获取配置失败：" + e.getMessage());
        }
    }

    @GetMapping("/active")
    public Result<LlmApiConfig> getActiveConfig(@RequestParam(required = false) Long userId) {
        try {
            if (userId == null) {
                userId = 1L;
            }
            LlmApiConfig config = llmApiConfigService.getActiveConfig(userId);
            return Result.ok(config);
        } catch (Exception e) {
            log.error("获取激活配置失败", e);
            return Result.fail("获取配置失败：" + e.getMessage());
        }
    }

    @PostMapping
    public Result<LlmApiConfig> saveConfig(@RequestBody LlmApiConfig config) {
        try {
            if (config.getUserId() == null) {
                config.setUserId(1L);
            }
            llmApiConfigService.saveOrUpdate(config);
            return Result.ok(config);
        } catch (Exception e) {
            log.error("保存LLM配置失败", e);
            return Result.fail("保存配置失败：" + e.getMessage());
        }
    }

    @PostMapping("/activate/{id}")
    public Result<Void> activateConfig(@PathVariable Long id, @RequestParam(required = false) Long userId) {
        try {
            if (userId == null) {
                userId = 1L;
            }
            llmApiConfigService.setActiveConfig(userId, id);
            return Result.ok(null);
        } catch (Exception e) {
            log.error("激活配置失败", e);
            return Result.fail("激活配置失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteConfig(@PathVariable Long id) {
        try {
            llmApiConfigService.removeById(id);
            return Result.ok(null);
        } catch (Exception e) {
            log.error("删除LLM配置失败", e);
            return Result.fail("删除配置失败：" + e.getMessage());
        }
    }
}
