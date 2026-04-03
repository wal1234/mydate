package com.example.scheduleservice.controller;

import com.example.common.result.Result;
import com.example.scheduleservice.entity.ConversationContext;
import com.example.scheduleservice.service.ContextManagerService;
import com.example.scheduleservice.service.TokenCounterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/context")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ContextManagementController {

    private final ContextManagerService contextManagerService;
    private final TokenCounterService tokenCounterService;

    @GetMapping("/config")
    public Result<ConversationContext> getContextConfig(
            @RequestParam(required = false) Long userId,
            @RequestParam Long sessionId) {
        try {
            if (userId == null) {
                userId = 1L;
            }
            ConversationContext context = contextManagerService.getOrCreateContext(userId, sessionId);
            return Result.ok(context);
        } catch (Exception e) {
            log.error("获取上下文配置失败", e);
            return Result.fail("获取上下文配置失败：" + e.getMessage());
        }
    }

    @PutMapping("/config")
    public Result<ConversationContext> updateContextConfig(
            @RequestParam(required = false) Long userId,
            @RequestParam Long sessionId,
            @RequestBody Map<String, Object> config) {
        try {
            if (userId == null) {
                userId = 1L;
            }
            contextManagerService.updateContextConfig(userId, sessionId, config);
            ConversationContext context = contextManagerService.getOrCreateContext(userId, sessionId);
            return Result.ok(context);
        } catch (Exception e) {
            log.error("更新上下文配置失败", e);
            return Result.fail("更新上下文配置失败：" + e.getMessage());
        }
    }

    @PostMapping("/analyze")
    public Result<Map<String, Object>> analyzeContext(
            @RequestParam(required = false) Long userId,
            @RequestParam Long sessionId,
            @RequestParam String modelName) {
        try {
            if (userId == null) {
                userId = 1L;
            }
            
            ConversationContext context = contextManagerService.getOrCreateContext(userId, sessionId);
            
            Map<String, Object> analysis = new HashMap<>();
            analysis.put("contextWindow", tokenCounterService.getContextWindow(modelName));
            analysis.put("maxTokens", context.getMaxTokens());
            analysis.put("availableTokens", tokenCounterService.calculateAvailableContext(
                    modelName, context.getMaxTokens() != null ? context.getMaxTokens() / 4 : 1024));
            analysis.put("preserveSystem", context.getPreserveSystemMessages());
            analysis.put("summarizeEnabled", context.getSummarizeEnabled());
            analysis.put("summarizeThreshold", context.getSummarizeThreshold());
            
            return Result.ok(analysis);
        } catch (Exception e) {
            log.error("分析上下文失败", e);
            return Result.fail("分析上下文失败：" + e.getMessage());
        }
    }

    @PostMapping("/tokens/count")
    public Result<Map<String, Object>> countTokens(@RequestBody Map<String, Object> request) {
        try {
            String text = request.get("text").toString();
            int tokens = tokenCounterService.countTokens(text);
            
            Map<String, Object> result = new HashMap<>();
            result.put("text", text);
            result.put("characterCount", text.length());
            result.put("estimatedTokens", tokens);
            
            return Result.ok(result);
        } catch (Exception e) {
            log.error("计算Token失败", e);
            return Result.fail("计算Token失败：" + e.getMessage());
        }
    }

    @PostMapping("/summary")
    public Result<Map<String, Object>> generateSummary(
            @RequestParam(required = false) Long userId,
            @RequestParam Long sessionId) {
        try {
            if (userId == null) {
                userId = 1L;
            }
            
            ConversationContext context = contextManagerService.getOrCreateContext(userId, sessionId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("currentSummary", context.getCurrentSummary());
            result.put("lastUpdated", context.getUpdatedAt());
            
            return Result.ok(result);
        } catch (Exception e) {
            log.error("生成摘要失败", e);
            return Result.fail("生成摘要失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/summary")
    public Result<Void> clearSummary(
            @RequestParam(required = false) Long userId,
            @RequestParam Long sessionId) {
        try {
            if (userId == null) {
                userId = 1L;
            }
            contextManagerService.updateSummary(userId, sessionId, "");
            return Result.ok(null);
        } catch (Exception e) {
            log.error("清除摘要失败", e);
            return Result.fail("清除摘要失败：" + e.getMessage());
        }
    }

    @GetMapping("/models")
    public Result<Map<String, Integer>> getSupportedModels() {
        try {
            Map<String, Integer> models = new HashMap<>();
            models.put("gpt-4o", 128000);
            models.put("gpt-4-turbo", 128000);
            models.put("gpt-4", 8192);
            models.put("gpt-4o-mini", 128000);
            models.put("gpt-3.5-turbo", 16385);
            models.put("claude-3-opus", 200000);
            models.put("claude-3-sonnet", 200000);
            models.put("claude-3-haiku", 200000);
            models.put("gemini-pro", 30720);
            return Result.ok(models);
        } catch (Exception e) {
            log.error("获取模型列表失败", e);
            return Result.fail("获取模型列表失败：" + e.getMessage());
        }
    }
}
