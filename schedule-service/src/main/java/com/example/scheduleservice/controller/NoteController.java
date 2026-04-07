package com.example.scheduleservice.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.common.result.Result;
import com.example.scheduleservice.entity.Note;
import com.example.scheduleservice.service.NoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 笔记 REST API 控制器
 *
 * <p>提供笔记的完整CRUD操作，支持分类查询、搜索、置顶等功能。</p>
 *
 * <p><b>接口列表：</b></p>
 * <ul>
 *   <li>GET /api/notes - 查询笔记列表（分页）</li>
 *   <li>GET /api/notes/{id} - 获取笔记详情</li>
 *   <li>POST /api/notes - 创建笔记</li>
 *   <li>PUT /api/notes/{id} - 更新笔记</li>
 *   <li>DELETE /api/notes/{id} - 删除笔记（移至垃圾箱）</li>
 *   <li>POST /api/notes/{id}/pin - 切换置顶状态</li>
 * </ul>
 *
 * @author Schedule Service
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NoteController {

    private final NoteService noteService;

    /**
     * 查询笔记列表（分页）
     *
     * <p>支持按内容类型、分类、关键词筛选。</p>
     *
     * @param contentType 内容类型（quick/rich，可选）
     * @param categoryId 分类ID（可选）
     * @param keyword    搜索关键词（可选）
     * @param page       页码（默认1）
     * @param size       每页数量（默认20）
     * @return 分页后的笔记列表
     */
    @GetMapping
    public Result<IPage<Note>> list(
            @RequestParam(required = false) String contentType,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("📝 【笔记查询】开始查询笔记列表");
        log.debug("   ├─ 类型: {}", contentType);
        log.debug("   ├─ 分类: {}", categoryId);
        log.debug("   ├─ 关键词: {}", keyword);
        log.debug("   ├─ 分页: 第 {} 页，每页 {} 条", page, size);

        try {
            IPage<Note> result = noteService.list(contentType, categoryId, keyword, page, size);

            log.info("✅ 【笔记查询】查询成功 - 共 {} 条记录", result.getTotal());

            return Result.ok(result);

        } catch (Exception e) {
            log.error("❌ 【笔记查询】查询失败", e);
            return Result.fail("查询笔记列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取笔记详情
     *
     * @param id 笔记ID
     * @return 笔记详情
     */
    @GetMapping("/{id}")
    public Result<Note> getById(@PathVariable Long id) {
        log.info("📝 【笔记详情】查询笔记详情 - ID: {}", id);

        try {
            Note note = noteService.getById(id);

            if (note == null) {
                log.warn("   ⚠️ 笔记不存在: ID={}", id);
                return Result.fail("笔记不存在");
            }

            log.info("✅ 【笔记详情】查询成功 - ID: {}, 标题: {}", id, note.getTitle());

            return Result.ok(note);

        } catch (Exception e) {
            log.error("❌ 【笔记详情】查询失败 - ID: {}", id, e);
            return Result.fail("查询笔记详情失败：" + e.getMessage());
        }
    }

    /**
     * 创建笔记
     *
     * <p><b>请求体：</b></p>
     * <pre>
     * {
     *   "title": "笔记标题",
     *   "content": "笔记内容...",
     *   "contentType": "quick",
     *   "categoryId": 1,
     *   "color": "#fef3c7"
     * }
     * </pre>
     *
     * @param note 笔记信息
     * @return 创建成功后的笔记对象
     */
    @PostMapping
    public Result<Note> create(@RequestBody Note note) {
        log.info("✨ 【创建笔记】开始创建笔记");
        log.debug("   ├─ 标题: {}", note.getTitle());
        log.debug("   ├─ 类型: {}", note.getContentType());
        log.debug("   ├─ 分类: {}", note.getCategoryId());

        try {
            Note created = noteService.create(note);

            log.info("✅ 【创建笔记】创建成功 - ID: {}, 标题: {}", created.getId(), created.getTitle());

            return Result.ok(created);

        } catch (Exception e) {
            log.error("❌ 【创建笔记】创建失败", e);
            return Result.fail("创建笔记失败：" + e.getMessage());
        }
    }

    /**
     * 更新笔记
     *
     * @param id   笔记ID
     * @param note 新的笔记数据
     * @return 更新后的笔记对象
     */
    @PutMapping("/{id}")
    public Result<Note> update(@PathVariable Long id, @RequestBody Note note) {
        log.info("✏️ 【更新笔记】开始更新笔记 - ID: {}", id);

        try {
            Note updated = noteService.update(id, note);

            log.info("✅ 【更新笔记】更新成功 - ID: {}", id);

            return Result.ok(updated);

        } catch (Exception e) {
            log.error("❌ 【更新笔记】更新失败 - ID: {}", id, e);
            return Result.fail("更新笔记失败：" + e.getMessage());
        }
    }

    /**
     * 删除笔记
     *
     * <p>将笔记移动到垃圾箱，支持30天内恢复。</p>
     *
     * @param id     笔记ID
     * @param userId 用户ID（可选）
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id,
                           @RequestParam(required = false, defaultValue = "1") Long userId) {

        log.info("🗑️ 【删除笔记】开始删除笔记 - ID: {}, 用户ID: {}", id, userId);

        try {
            noteService.delete(id, userId);

            log.info("✅ 【删除笔记】删除成功 - ID: {}", id);

            return Result.ok(null);

        } catch (Exception e) {
            log.error("❌ 【删除笔记】删除失败 - ID: {}", id, e);
            return Result.fail("删除笔记失败：" + e.getMessage());
        }
    }

    /**
     * 切换置顶状态
     *
     * <p>如果笔记已置顶，则取消置顶；如果未置顶，则设为置顶。</p>
     *
     * @param id 笔记ID
     * @return 更新后的笔记对象
     */
    @PostMapping("/{id}/pin")
    public Result<Note> togglePin(@PathVariable Long id) {
        log.info("📌 【切换置顶】切换笔记置顶状态 - ID: {}", id);

        try {
            Note note = noteService.togglePin(id);

            boolean isPinned = note != null && note.getIsPinned() == 1;
            log.info("✅ 【切换置顶】切换成功 - ID: {}, 置顶: {}", id, isPinned);

            return Result.ok(note);

        } catch (Exception e) {
            log.error("❌ 【切换置顶】切换失败 - ID: {}", id, e);
            return Result.fail("切换置顶状态失败：" + e.getMessage());
        }
    }
}
