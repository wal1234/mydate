package com.example.scheduleservice.controller;

import com.example.common.result.Result;
import com.example.scheduleservice.entity.TrashItem;
import com.example.scheduleservice.service.TrashService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 垃圾箱 REST API 控制器
 *
 * <p>提供垃圾箱的统一管理功能，支持恢复、永久删除、清空等操作。</p>
 *
 * @author Schedule Service
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/trash")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TrashController {

    private final TrashService trashService;

    /**
     * 获取垃圾箱列表
     *
     * @param userId 用户ID
     * @return 垃圾箱项目列表
     */
    @GetMapping
    public Result<List<TrashItem>> getTrashItems(
            @RequestParam(required = false, defaultValue = "1") Long userId) {

        log.info("🗑️ 【垃圾箱列表】获取垃圾箱列表 - 用户ID: {}", userId);

        try {
            List<TrashItem> items = trashService.getUserTrashItems(userId);
            log.info("✅ 【垃圾箱列表】获取成功 - 共 {} 条记录", items.size());
            return Result.ok(items);

        } catch (Exception e) {
            log.error("❌ 【垃圾箱列表】获取失败 - 用户ID: {}", userId, e);
            return Result.fail("获取垃圾箱列表失败：" + e.getMessage());
        }
    }

    /**
     * 按类型获取垃圾箱项目
     *
     * @param userId   用户ID
     * @param itemType 项目类型（schedule/document/note）
     * @return 该类型的垃圾箱项目列表
     */
    @GetMapping("/by-type")
    public Result<List<TrashItem>> getTrashItemsByType(
            @RequestParam(required = false, defaultValue = "1") Long userId,
            @RequestParam String itemType) {

        log.info("🗑️ 【按类型查询】查询垃圾箱 - 用户ID: {}, 类型: {}", userId, itemType);

        try {
            List<TrashItem> items = trashService.getTrashItemsByType(userId, itemType);
            log.info("✅ 【按类型查询】获取成功 - 类型: {}, 共 {} 条", itemType, items.size());
            return Result.ok(items);

        } catch (Exception e) {
            log.error("❌ 【按类型查询】获取失败", e);
            return Result.fail("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取垃圾箱项目详情
     *
     * @param trashId 垃圾箱记录ID
     * @return 垃圾箱项目详情
     */
    @GetMapping("/{trashId}")
    public Result<TrashItem> getTrashItem(@PathVariable Long trashId) {
        log.info("🗑️ 【垃圾箱详情】获取详情 - ID: {}", trashId);

        try {
            TrashItem item = trashService.getTrashItem(trashId);
            if (item == null) {
                log.warn("   ⚠️ 项目不存在: ID={}", trashId);
                return Result.fail("项目不存在");
            }

            log.info("✅ 【垃圾箱详情】获取成功 - ID: {}, 标题: {}", trashId, item.getItemTitle());
            return Result.ok(item);

        } catch (Exception e) {
            log.error("❌ 【垃圾箱详情】获取失败 - ID: {}", trashId, e);
            return Result.fail("获取详情失败：" + e.getMessage());
        }
    }

    /**
     * 恢复项目
     *
     * <p>将垃圾箱中的项目恢复到原位置。</p>
     *
     * @param trashId 垃圾箱记录ID
     * @return 操作结果
     */
    @PostMapping("/{trashId}/restore")
    public Result<?> restoreItem(@PathVariable Long trashId) {
        log.info("♻️ 【恢复项目】开始恢复 - ID: {}", trashId);

        try {
            boolean success = trashService.restore(trashId);
            if (success) {
                log.info("✅ 【恢复项目】恢复成功 - ID: {}", trashId);
                return Result.ok(null);
            } else {
                log.warn("❌ 【恢复项目】恢复失败 - ID: {}", trashId);
                return Result.fail("恢复失败");
            }

        } catch (Exception e) {
            log.error("❌ 【恢复项目】恢复失败 - ID: {}", trashId, e);
            return Result.fail("恢复失败：" + e.getMessage());
        }
    }

    /**
     * 永久删除项目
     *
     * <p>立即永久删除垃圾箱中的项目，此操作不可恢复。</p>
     *
     * @param trashId 垃圾箱记录ID
     * @return 操作结果
     */
    @DeleteMapping("/{trashId}")
    public Result<?> permanentDelete(@PathVariable Long trashId) {
        log.info("⚠️ 【永久删除】开始永久删除 - ID: {}", trashId);

        try {
            boolean success = trashService.permanentDelete(trashId);
            if (success) {
                log.info("✅ 【永久删除】删除成功 - ID: {}", trashId);
                return Result.ok(null);
            } else {
                log.warn("❌ 【永久删除】删除失败 - ID: {}", trashId);
                return Result.fail("删除失败");
            }

        } catch (Exception e) {
            log.error("❌ 【永久删除】删除失败 - ID: {}", trashId, e);
            return Result.fail("删除失败：" + e.getMessage());
        }
    }

    /**
     * 清空垃圾箱
     *
     * <p>清空用户的所有垃圾箱项目，此操作不可恢复。</p>
     *
     * @param userId 用户ID
     * @return 删除的项目数量
     */
    @DeleteMapping("/empty")
    public Result<Map<String, Integer>> emptyTrash(
            @RequestParam(required = false, defaultValue = "1") Long userId) {

        log.info("⚠️ 【清空垃圾箱】开始清空 - 用户ID: {}", userId);

        try {
            int count = trashService.emptyTrash(userId);
            log.info("✅ 【清空垃圾箱】清空成功 - 删除 {} 条记录", count);
            return Result.ok(Map.of("deletedCount", count));

        } catch (Exception e) {
            log.error("❌ 【清空垃圾箱】清空失败 - 用户ID: {}", userId, e);
            return Result.fail("清空失败：" + e.getMessage());
        }
    }

    /**
     * 获取垃圾箱数量
     *
     * @param userId 用户ID
     * @return 垃圾箱项目数量
     */
    @GetMapping("/count")
    public Result<Map<String, Long>> getTrashCount(
            @RequestParam(required = false, defaultValue = "1") Long userId) {

        log.debug("📊 【垃圾箱数量】获取数量 - 用户ID: {}", userId);

        try {
            long count = trashService.getTrashCount(userId);
            log.debug("   └─ 数量: {}", count);
            return Result.ok(Map.of("count", count));

        } catch (Exception e) {
            log.error("❌ 【垃圾箱数量】获取失败", e);
            return Result.fail("获取数量失败");
        }
    }

    /**
     * 获取垃圾箱统计
     *
     * <p>返回各类型项目的统计数量。</p>
     *
     * @param userId 用户ID
     * @return 统计信息
     */
    @GetMapping("/stats")
    public Result<Map<String, Long>> getTrashStats(
            @RequestParam(required = false, defaultValue = "1") Long userId) {

        log.info("📊 【垃圾箱统计】获取统计 - 用户ID: {}", userId);

        try {
            Map<String, Long> stats = trashService.getTrashStats(userId);
            log.info("✅ 【垃圾箱统计】获取成功 - 总数: {}", stats.get("total"));
            return Result.ok(stats);

        } catch (Exception e) {
            log.error("❌ 【垃圾箱统计】获取失败", e);
            return Result.fail("获取统计失败");
        }
    }

    /**
     * 清理过期项目（管理员接口）
     *
     * <p>清理超过30天的垃圾箱项目。此接口由定时任务调用。</p>
     *
     * @return 清理的项目数量
     */
    @PostMapping("/cleanup")
    public Result<Map<String, Integer>> cleanupExpired() {
        log.info("🧹 【清理过期】开始清理过期项目");

        try {
            int count = trashService.cleanupExpiredItems();
            log.info("✅ 【清理过期】清理完成 - 清理 {} 条记录", count);
            return Result.ok(Map.of("cleanedCount", count));

        } catch (Exception e) {
            log.error("❌ 【清理过期】清理失败", e);
            return Result.fail("清理失败：" + e.getMessage());
        }
    }
}
