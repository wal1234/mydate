package com.example.scheduleservice.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class TokenCounterService {

    private static final Map<String, Integer> MODEL_CONTEXTS = new HashMap<>();
    private static final double TOKENS_PER_CHAR = 0.25;

    static {
        MODEL_CONTEXTS.put("gpt-4o", 128000);
        MODEL_CONTEXTS.put("gpt-4-turbo", 128000);
        MODEL_CONTEXTS.put("gpt-4", 8192);
        MODEL_CONTEXTS.put("gpt-4o-mini", 128000);
        MODEL_CONTEXTS.put("gpt-3.5-turbo", 16385);
        MODEL_CONTEXTS.put("claude-3-opus", 200000);
        MODEL_CONTEXTS.put("claude-3-sonnet", 200000);
        MODEL_CONTEXTS.put("claude-3-haiku", 200000);
        MODEL_CONTEXTS.put("gemini-pro", 30720);
    }

    public int countTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return (int) Math.ceil(text.length() * TOKENS_PER_CHAR);
    }

    public int countMessageTokens(Map<String, String> message) {
        String role = message.getOrDefault("role", "");
        String content = message.getOrDefault("content", "");
        
        int tokens = countTokens(role) + countTokens(content);
        tokens += 4;
        
        return tokens;
    }

    public int countMessagesTokens(java.util.List<Map<String, String>> messages) {
        int total = 0;
        for (Map<String, String> message : messages) {
            total += countMessageTokens(message);
        }
        total += 3;
        return total;
    }

    public int getContextWindow(String modelName) {
        if (modelName == null) {
            return 4096;
        }
        
        for (Map.Entry<String, Integer> entry : MODEL_CONTEXTS.entrySet()) {
            if (modelName.toLowerCase().contains(entry.getKey().toLowerCase())) {
                return entry.getValue();
            }
        }
        
        return 4096;
    }

    public int calculateAvailableContext(String modelName, Integer maxResponseTokens) {
        int contextWindow = getContextWindow(modelName);
        int reservedTokens = maxResponseTokens != null ? maxResponseTokens : 1024;
        int buffer = 25;
        
        return contextWindow - reservedTokens - buffer;
    }

    public java.util.List<Map<String, String>> pruneMessages(
            java.util.List<Map<String, String>> messages,
            int maxTokens,
            boolean preserveSystem) {
        
        if (messages == null || messages.isEmpty()) {
            return messages;
        }

        java.util.List<Map<String, String>> systemMessages = new java.util.ArrayList<>();
        java.util.List<Map<String, String>> otherMessages = new java.util.ArrayList<>();

        for (Map<String, String> msg : messages) {
            if ("system".equals(msg.get("role")) && preserveSystem) {
                systemMessages.add(msg);
            } else {
                otherMessages.add(msg);
            }
        }

        int systemTokens = countMessagesTokens(systemMessages);
        int availableTokens = maxTokens - systemTokens;

        if (availableTokens <= 0) {
            return systemMessages;
        }

        java.util.List<Map<String, String>> result = new java.util.ArrayList<>(systemMessages);
        int currentTokens = systemTokens;

        for (int i = otherMessages.size() - 1; i >= 0 && currentTokens < maxTokens; i--) {
            Map<String, String> msg = otherMessages.get(i);
            int msgTokens = countMessageTokens(msg);
            
            if (currentTokens + msgTokens <= maxTokens) {
                result.add(0, msg);
                currentTokens += msgTokens;
            } else {
                break;
            }
        }

        return result;
    }
}
