package com.example.scheduleservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.scheduleservice.entity.*;
import com.example.scheduleservice.mapper.ChatMessageMapper;
import com.example.scheduleservice.mapper.ChatSessionMapper;
import com.example.scheduleservice.mapper.ScheduleEventMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ChatService extends ServiceImpl<ChatMessageMapper, ChatMessage> {

    @Autowired
    private ChatSessionMapper chatSessionMapper;

    @Autowired
    private ScheduleEventMapper scheduleEventMapper;

    @Autowired
    private LlmApiConfigService llmApiConfigService;

    @Autowired
    private ContextManagerService contextManagerService;

    @Autowired
    private PromptTemplateService promptTemplateService;

    @Autowired
    private PromptRenderService promptRenderService;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatSession createSession(Long userId, String title) {
        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setSessionTitle(title != null ? title : "新对话");
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        chatSessionMapper.insert(session);
        return session;
    }

    public List<ChatSession> getUserSessions(Long userId) {
        QueryWrapper<ChatSession> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .eq("deleted", 0)
                .orderByDesc("updated_at");
        return chatSessionMapper.selectList(wrapper);
    }

    public List<ChatMessage> getSessionMessages(Long sessionId) {
        QueryWrapper<ChatMessage> wrapper = new QueryWrapper<>();
        wrapper.eq("session_id", sessionId)
                .orderByAsc("created_at");
        return this.list(wrapper);
    }

    public void deleteSession(Long sessionId) {
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session != null) {
            session.setDeleted(1);
            chatSessionMapper.updateById(session);
        }
    }

    public String sendMessage(Long userId, Long sessionId, String userMessage) {
        LlmApiConfig config = llmApiConfigService.getActiveConfig(userId);
        if (config == null) {
            return "错误：请先在设置中配置并激活大模型API";
        }

        userMessage = userMessage.trim();
        if (userMessage.isEmpty()) {
            return "错误：消息内容不能为空";
        }

        ChatMessage userMsg = new ChatMessage();
        userMsg.setSessionId(sessionId);
        userMsg.setRole("user");
        userMsg.setContent(userMessage);
        userMsg.setCreatedAt(LocalDateTime.now());
        this.save(userMsg);

        List<ChatMessage> history = getSessionMessages(sessionId);

        String assistantReply = callLlmApiWithContext(config, userId, sessionId, userMessage, history);

        ChatMessage assistantMsg = new ChatMessage();
        assistantMsg.setSessionId(sessionId);
        assistantMsg.setRole("assistant");
        assistantMsg.setContent(assistantReply);
        assistantMsg.setCreatedAt(LocalDateTime.now());
        this.save(assistantMsg);

        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session != null) {
            session.setUpdatedAt(LocalDateTime.now());
            if (session.getSessionTitle() == null || session.getSessionTitle().equals("新对话")) {
                session.setSessionTitle(userMessage.length() > 20 ? userMessage.substring(0, 20) + "..." : userMessage);
            }
            chatSessionMapper.updateById(session);
        }

        return assistantReply;
    }

    public String sendMessageWithPrompt(Long userId, Long sessionId, String userMessage, Long templateId) {
        LlmApiConfig config = llmApiConfigService.getActiveConfig(userId);
        if (config == null) {
            return "错误：请先在设置中配置并激活大模型API";
        }

        userMessage = userMessage.trim();
        if (userMessage.isEmpty()) {
            return "错误：消息内容不能为空";
        }

        ChatMessage userMsg = new ChatMessage();
        userMsg.setSessionId(sessionId);
        userMsg.setRole("user");
        userMsg.setContent(userMessage);
        userMsg.setCreatedAt(LocalDateTime.now());
        this.save(userMsg);

        List<ChatMessage> history = getSessionMessages(sessionId);

        String scheduleContext = getScheduleContext(userId);

        Map<String, Object> variables = new HashMap<>();
        variables.put("scheduleContext", scheduleContext);
        variables.put("userMessage", userMessage);
        variables.put("currentTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

        String systemPrompt = "你是一个智能助手，可以帮助用户管理日程和回答问题。\n\n" + scheduleContext;

        if (templateId != null) {
            PromptTemplate template = promptTemplateService.getTemplateById(templateId);
            if (template != null) {
                PromptVersion activeVersion = promptTemplateService.getActiveVersion(templateId);
                if (activeVersion != null) {
                    template.setTemplateContent(activeVersion.getContent());
                }
                systemPrompt = promptRenderService.render(template.getTemplateContent(), variables);
            }
        }

        String assistantReply = callLlmApi(config, userMessage, systemPrompt, history);

        ChatMessage assistantMsg = new ChatMessage();
        assistantMsg.setSessionId(sessionId);
        assistantMsg.setRole("assistant");
        assistantMsg.setContent(assistantReply);
        assistantMsg.setCreatedAt(LocalDateTime.now());
        this.save(assistantMsg);

        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session != null) {
            session.setUpdatedAt(LocalDateTime.now());
            if (session.getSessionTitle() == null || session.getSessionTitle().equals("新对话")) {
                session.setSessionTitle(userMessage.length() > 20 ? userMessage.substring(0, 20) + "..." : userMessage);
            }
            chatSessionMapper.updateById(session);
        }

        return assistantReply;
    }

    private String callLlmApiWithContext(LlmApiConfig config, Long userId, Long sessionId, 
                                        String userMessage, List<ChatMessage> history) {
        try {
            String scheduleContext = getScheduleContext(userId);

            ConversationContext contextConfig = contextManagerService.getOrCreateContext(userId, sessionId);

            Map<String, Object> variables = new HashMap<>();
            variables.put("scheduleContext", scheduleContext);
            variables.put("userMessage", userMessage);
            variables.put("currentTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

            String systemPrompt = "你是一个智能助手，可以帮助用户管理日程和回答问题。\n\n" + scheduleContext;

            List<Map<String, String>> messages = contextManagerService.buildOptimizedContext(
                    history,
                    contextConfig,
                    config.getModelName(),
                    systemPrompt
            );

            messages.add(Map.of("role", "user", "content", userMessage));

            return sendToLlm(config, messages);

        } catch (Exception e) {
            log.error("调用大模型API失败", e);
            return "抱歉，调用AI服务时出错：" + e.getMessage();
        }
    }

    private String getScheduleContext(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfToday = startOfToday.plusDays(1);
        LocalDateTime endOfWeek = startOfToday.plusDays(7);

        QueryWrapper<ScheduleEvent> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .eq("deleted", 0)
                .and(w -> w.and(w1 -> w1.ge("start_time", startOfToday).le("end_time", endOfWeek))
                        .or(w2 -> w2.ge("start_time", startOfToday).le("start_time", endOfWeek)))
                .orderByAsc("start_time");

        List<ScheduleEvent> events = scheduleEventMapper.selectList(wrapper);

        if (events.isEmpty()) {
            return "用户当前没有已安排的日程。";
        }

        StringBuilder context = new StringBuilder();
        context.append("以下是用户最近一周的日程安排：\n\n");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm");

        for (ScheduleEvent event : events) {
            String startTime = event.getStartTime().format(formatter);
            String endTime = event.getEndTime().format(formatter);
            String allDay = event.getIsAllDay() == 1 ? "全天" : startTime + " - " + endTime;

            context.append(String.format("- [%s] %s",
                    allDay,
                    event.getTitle()));

            if (event.getDescription() != null && !event.getDescription().isEmpty()) {
                context.append(String.format(" | %s", event.getDescription()));
            }
            context.append("\n");
        }

        context.append("\n请根据以上日程信息回答用户的问题。");
        return context.toString();
    }

    private String callLlmApi(LlmApiConfig config, String userMessage, String systemPrompt, List<ChatMessage> history) {
        try {
            List<Map<String, String>> messages = new ArrayList<>();

            messages.add(Map.of("role", "system", "content", systemPrompt));

            for (ChatMessage msg : history) {
                messages.add(Map.of(
                        "role", msg.getRole(),
                        "content", msg.getContent() != null ? msg.getContent() : ""
                ));
            }

            messages.add(Map.of("role", "user", "content", userMessage));

            return sendToLlm(config, messages);

        } catch (Exception e) {
            log.error("调用大模型API失败", e);
            return "抱歉，调用AI服务时出错：" + e.getMessage();
        }
    }

    private String sendToLlm(LlmApiConfig config, List<Map<String, String>> messages) {
        try {
            String apiUrl = config.getApiUrl();
            if (!apiUrl.endsWith("/chat/completions")) {
                apiUrl = apiUrl.endsWith("/") ? apiUrl + "chat/completions" : apiUrl + "/chat/completions";
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            if (config.getApiKey() != null && !config.getApiKey().isEmpty()) {
                headers.set("Authorization", "Bearer " + config.getApiKey());
            }

            if (config.getHeaders() != null && !config.getHeaders().isEmpty()) {
                try {
                    Map<String, String> customHeaders = objectMapper.readValue(config.getHeaders(), Map.class);
                    customHeaders.forEach(headers::set);
                } catch (Exception e) {
                    log.warn("Failed to parse custom headers", e);
                }
            }

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", config.getModelName());
            requestBody.put("messages", messages);

            if (config.getStreamEnabled() != null && config.getStreamEnabled() == 1) {
                requestBody.put("stream", false);
            }

            if (config.getTemperature() != null) {
                requestBody.put("temperature", config.getTemperature());
            }

            if (config.getMaxTokens() != null) {
                requestBody.put("max_tokens", config.getMaxTokens());
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");

                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> firstChoice = choices.get(0);
                    Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
                    return (String) message.get("content");
                }
            }

            return "抱歉，API响应格式异常";

        } catch (Exception e) {
            log.error("调用大模型API失败", e);
            return "抱歉，调用AI服务时出错：" + e.getMessage();
        }
    }
}
