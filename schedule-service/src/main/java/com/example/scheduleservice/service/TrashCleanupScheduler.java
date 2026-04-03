package com.example.scheduleservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrashCleanupScheduler {

    private final TrashService trashService;

    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredTrashItems() {
        log.info("Starting scheduled trash cleanup...");
        try {
            int count = trashService.cleanupExpiredItems();
            log.info("Trash cleanup completed. {} items cleaned.", count);
        } catch (Exception e) {
            log.error("Trash cleanup failed", e);
        }
    }
}
