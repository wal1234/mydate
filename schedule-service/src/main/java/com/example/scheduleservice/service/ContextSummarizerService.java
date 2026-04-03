package com.example.scheduleservice.service;

import com.example.scheduleservice.entity.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ContextSummarizerService {

    public String generateSummary(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return "";
        }

        StringBuilder summary = new StringBuilder();
        summary.append("对话概览：\n");

        long userMessages = messages.stream()
                .filter(m -> "user".equals(m.getRole()))
                .count();
        long assistantMessages = messages.stream()
                .filter(m -> "assistant".equals(m.getRole()))
                .count();

        summary.append(String.format("- 共 %d 条用户消息，%d 条助手回复\n", userMessages, assistantMessages));

        if (!messages.isEmpty()) {
            ChatMessage firstUser = messages.stream()
                    .filter(m -> "user".equals(m.getRole()))
                    .findFirst()
                    .orElse(null);
            
            if (firstUser != null) {
                String firstTopic = firstUser.getContent();
                if (firstTopic.length() > 50) {
                    firstTopic = firstTopic.substring(0, 50) + "...";
                }
                summary.append("- 话题：").append(firstTopic).append("\n");
            }

            List<String> keyTopics = extractKeyTopics(messages);
            if (!keyTopics.isEmpty()) {
                summary.append("- 关键主题：");
                summary.append(String.join("、", keyTopics));
                summary.append("\n");
            }
        }

        if (messages.size() > 10) {
            summary.append("- 最近讨论：");
            List<ChatMessage> recentMessages = messages.subList(
                    Math.max(0, messages.size() - 4), 
                    messages.size()
            );
            String recentSummary = recentMessages.stream()
                    .filter(m -> "user".equals(m.getRole()))
                    .map(m -> {
                        String content = m.getContent();
                        return content.length() > 30 ? content.substring(0, 30) + "..." : content;
                    })
                    .collect(Collectors.joining("; "));
            summary.append(recentSummary);
        }

        return summary.toString();
    }

    private List<String> extractKeyTopics(List<ChatMessage> messages) {
        return messages.stream()
                .filter(m -> "user".equals(m.getRole()))
                .map(ChatMessage::getContent)
                .flatMap(content -> java.util.Arrays.stream(content.split("[。！？.!?]")))
                .filter(sentence -> sentence.length() > 5 && sentence.length() < 100)
                .distinct()
                .limit(3)
                .map(sentence -> {
                    if (sentence.length() > 20) {
                        return sentence.substring(0, 20) + "...";
                    }
                    return sentence.trim();
                })
                .collect(Collectors.toList());
    }

    public String compressContext(List<ChatMessage> messages, int maxMessages) {
        if (messages.size() <= maxMessages) {
            return null;
        }

        List<ChatMessage> toCompress = messages.subList(0, messages.size() - maxMessages);
        List<ChatMessage> toKeep = messages.subList(messages.size() - maxMessages, messages.size());

        String summary = generateSummary(toCompress);

        StringBuilder compressed = new StringBuilder();
        compressed.append("【早期对话摘要】\n");
        compressed.append(summary);
        compressed.append("\n\n其余 ").append(toCompress.size()).append(" 条消息已省略。\n\n");

        compressed.append("【最近对话】\n");
        for (ChatMessage msg : toKeep) {
            compressed.append(msg.getRole()).append(": ");
            String content = msg.getContent();
            if (content.length() > 200) {
                content = content.substring(0, 200) + "...";
            }
            compressed.append(content).append("\n");
        }

        return compressed.toString();
    }
}
