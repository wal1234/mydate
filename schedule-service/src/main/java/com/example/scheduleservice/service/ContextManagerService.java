package com.example.scheduleservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.scheduleservice.entity.ChatMessage;
import com.example.scheduleservice.entity.ConversationContext;
import com.example.scheduleservice.mapper.ConversationContextMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContextManagerService {

    private final ConversationContextMapper contextMapper;
    private final TokenCounterService tokenCounter;
    private final ContextSummarizerService summarizer;

    private static final int DEFAULT_MAX_TOKENS = 6000;
    private static final int DEFAULT_CONTEXT_WINDOW = 128000;
    private static final double DEFAULT_SUMMARIZE_THRESHOLD = 0.8;

    public ConversationContext getOrCreateContext(Long userId, Long sessionId) {
        QueryWrapper<ConversationContext> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .eq("session_id", sessionId)
               .eq("deleted", 0);
        
        ConversationContext context = contextMapper.selectOne(wrapper);
        
        if (context == null) {
            context = new ConversationContext();
            context.setUserId(userId);
            context.setSessionId(sessionId);
            context.setMaxTokens(DEFAULT_MAX_TOKENS);
            context.setContextWindow(DEFAULT_CONTEXT_WINDOW);
            context.setPreserveSystemMessages(1);
            context.setSummarizeEnabled(1);
            context.setSummarizeThreshold(DEFAULT_SUMMARIZE_THRESHOLD);
            context.setCurrentSummary("");
            context.setCreatedAt(java.time.LocalDateTime.now());
            context.setUpdatedAt(java.time.LocalDateTime.now());
            contextMapper.insert(context);
        }
        
        return context;
    }

    public void updateContextConfig(Long userId, Long sessionId, Map<String, Object> config) {
        ConversationContext context = getOrCreateContext(userId, sessionId);
        
        if (config.containsKey("maxTokens")) {
            context.setMaxTokens((Integer) config.get("maxTokens"));
        }
        if (config.containsKey("contextWindow")) {
            context.setContextWindow((Integer) config.get("contextWindow"));
        }
        if (config.containsKey("preserveSystemMessages")) {
            context.setPreserveSystemMessages((Integer) config.get("preserveSystemMessages"));
        }
        if (config.containsKey("summarizeEnabled")) {
            context.setSummarizeEnabled((Integer) config.get("summarizeEnabled"));
        }
        if (config.containsKey("summarizeThreshold")) {
            context.setSummarizeThreshold((Double) config.get("summarizeThreshold"));
        }
        
        context.setUpdatedAt(java.time.LocalDateTime.now());
        contextMapper.updateById(context);
    }

    public List<Map<String, String>> buildOptimizedContext(
            List<ChatMessage> history,
            ConversationContext contextConfig,
            String modelName,
            String systemPrompt) {
        
        List<Map<String, String>> messages = new ArrayList<>();
        
        if (contextConfig.getCurrentSummary() != null && 
            !contextConfig.getCurrentSummary().isEmpty() &&
            contextConfig.getSummarizeEnabled() == 1) {
            Map<String, String> summaryMsg = new HashMap<>();
            summaryMsg.put("role", "system");
            summaryMsg.put("content", "【对话摘要】" + contextConfig.getCurrentSummary());
            messages.add(summaryMsg);
        }

        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemPrompt);
            messages.add(systemMsg);
        }

        List<Map<String, String>> historyMessages = history.stream()
                .map(msg -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("role", msg.getRole());
                    map.put("content", msg.getContent() != null ? msg.getContent() : "");
                    return map;
                })
                .collect(Collectors.toList());

        int maxTokens = contextConfig.getMaxTokens() != null ? 
                        contextConfig.getMaxTokens() : DEFAULT_MAX_TOKENS;
        
        int availableTokens = tokenCounter.calculateAvailableContext(
                modelName, 
                contextConfig.getMaxTokens() != null ? contextConfig.getMaxTokens() / 4 : 1024
        );

        boolean preserveSystem = contextConfig.getPreserveSystemMessages() != null && 
                                contextConfig.getPreserveSystemMessages() == 1;

        List<Map<String, String>> prunedHistory = tokenCounter.pruneMessages(
                historyMessages, 
                availableTokens, 
                preserveSystem
        );

        int historyTokens = tokenCounter.countMessagesTokens(prunedHistory);
        int totalTokens = tokenCounter.countMessagesTokens(messages) + historyTokens;
        
        if (contextConfig.getSummarizeEnabled() == 1 && 
            totalTokens > maxTokens * contextConfig.getSummarizeThreshold()) {
            log.info("Context exceeds threshold, summarization triggered");
        }

        messages.addAll(prunedHistory);

        return messages;
    }

    public void updateSummary(Long userId, Long sessionId, String summary) {
        ConversationContext context = getOrCreateContext(userId, sessionId);
        context.setCurrentSummary(summary);
        context.setUpdatedAt(java.time.LocalDateTime.now());
        contextMapper.updateById(context);
    }

    public int calculateContextUsage(List<ChatMessage> history, String systemPrompt, String modelName) {
        List<Map<String, String>> messages = new ArrayList<>();
        
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemPrompt);
            messages.add(systemMsg);
        }

        for (ChatMessage msg : history) {
            Map<String, String> map = new HashMap<>();
            map.put("role", msg.getRole());
            map.put("content", msg.getContent() != null ? msg.getContent() : "");
            messages.add(map);
        }

        int usedTokens = tokenCounter.countMessagesTokens(messages);
        int contextWindow = tokenCounter.getContextWindow(modelName);
        
        return (int) ((usedTokens / (double) contextWindow) * 100);
    }
}
