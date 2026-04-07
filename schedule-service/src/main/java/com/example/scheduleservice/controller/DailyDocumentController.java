package com.example.scheduleservice.controller;

import com.example.common.result.PageResult;
import com.example.common.result.Result;
import com.example.scheduleservice.entity.DailyDocument;
import com.example.scheduleservice.service.DailyDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 每日文档 REST API 控制器
 *
 * <p>提供每日文档的完整CRUD操作，支持按日期查询、按日期区间查询等功能。</p>
 *
 * <p><b>接口列表：</b></p>
 * <ul>
 *   <li>GET /api/daily-docs - 查询文档列表（分页）</li>
 *   <li>GET /api/daily-docs/{id} - 获取文档详情</li>
 *   <li>GET /api/daily-docs/date/{date} - 按日期查询文档</li>
 *   <li>POST /api/daily-docs - 创建文档</li>
 *   <li>PUT /api/daily-docs/{id} - 更新文档</li>
 *   <li>DELETE /api/daily-docs/{id} - 删除文档（移至垃圾箱）</li>
 * </ul>
 *
 * @author Schedule Service
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/daily-docs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DailyDocumentController {

    private final DailyDocumentService dailyDocumentService;

    /**
     * 查询文档列表（分页）
     *
     * <p>根据日期范围查询文档，支持分页返回。</p>
     *
     * @param from 起始日期（必填）
     * @param to   结束日期（必填）
     * @param page 页码（默认1）
     * @param size 每页数量（默认20）
     * @return 分页后的文档列表
     *
     * @example
     * GET /api/daily-docs?from=2024-01-01&to=2024-12-31&page=1&size=20
     */
    @GetMapping
    public Result<PageResult<DailyDocument>> list(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("📄 【文档查询】开始查询文档列表");
        log.debug("   ├─ 日期范围: {} ~ {}", from, to);
        log.debug("   ├─ 分页: 第 {} 页，每页 {} 条", page, size);

        try {
            // 参数验证
            if (from.isAfter(to)) {
                log.warn("   ⚠️ 参数错误: 起始日期晚于结束日期");
                return Result.fail("起始日期不能晚于结束日期");
            }

            // 调用服务层查询
            PageResult<DailyDocument> result = dailyDocumentService.listByDateRangePaged(from, to, page, size);

            log.info("✅ 【文档查询】查询成功 - 共 {} 条记录", result.getTotal());

            return Result.ok(result);

        } catch (Exception e) {
            log.error("❌ 【文档查询】查询失败 - from={}, to={}", from, to, e);
            return Result.fail("查询文档列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取文档详情
     *
     * <p>根据ID获取单个文档的详细信息。</p>
     *
     * @param id 文档ID
     * @return 文档详情
     *
     * @example
     * GET /api/daily-docs/123
     */
    @GetMapping("/{id}")
    public Result<DailyDocument> getById(@PathVariable Long id) {
        log.info("📄 【文档详情】查询文档详情 - ID: {}", id);

        try {
            if (id == null || id <= 0) {
                log.warn("   ⚠️ 参数错误: ID无效");
                return Result.fail("文档ID无效");
            }

            DailyDocument doc = dailyDocumentService.getById(id);

            if (doc == null) {
                log.warn("   ⚠️ 文档不存在: ID={}", id);
                return Result.fail("文档不存在");
            }

            log.info("✅ 【文档详情】查询成功 - ID: {}, 日期: {}", id, doc.getDocDate());

            return Result.ok(doc);

        } catch (Exception e) {
            log.error("❌ 【文档详情】查询失败 - ID: {}", id, e);
            return Result.fail("查询文档详情失败：" + e.getMessage());
        }
    }

    /**
     * 按日期查询文档
     *
     * <p>获取指定日期的文档。</p>
     *
     * @param date 文档日期（格式：yyyy-MM-dd）
     * @return 该日期的文档
     *
     * @example
     * GET /api/daily-docs/date/2024-01-15
     */
    @GetMapping("/date/{date}")
    public Result<DailyDocument> getByDate(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("📅 【按日期查询】查询文档 - 日期: {}", date);

        try {
            DailyDocument doc = dailyDocumentService.getByDocDate(date);

            if (doc == null) {
                log.info("   ℹ️ 该日期暂无文档: 日期={}", date);
                return Result.ok(null);
            }

            log.info("✅ 【按日期查询】查询成功 - 日期: {}, 标题: {}", date, doc.getTitle());

            return Result.ok(doc);

        } catch (Exception e) {
            log.error("❌ 【按日期查询】查询失败 - 日期: {}", date, e);
            return Result.fail("查询文档失败：" + e.getMessage());
        }
    }

    /**
     * 创建文档
     *
     * <p>创建一个新的每日文档。同一天只能创建一个文档。</p>
     *
     * <p><b>请求体：</b></p>
     * <pre>
     * {
     *   "docDate": "2024-01-15",
     *   "title": "工作日志",
     *   "content": "今天完成了XXX任务..."
     * }
     * </pre>
     *
     * @param doc 文档信息（包含docDate必填）
     * @return 创建成功后的文档对象
     *
     * @example
     * POST /api/daily-docs
     */
    @PostMapping
    public Result<DailyDocument> create(@RequestBody DailyDocument doc) {
        log.info("✨ 【创建文档】开始创建文档");
        log.debug("   ├─ 日期: {}", doc.getDocDate());
        log.debug("   ├─ 标题: {}", doc.getTitle());

        try {
            if (doc.getDocDate() == null) {
                log.warn("   ⚠️ 参数错误: 文档日期为空");
                return Result.fail("文档日期不能为空");
            }

            DailyDocument created = dailyDocumentService.create(doc);

            log.info("✅ 【创建文档】创建成功 - ID: {}, 日期: {}", created.getId(), created.getDocDate());

            return Result.ok(created);

        } catch (Exception e) {
            log.error("❌ 【创建文档】创建失败 - 日期: {}", doc.getDocDate(), e);
            return Result.fail("创建文档失败：" + e.getMessage());
        }
    }

    /**
     * 更新文档
     *
     * <p>根据ID更新文档的标题和内容。</p>
     *
     * <p><b>请求体：</b></p>
     * <pre>
     * {
     *   "title": "更新后的标题",
     *   "content": "更新后的内容..."
     * }
     * </pre>
     *
     * @param id   文档ID
     * @param doc  新的文档数据
     * @return 更新后的文档对象
     *
     * @example
     * PUT /api/daily-docs/123
     */
    @PutMapping("/{id}")
    public Result<DailyDocument> update(@PathVariable Long id, @RequestBody DailyDocument doc) {
        log.info("✏️ 【更新文档】开始更新文档 - ID: {}", id);

        try {
            if (id == null || id <= 0) {
                log.warn("   ⚠️ 参数错误: ID无效");
                return Result.fail("文档ID无效");
            }

            DailyDocument updated = dailyDocumentService.update(id, doc);

            log.info("✅ 【更新文档】更新成功 - ID: {}, 日期: {}", id, updated.getDocDate());

            return Result.ok(updated);

        } catch (Exception e) {
            log.error("❌ 【更新文档】更新失败 - ID: {}", id, e);
            return Result.fail("更新文档失败：" + e.getMessage());
        }
    }

    /**
     * 删除文档
     *
     * <p>将文档移动到垃圾箱，支持30天内恢复。</p>
     *
     * @param id     文档ID
     * @param userId  用户ID（可选）
     * @return 操作结果
     *
     * @example
     * DELETE /api/daily-docs/123
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id,
                           @RequestParam(required = false, defaultValue = "1") Long userId) {

        log.info("🗑️ 【删除文档】开始删除文档 - ID: {}, 用户ID: {}", id, userId);

        try {
            if (id == null || id <= 0) {
                log.warn("   ⚠️ 参数错误: ID无效");
                return Result.fail("文档ID无效");
            }

            dailyDocumentService.deleteById(id, userId);

            log.info("✅ 【删除文档】删除成功 - ID: {}（已移至垃圾箱，30天后自动永久删除）", id);

            return Result.ok(null);

        } catch (Exception e) {
            log.error("❌ 【删除文档】删除失败 - ID: {}", id, e);
            return Result.fail("删除文档失败：" + e.getMessage());
        }
    }
}
