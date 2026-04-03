package com.example.scheduleservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class NotificationService {

    @Value("${notification.serverchan.url:https://sctapi.ftqq.com}")
    private String serverChanUrl;

    @Value("${notification.serverchan.key:}")
    private String serverChanKey;

    @Value("${notification.qmsg.url:https://qmsg.zendee.cn}")
    private String qmsgUrl;

    @Value("${notification.qmsg.key:}")
    private String qmsgKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean sendWechatNotification(String title, String content) {
        if (serverChanKey == null || serverChanKey.isEmpty()) {
            log.warn("ServerChan key is not configured, skip wechat notification");
            return false;
        }

        try {
            String url = serverChanUrl + "/" + serverChanKey + ".send";
            Map<String, String> params = new HashMap<>();
            params.put("title", title);
            params.put("desp", content);

            restTemplate.postForEntity(url, params, String.class);
            log.info("Wechat notification sent successfully: {}", title);
            return true;
        } catch (Exception e) {
            log.error("Failed to send wechat notification: {}", e.getMessage());
            return false;
        }
    }

    public boolean sendQQNotification(String qq, String message) {
        if (qmsgKey == null || qmsgKey.isEmpty()) {
            log.warn("Qmsg key is not configured, skip QQ notification");
            return false;
        }

        if (qq == null || qq.isEmpty()) {
            log.warn("QQ number is not configured, skip QQ notification");
            return false;
        }

        try {
            String url = qmsgUrl + "/send/" + qmsgKey;
            Map<String, String> params = new HashMap<>();
            params.put("msg", message);
            params.put("qq", qq);

            restTemplate.postForEntity(url, params, String.class);
            log.info("QQ notification sent successfully to {}: {}", qq, message);
            return true;
        } catch (Exception e) {
            log.error("Failed to send QQ notification: {}", e.getMessage());
            return false;
        }
    }

    public boolean sendScheduleReminder(String title, String description, String startTime, String wechatKey, String qq) {
        String message = String.format("【日程提醒】\n📌 %s\n📝 %s\n🕐 %s", title, description != null ? description : "无", startTime);

        boolean wechatResult = false;
        boolean qqResult = false;

        if (wechatKey != null && !wechatKey.isEmpty()) {
            serverChanKey = wechatKey;
            wechatResult = sendWechatNotification(title, message);
        }

        if (qq != null && !qq.isEmpty()) {
            qqResult = sendQQNotification(qq, message);
        }

        return wechatResult || qqResult;
    }
}
