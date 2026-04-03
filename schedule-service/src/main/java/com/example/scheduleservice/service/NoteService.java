package com.example.scheduleservice.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.scheduleservice.entity.Note;
import com.example.scheduleservice.mapper.NoteMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 笔记业务服务
 *
 * <p>提供笔记的完整CRUD操作，支持分类、置顶、搜索等功能。
 * 笔记分为快速笔记和富文本笔记两种类型。</p>
 *
 * <p><b>主要功能：</b></p>
 * <ul>
 *   <li>笔记创建、查询、更新、删除</li>
 *   <li>按分类查询笔记</li>
 *   <li>按类型查询笔记</li>
 *   <li>关键词搜索</li>
 *   <li>笔记置顶功能</li>
 *   <li>支持软删除到垃圾箱</li>
 * </ul>
 *
 * @author Schedule Service
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteMapper noteMapper;
    private final TrashService trashService;

    /**
     * 分页查询笔记列表
     *
     * <p>支持按内容类型、分类、关键词筛选笔记，并按置顶状态和更新时间排序。</p>
     *
     * @param contentType 内容类型（quick/rich）
     * @param categoryId  分类ID
     * @param keyword     搜索关键词（搜索标题和内容）
     * @param page        页码
     * @param size        每页数量
     * @return 分页结果
     */
    public IPage<Note> list(String contentType, Long categoryId, String keyword, int page, int size) {
        log.debug("📝 查询笔记列表 - 类型: {}, 分类: {}, 关键词: {}, 页码: {}", 
                contentType, categoryId, keyword, page);

        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getDeleted, 0);
        
        // 按类型筛选
        if (StringUtils.hasText(contentType)) {
            wrapper.eq(Note::getContentType, contentType);
        }
        
        // 按分类筛选
        if (categoryId != null) {
            wrapper.eq(Note::getCategoryId, categoryId);
        }
        
        // 关键词搜索
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Note::getTitle, keyword).or().like(Note::getContent, keyword));
        }
        
        // 排序：置顶优先，然后按更新时间倒序
        wrapper.orderByDesc(Note::getIsPinned);
        wrapper.orderByDesc(Note::getUpdatedAt);
        
        IPage<Note> result = noteMapper.selectPage(new Page<>(page, size), wrapper);
        
        log.info("✅ 笔记列表查询完成 - 共 {} 条", result.getTotal());
        
        return result;
    }

    /**
     * 根据内容类型查询笔记
     *
     * @param contentType 内容类型
     * @return 笔记列表
     */
    public List<Note> findByContentType(String contentType) {
        log.debug("📝 按类型查询笔记 - 类型: {}", contentType);
        
        List<Note> notes = noteMapper.findByContentType(contentType);
        
        log.info("✅ 按类型查询完成 - 类型: {}, 共 {} 条", contentType, notes.size());
        
        return notes;
    }

    /**
     * 根据分类查询笔记
     *
     * @param categoryId 分类ID
     * @return 笔记列表
     */
    public List<Note> findByCategory(Long categoryId) {
        log.debug("📂 按分类查询笔记 - 分类ID: {}", categoryId);
        
        List<Note> notes = noteMapper.findByCategory(categoryId);
        
        log.info("✅ 按分类查询完成 - 分类ID: {}, 共 {} 条", categoryId, notes.size());
        
        return notes;
    }

    /**
     * 根据ID查询笔记
     *
     * @param id 笔记ID
     * @return 笔记对象
     */
    public Note getById(Long id) {
        if (id == null) {
            log.warn("⚠️ 查询笔记失败：ID为空");
            return null;
        }
        
        Note note = noteMapper.selectById(id);
        
        if (note == null) {
            log.debug("🔍 查询笔记详情 - ID: {} 未找到", id);
        } else {
            log.debug("🔍 查询笔记详情 - ID: {}, 标题: {}", id, note.getTitle());
        }
        
        return note;
    }

    /**
     * 创建笔记
     *
     * @param note 笔记对象
     * @return 创建后的笔记
     */
    public Note create(Note note) {
        log.debug("✏️ 创建笔记 - 标题: {}", note.getTitle());
        
        noteMapper.insert(note);
        
        log.info("✅ 笔记创建成功 - ID: {}, 标题: {}", note.getId(), note.getTitle());
        
        return note;
    }

    /**
     * 更新笔记
     *
     * @param id   笔记ID
     * @param note 新的笔记数据
     * @return 更新后的笔记
     */
    public Note update(Long id, Note note) {
        log.debug("✏️ 更新笔记 - ID: {}", id);
        
        note.setId(id);
        noteMapper.updateById(note);
        
        log.info("✅ 笔记更新成功 - ID: {}, 标题: {}", id, note.getTitle());
        
        return noteMapper.selectById(id);
    }

    /**
     * 移动笔记到垃圾箱
     *
     * <p>将笔记移动到垃圾箱，支持30天内恢复。</p>
     *
     * @param id     笔记ID
     * @param userId 用户ID
     */
    public void delete(Long id, Long userId) {
        log.debug("🗑️ 移动笔记到垃圾箱 - ID: {}", id);
        
        // 查询要删除的笔记
        Note existing = noteMapper.selectById(id);
        if (existing == null) {
            log.warn("🗑️ 删除笔记失败：笔记不存在 - ID: {}", id);
            return;
        }
        
        // 保存标题用于日志
        String title = existing.getTitle();
        
        // 移动到垃圾箱
        trashService.moveToTrash(
                userId,
                "note",
                id,
                title,
                existing.getContent(),
                existing
        );
        
        // 标记为已删除
        existing.setDeleted(1);
        noteMapper.updateById(existing);
        
        log.info("✅ 笔记已移动到垃圾箱 - ID: {}, 标题: {}, 保留期: 30天", id, title);
    }

    /**
     * 切换笔记置顶状态
     *
     * <p>如果笔记已置顶，则取消置顶；如果未置顶，则设为置顶。</p>
     *
     * @param id 笔记ID
     * @return 更新后的笔记
     */
    public Note togglePin(Long id) {
        log.debug("📌 切换笔记置顶状态 - ID: {}", id);
        
        Note note = noteMapper.selectById(id);
        if (note != null) {
            note.setIsPinned(note.getIsPinned() == 1 ? 0 : 1);
            noteMapper.updateById(note);
            log.info("✅ 笔记置顶状态切换成功 - ID: {}, 置顶: {}", id, note.getIsPinned() == 1);
        }
        
        return note;
    }
}
