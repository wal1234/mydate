package com.example.scheduleservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.scheduleservice.entity.*;
import com.example.scheduleservice.mapper.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 垃圾箱业务服务
 *
 * <p>提供垃圾箱的统一管理功能，支持将日程、文档、笔记等移动到垃圾箱，
 * 并在30天后自动永久删除。用户可以恢复误删的项目。</p>
 *
 * <p><b>主要功能：</b></p>
 * <ul>
 *   <li>移动项目到垃圾箱</li>
 *   <li>恢复垃圾箱中的项目</li>
 *   <li>永久删除项目</li>
 *   <li>清空垃圾箱</li>
 *   <li>自动清理过期项目</li>
 *   <li>垃圾箱统计</li>
 * </ul>
 *
 * <p><b>保留策略：</b></p>
 * <ul>
 *   <li>默认保留期：30天</li>
 *   <li>每天凌晨2点自动清理过期项目</li>
 *   <li>支持手动清空垃圾箱</li>
 * </ul>
 *
 * @author Schedule Service
 * @version 1.0
 * @see TrashCleanupScheduler
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrashService extends ServiceImpl<TrashItemMapper, TrashItem> {

    private final TrashItemMapper trashItemMapper;
    private final ScheduleEventMapper scheduleEventMapper;
    private final DailyDocumentMapper dailyDocumentMapper;
    private final NoteMapper noteMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 垃圾箱保留天数 */
    private static final int RETENTION_DAYS = 30;

    /**
     * 移动项目到垃圾箱
     *
     * <p>将指定项目移动到垃圾箱，保存完整数据以便恢复。
     * 项目将在30天后自动永久删除。</p>
     *
     * <p>支持的类型：</p>
     * <ul>
     *   <li>schedule - 日程</li>
     *   <li>document - 文档</li>
     *   <li>note - 笔记</li>
     * </ul>
     *
     * @param userId       用户ID
     * @param itemType     项目类型
     * @param originalId   原始项目ID
     * @param title        项目标题
     * @param content      项目内容摘要
     * @param originalData  原始完整数据（用于恢复）
     * @return 创建的垃圾箱记录
     */
    @Transactional
    public TrashItem moveToTrash(Long userId, String itemType, Long originalId, 
                                  String title, String content, Object originalData) {
        log.debug("🗑️ 移动项目到垃圾箱 - 用户: {}, 类型: {}, 原始ID: {}", 
                userId, itemType, originalId);

        // 创建垃圾箱记录
        TrashItem item = new TrashItem();
        item.setUserId(userId);
        item.setItemType(itemType);
        item.setOriginalId(originalId);
        item.setItemTitle(title);
        item.setItemContent(content);
        
        // 序列化原始数据
        try {
            item.setOriginalData(objectMapper.writeValueAsString(originalData));
        } catch (Exception e) {
            log.error("❌ 序列化原始数据失败 - 原始ID: {}", originalId, e);
            item.setOriginalData("{}");
        }
        
        // 设置时间戳
        item.setDeletedAt(LocalDateTime.now());
        item.setExpireAt(LocalDateTime.now().plusDays(RETENTION_DAYS));
        item.setStatus(0);
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        
        // 保存到数据库
        trashItemMapper.insert(item);
        
        log.info("✅ 项目已移动到垃圾箱 - ID: {}, 类型: {}, 标题: {}, 过期时间: {}", 
                item.getId(), itemType, title, item.getExpireAt());
        
        return item;
    }

    /**
     * 获取用户垃圾箱中的所有项目
     *
     * @param userId 用户ID
     * @return 垃圾箱项目列表，按删除时间倒序
     */
    public List<TrashItem> getUserTrashItems(Long userId) {
        log.debug("📋 查询用户垃圾箱 - 用户ID: {}", userId);
        
        QueryWrapper<TrashItem> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .eq("status", 0)
                .orderByDesc("deleted_at");
        
        List<TrashItem> items = trashItemMapper.selectList(wrapper);
        
        log.info("✅ 用户垃圾箱查询完成 - 用户ID: {}, 共 {} 条", userId, items.size());
        
        return items;
    }

    /**
     * 按类型获取垃圾箱项目
     *
     * @param userId   用户ID
     * @param itemType 项目类型
     * @return 该类型的垃圾箱项目列表
     */
    public List<TrashItem> getTrashItemsByType(Long userId, String itemType) {
        log.debug("📋 按类型查询垃圾箱 - 用户ID: {}, 类型: {}", userId, itemType);
        
        QueryWrapper<TrashItem> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .eq("item_type", itemType)
                .eq("status", 0)
                .orderByDesc("deleted_at");
        
        List<TrashItem> items = trashItemMapper.selectList(wrapper);
        
        log.info("✅ 按类型查询完成 - 用户ID: {}, 类型: {}, 共 {} 条", userId, itemType, items.size());
        
        return items;
    }

    /**
     * 根据ID获取垃圾箱项目
     *
     * @param trashId 垃圾箱记录ID
     * @return 垃圾箱项目
     */
    public TrashItem getTrashItem(Long trashId) {
        return trashItemMapper.selectById(trashId);
    }

    /**
     * 恢复垃圾箱中的项目
     *
     * <p>根据项目类型，从原始数据恢复项目到对应表。
     * 恢复后，该垃圾箱记录状态变为"已恢复"。</p>
     *
     * @param trashId 垃圾箱记录ID
     * @return 是否恢复成功
     */
    @Transactional
    public boolean restore(Long trashId) {
        log.debug("♻️ 恢复垃圾箱项目 - ID: {}", trashId);
        
        TrashItem item = trashItemMapper.selectById(trashId);
        if (item == null) {
            log.warn("❌ 恢复失败：垃圾箱项目不存在 - ID: {}", trashId);
            return false;
        }

        try {
            switch (item.getItemType()) {
                case "schedule":
                    restoreSchedule(item);
                    break;
                case "document":
                    restoreDocument(item);
                    break;
                case "note":
                    restoreNote(item);
                    break;
                default:
                    log.warn("❌ 恢复失败：未知项目类型 - {}", item.getItemType());
                    return false;
            }

            // 更新垃圾箱记录状态
            item.setStatus(1);
            item.setUpdatedAt(LocalDateTime.now());
            trashItemMapper.updateById(item);
            
            log.info("✅ 项目恢复成功 - 垃圾箱ID: {}, 类型: {}, 标题: {}", 
                    trashId, item.getItemType(), item.getItemTitle());
            
            return true;
        } catch (Exception e) {
            log.error("❌ 项目恢复失败 - 垃圾箱ID: {}", trashId, e);
            return false;
        }
    }

    /**
     * 恢复日程
     */
    private void restoreSchedule(TrashItem item) throws Exception {
        ScheduleEvent event = objectMapper.readValue(item.getOriginalData(), ScheduleEvent.class);
        event.setId(null);
        event.setDeleted(0);
        scheduleEventMapper.insert(event);
        log.debug("📅 日程已恢复 - 原ID: {}", item.getOriginalId());
    }

    /**
     * 恢复文档
     */
    private void restoreDocument(TrashItem item) throws Exception {
        DailyDocument doc = objectMapper.readValue(item.getOriginalData(), DailyDocument.class);
        doc.setId(null);
        doc.setDeleted(0);
        dailyDocumentMapper.insert(doc);
        log.debug("📄 文档已恢复 - 原ID: {}", item.getOriginalId());
    }

    /**
     * 恢复笔记
     */
    private void restoreNote(TrashItem item) throws Exception {
        Note note = objectMapper.readValue(item.getOriginalData(), Note.class);
        note.setId(null);
        note.setDeleted(0);
        noteMapper.insert(note);
        log.debug("📝 笔记已恢复 - 原ID: {}", item.getOriginalId());
    }

    /**
     * 永久删除垃圾箱项目
     *
     * <p>将垃圾箱记录标记为"已永久删除"，但不删除原始数据。</p>
     *
     * @param trashId 垃圾箱记录ID
     * @return 是否删除成功
     */
    @Transactional
    public boolean permanentDelete(Long trashId) {
        log.debug("🗑️ 永久删除垃圾箱项目 - ID: {}", trashId);
        
        TrashItem item = trashItemMapper.selectById(trashId);
        if (item == null) {
            log.warn("❌ 永久删除失败：项目不存在 - ID: {}", trashId);
            return false;
        }

        // 标记为已永久删除
        item.setStatus(2);
        item.setUpdatedAt(LocalDateTime.now());
        trashItemMapper.updateById(item);
        
        log.info("✅ 项目已永久删除 - ID: {}, 类型: {}, 标题: {}", 
                trashId, item.getItemType(), item.getItemTitle());
        
        return true;
    }

    /**
     * 清理过期项目
     *
     * <p>自动清理超过30天的垃圾箱项目。
     * 此方法由定时任务调用。</p>
     *
     * @return 清理的项目数量
     */
    @Transactional
    public int cleanupExpiredItems() {
        log.info("🧹 开始清理过期垃圾箱项目...");
        
        QueryWrapper<TrashItem> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 0)
                .le("expire_at", LocalDateTime.now());
        
        List<TrashItem> expiredItems = trashItemMapper.selectList(wrapper);
        
        // 标记为已过期
        for (TrashItem item : expiredItems) {
            item.setStatus(2);
            item.setUpdatedAt(LocalDateTime.now());
            trashItemMapper.updateById(item);
        }
        
        log.info("✅ 过期项目清理完成 - 共清理 {} 条", expiredItems.size());
        
        return expiredItems.size();
    }

    /**
     * 清空用户垃圾箱
     *
     * <p>将用户的所有垃圾箱项目标记为"已永久删除"。</p>
     *
     * @param userId 用户ID
     * @return 删除的项目数量
     */
    @Transactional
    public int emptyTrash(Long userId) {
        log.debug("🗑️ 清空用户垃圾箱 - 用户ID: {}", userId);
        
        QueryWrapper<TrashItem> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .eq("status", 0);
        
        List<TrashItem> items = trashItemMapper.selectList(wrapper);
        
        // 标记为已删除
        for (TrashItem item : items) {
            item.setStatus(2);
            item.setUpdatedAt(LocalDateTime.now());
            trashItemMapper.updateById(item);
        }
        
        log.info("✅ 用户垃圾箱已清空 - 用户ID: {}, 删除 {} 条", userId, items.size());
        
        return items.size();
    }

    /**
     * 获取垃圾箱项目数量
     *
     * @param userId 用户ID
     * @return 项目数量
     */
    public long getTrashCount(Long userId) {
        QueryWrapper<TrashItem> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .eq("status", 0);
        
        return trashItemMapper.selectCount(wrapper);
    }

    /**
     * 获取垃圾箱统计信息
     *
     * <p>返回各类型项目的统计数量。</p>
     *
     * @param userId 用户ID
     * @return 统计信息Map，包含总数和各类型数量
     */
    public Map<String, Long> getTrashStats(Long userId) {
        log.debug("📊 获取垃圾箱统计 - 用户ID: {}", userId);
        
        QueryWrapper<TrashItem> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .eq("status", 0);
        
        List<TrashItem> items = trashItemMapper.selectList(wrapper);
        
        // 统计各类型数量
        Map<String, Long> stats = new java.util.HashMap<>();
        stats.put("total", (long) items.size());
        stats.put("schedules", items.stream().filter(i -> "schedule".equals(i.getItemType())).count());
        stats.put("documents", items.stream().filter(i -> "document".equals(i.getItemType())).count());
        stats.put("notes", items.stream().filter(i -> "note".equals(i.getItemType())).count());
        
        log.info("✅ 垃圾箱统计完成 - 用户ID: {}, 总数: {}", userId, stats.get("total"));
        
        return stats;
    }
}
