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

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/sessions")
    public Result<List<ChatSession>> getSessions(@RequestParam(required = false) Long userId) {
        try {
            if (userId == null) {
                userId = 1L;
            }
            List<ChatSession> sessions = chatService.getUserSessions(userId);
            return Result.ok(sessions);
        } catch (Exception e) {
            log.error("获取会话列表失败", e);
            return Result.fail("获取会话失败：" + e.getMessage());
        }
    }

    @PostMapping("/sessions")
    public Result<ChatSession> createSession(@RequestBody Map<String, Object> request) {
        try {
            Long userId = request.get("userId") != null ?
                    Long.valueOf(request.get("userId").toString()) : 1L;
            String title = request.get("title") != null ?
                    request.get("title").toString() : null;

            ChatSession session = chatService.createSession(userId, title);
            return Result.ok(session);
        } catch (Exception e) {
            log.error("创建会话失败", e);
            return Result.fail("创建会话失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/sessions/{sessionId}")
    public Result<Void> deleteSession(@PathVariable Long sessionId) {
        try {
            chatService.deleteSession(sessionId);
            return Result.ok(null);
        } catch (Exception e) {
            log.error("删除会话失败", e);
            return Result.fail("删除会话失败：" + e.getMessage());
        }
    }

    @GetMapping("/messages/{sessionId}")
    public Result<List<ChatMessage>> getMessages(@PathVariable Long sessionId) {
        try {
            List<ChatMessage> messages = chatService.getSessionMessages(sessionId);
            return Result.ok(messages);
        } catch (Exception e) {
            log.error("获取消息列表失败", e);
            return Result.fail("获取消息失败：" + e.getMessage());
        }
    }

    @PostMapping("/send")
    public Result<Map<String, Object>> sendMessage(@RequestBody Map<String, Object> request) {
        try {
            Long userId = request.get("userId") != null ?
                    Long.valueOf(request.get("userId").toString()) : 1L;
            Long sessionId = Long.valueOf(request.get("sessionId").toString());
            String message = request.get("message").toString();

            String reply = chatService.sendMessage(userId, sessionId, message);

            return Result.ok(Map.of(
                    "reply", reply,
                    "sessionId", sessionId
            ));
        } catch (Exception e) {
            log.error("发送消息失败", e);
            return Result.fail("发送消息失败：" + e.getMessage());
        }
    }
}
