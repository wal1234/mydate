package com.example.scheduleservice.controller;

import com.example.common.result.Result;
import com.example.scheduleservice.entity.TrashItem;
import com.example.scheduleservice.service.TrashService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/trash")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TrashController {

    private final TrashService trashService;

    @GetMapping
    public Result<List<TrashItem>> getTrashItems(@RequestParam(required = false) Long userId) {
        try {
            if (userId == null) {
                userId = 1L;
            }
            List<TrashItem> items = trashService.getUserTrashItems(userId);
            return Result.ok(items);
        } catch (Exception e) {
            log.error("获取垃圾箱列表失败", e);
            return Result.fail("获取垃圾箱列表失败：" + e.getMessage());
        }
    }

    @GetMapping("/by-type")
    public Result<List<TrashItem>> getTrashItemsByType(
            @RequestParam(required = false) Long userId,
            @RequestParam String itemType) {
        try {
            if (userId == null) {
                userId = 1L;
            }
            List<TrashItem> items = trashService.getTrashItemsByType(userId, itemType);
            return Result.ok(items);
        } catch (Exception e) {
            log.error("获取垃圾箱列表失败", e);
            return Result.fail("获取垃圾箱列表失败：" + e.getMessage());
        }
    }

    @GetMapping("/{trashId}")
    public Result<TrashItem> getTrashItem(@PathVariable Long trashId) {
        try {
            TrashItem item = trashService.getTrashItem(trashId);
            if (item == null) {
                return Result.fail("垃圾箱项目不存在");
            }
            return Result.ok(item);
        } catch (Exception e) {
            log.error("获取垃圾箱项目失败", e);
            return Result.fail("获取垃圾箱项目失败：" + e.getMessage());
        }
    }

    @PostMapping("/{trashId}/restore")
    public Result<Void> restoreItem(@PathVariable Long trashId) {
        try {
            boolean success = trashService.restore(trashId);
            if (success) {
                return Result.ok(null);
            } else {
                return Result.fail("恢复失败");
            }
        } catch (Exception e) {
            log.error("恢复项目失败", e);
            return Result.fail("恢复项目失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{trashId}")
    public Result<Void> permanentDelete(@PathVariable Long trashId) {
        try {
            boolean success = trashService.permanentDelete(trashId);
            if (success) {
                return Result.ok(null);
            } else {
                return Result.fail("删除失败");
            }
        } catch (Exception e) {
            log.error("永久删除失败", e);
            return Result.fail("永久删除失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/empty")
    public Result<Map<String, Integer>> emptyTrash(@RequestParam(required = false) Long userId) {
        try {
            if (userId == null) {
                userId = 1L;
            }
            int count = trashService.emptyTrash(userId);
            return Result.ok(Map.of("deletedCount", count));
        } catch (Exception e) {
            log.error("清空垃圾箱失败", e);
            return Result.fail("清空垃圾箱失败：" + e.getMessage());
        }
    }

    @GetMapping("/count")
    public Result<Map<String, Long>> getTrashCount(@RequestParam(required = false) Long userId) {
        try {
            if (userId == null) {
                userId = 1L;
            }
            long count = trashService.getTrashCount(userId);
            return Result.ok(Map.of("count", count));
        } catch (Exception e) {
            log.error("获取垃圾箱数量失败", e);
            return Result.fail("获取垃圾箱数量失败：" + e.getMessage());
        }
    }

    @GetMapping("/stats")
    public Result<Map<String, Long>> getTrashStats(@RequestParam(required = false) Long userId) {
        try {
            if (userId == null) {
                userId = 1L;
            }
            Map<String, Long> stats = trashService.getTrashStats(userId);
            return Result.ok(stats);
        } catch (Exception e) {
            log.error("获取垃圾箱统计失败", e);
            return Result.fail("获取垃圾箱统计失败：" + e.getMessage());
        }
    }

    @PostMapping("/cleanup")
    public Result<Map<String, Integer>> cleanupExpired() {
        try {
            int count = trashService.cleanupExpiredItems();
            return Result.ok(Map.of("cleanedCount", count));
        } catch (Exception e) {
            log.error("清理过期项目失败", e);
            return Result.fail("清理过期项目失败：" + e.getMessage());
        }
    }
}
