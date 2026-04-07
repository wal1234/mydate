package com.example.scheduleservice.controller;

import com.example.common.result.Result;
import com.example.scheduleservice.entity.ChatMessage;
import com.example.scheduleservice.entity.ChatSession;
import com.example.scheduleservice.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI 聊天 REST API 控制器
 *
 * <p>提供AI聊天的会话管理和消息收发功能。</p>
 *
 * @author Schedule Service
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;

    /**
     * 获取用户的会话列表
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    @GetMapping("/sessions")
    public Result<List<ChatSession>> getSessions(@RequestParam(required = false, defaultValue = "1") Long userId) {
        log.info("💬 【会话列表】获取会话列表 - 用户ID: {}", userId);

        try {
            List<ChatSession> sessions = chatService.getUserSessions(userId);
            log.info("✅ 【会话列表】获取成功 - 共 {} 个会话", sessions.size());
            return Result.ok(sessions);

        } catch (Exception e) {
            log.error("❌ 【会话列表】获取失败 - 用户ID: {}", userId, e);
            return Result.fail("获取会话列表失败：" + e.getMessage());
        }
    }

    /**
     * 创建新会话
     *
     * @param userId 用户ID
     * @return 新创建的会话
     */
    @PostMapping("/sessions")
    public Result<ChatSession> createSession(
            @RequestParam(required = false, defaultValue = "1") Long userId,
            @RequestBody(required = false) Map<String, String> body) {

        String title = body != null ? body.get("title") : null;
        log.info("💬 【创建会话】创建新会话 - 用户ID: {}, 标题: {}", userId, title);

        try {
            ChatSession session = chatService.createSession(userId, title);
            log.info("✅ 【创建会话】创建成功 - 会话ID: {}", session.getId());
            return Result.ok(session);

        } catch (Exception e) {
            log.error("❌ 【创建会话】创建失败", e);
            return Result.fail("创建会话失败：" + e.getMessage());
        }
    }

    /**
     * 获取会话消息历史
     *
     * @param sessionId 会话ID
     * @return 消息列表
     */
    @GetMapping("/sessions/{sessionId}/messages")
    public Result<List<ChatMessage>> getMessages(@PathVariable Long sessionId) {
        log.info("💬 【消息历史】获取消息历史 - 会话ID: {}", sessionId);

        try {
            List<ChatMessage> messages = chatService.getSessionMessages(sessionId);
            log.info("✅ 【消息历史】获取成功 - 会话ID: {}, 消息数: {}", sessionId, messages.size());
            return Result.ok(messages);

        } catch (Exception e) {
            log.error("❌ 【消息历史】获取失败 - 会话ID: {}", sessionId, e);
            return Result.fail("获取消息历史失败：" + e.getMessage());
        }
    }

    /**
     * 发送消息
     *
     * <p><b>请求体：</b></p>
     * <pre>
     * {
     *   "message": "你好，帮我安排明天的会议",
     *   "sessionId": 1
     * }
     * </pre>
     *
     * @param userId 用户ID
     * @param request 包含消息内容和会话ID
     * @return AI回复
     */
    @PostMapping("/send")
    public Result<Map<String, Object>> sendMessage(
            @RequestParam(required = false, defaultValue = "1") Long userId,
            @RequestBody Map<String, Object> request) {

        String message = (String) request.get("message");
        Long sessionId = request.get("sessionId") != null ?
                Long.valueOf(request.get("sessionId").toString()) : null;

        log.info("🤖 【发送消息】发送消息 - 用户ID: {}, 会话ID: {}", userId, sessionId);
        log.debug("   └─ 消息: {}", message);

        try {
            // 如果没有会话ID，创建一个新会话
            if (sessionId == null) {
                ChatSession newSession = chatService.createSession(userId, null);
                sessionId = newSession.getId();
                log.info("   └─ 创建新会话: {}", sessionId);
            }

            // 发送消息并获取回复
            String reply = chatService.sendMessage(userId, sessionId, message);

            log.info("✅ 【发送消息】消息发送成功 - 会话ID: {}", sessionId);
            log.debug("   └─ AI回复: {}", reply);

            return Result.ok(Map.of("reply", reply, "sessionId", sessionId));

        } catch (Exception e) {
            log.error("❌ 【发送消息】发送失败 - 用户ID: {}, 会话ID: {}", userId, sessionId, e);
            return Result.fail("发送消息失败：" + e.getMessage());
        }
    }

    /**
     * 删除会话
     *
     * @param sessionId 会话ID
     * @return 操作结果
     */
    @DeleteMapping("/sessions/{sessionId}")
    public Result<?> deleteSession(@PathVariable Long sessionId) {
        log.info("🗑️ 【删除会话】删除会话 - 会话ID: {}", sessionId);

        try {
            chatService.deleteSession(sessionId);
            log.info("✅ 【删除会话】删除成功 - 会话ID: {}", sessionId);
            return Result.ok(null);

        } catch (Exception e) {
            log.error("❌ 【删除会话】删除失败 - 会话ID: {}", sessionId, e);
            return Result.fail("删除会话失败：" + e.getMessage());
        }
    }
}
