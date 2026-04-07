package com.example.scheduleservice.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.common.result.PageResult;
import com.example.common.result.Result;
import com.example.scheduleservice.entity.ScheduleEvent;
import com.example.scheduleservice.service.ScheduleEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 日程事件 REST API 控制器
 *
 * <p>提供日程的完整CRUD操作接口，支持按时间范围查询、分页查询等功能。</p>
 *
 * <p><b>接口列表：</b></p>
 * <ul>
 *   <li>GET /api/schedules - 查询日程列表（分页）</li>
 *   <li>GET /api/schedules/today - 获取今日日程</li>
 *   <li>GET /api/schedules/{id} - 获取日程详情</li>
 *   <li>POST /api/schedules - 创建日程</li>
 *   <li>PUT /api/schedules/{id} - 更新日程</li>
 *   <li>DELETE /api/schedules/{id} - 删除日程（移至垃圾箱）</li>
 * </ul>
 *
 * @author Schedule Service
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ScheduleController {

    private final ScheduleEventService scheduleEventService;

    /**
     * 查询日程列表（分页）
     *
     * <p>根据时间范围查询日程，支持分页返回。</p>
     *
     * <p><b>请求参数：</b></p>
     * <ul>
     *   <li>start: 开始时间（必填，格式：yyyy-MM-dd'T'HH:mm:ss）</li>
     *   <li>end: 结束时间（必填，格式：yyyy-MM-dd'T'HH:mm:ss）</li>
     *   <li>view: 视图类型（可选，用于前端视图切换）</li>
     *   <li>page: 页码（可选，默认1）</li>
     *   <li>size: 每页数量（可选，默认20）</li>
     * </ul>
     *
     * <p><b>返回数据：</b></p>
     * <pre>
     * {
     *   "code": 0,
     *   "message": "成功",
     *   "data": {
     *     "list": [...],
     *     "total": 100,
     *     "page": 1,
     *     "size": 20
     *   }
     * }
     * </pre>
     *
     * @param start 开始时间
     * @param end   结束时间
     * @param view  视图类型（可选）
     * @param page  页码（默认1）
     * @param size  每页数量（默认20）
     * @return 分页后的日程列表
     *
     * @example
     * GET /api/schedules?start=2024-01-01T00:00:00&end=2024-12-31T23:59:59&page=1&size=20
     */
    @GetMapping
    public Result<PageResult<ScheduleEvent>> list(
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(required = false) String view,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("🔍 【日程查询】开始查询日程列表");
        log.debug("   ├─ 参数: start={}, end={}, view={}, page={}, size={}", start, end, view, page, size);

        try {
            // 解析时间参数
            LocalDateTime startTime = LocalDateTime.parse(start + "T00:00:00");
            LocalDateTime endTime = LocalDateTime.parse(end + "T23:59:59");

            log.debug("   ├─ 解析后: startTime={}, endTime={}", startTime, endTime);

            // 调用服务层查询
            PageResult<ScheduleEvent> result = scheduleEventService.listByTimeRangePaged(startTime, endTime, page, size);

            log.info("✅ 【日程查询】查询成功 - 共 {} 条记录，第 {}/{} 页",
                    result.getTotal(), page, (result.getTotal() + size - 1) / size);

            return Result.ok(result);

        } catch (Exception e) {
            log.error("❌ 【日程查询】查询失败 - start={}, end={}", start, end, e);
            return Result.fail("查询日程列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取今日日程
     *
     * <p>获取当天的所有日程安排，按开始时间排序。</p>
     *
     * <p><b>返回数据：</b></p>
     * <pre>
     * {
     *   "code": 0,
     *   "message": "成功",
     *   "data": [...]
     * }
     * </pre>
     *
     * @return 今日日程列表
     *
     * @example
     * GET /api/schedules/today
     */
    @GetMapping("/today")
    public Result<List<ScheduleEvent>> getTodaySchedules() {
        LocalDate today = LocalDate.now();

        log.info("📅 【今日日程】获取今日日程 - 日期: {}", today);

        try {
            LocalDateTime startTime = today.atStartOfDay();
            LocalDateTime endTime = today.plusDays(1).atStartOfDay();

            log.debug("   ├─ 时间范围: {} ~ {}", startTime, endTime);

            List<ScheduleEvent> events = scheduleEventService.listByTimeRange(startTime, endTime);

            log.info("✅ 【今日日程】获取成功 - 共 {} 条日程", events.size());

            return Result.ok(events);

        } catch (Exception e) {
            log.error("❌ 【今日日程】获取失败 - 日期: {}", today, e);
            return Result.fail("获取今日日程失败：" + e.getMessage());
        }
    }

    /**
     * 获取日程详情
     *
     * <p>根据ID获取单个日程的详细信息。</p>
     *
     * @param id 日程ID
     * @return 日程详情
     *
     * @example
     * GET /api/schedules/123
     */
    @GetMapping("/{id}")
    public Result<ScheduleEvent> getById(@PathVariable Long id) {
        log.info("🔍 【日程详情】查询日程详情 - ID: {}", id);

        try {
            if (id == null || id <= 0) {
                log.warn("   ⚠️ 参数错误: ID无效 - {}", id);
                return Result.fail("日程ID无效");
            }

            ScheduleEvent event = scheduleEventService.getById(id);

            if (event == null) {
                log.warn("   ⚠️ 日程不存在: ID={}", id);
                return Result.fail("日程不存在");
            }

            log.info("✅ 【日程详情】查询成功 - ID: {}, 标题: {}", id, event.getTitle());

            return Result.ok(event);

        } catch (Exception e) {
            log.error("❌ 【日程详情】查询失败 - ID: {}", id, e);
            return Result.fail("查询日程详情失败：" + e.getMessage());
        }
    }

    /**
     * 创建日程
     *
     * <p>创建一个新的日程安排。</p>
     *
     * <p><b>请求体：</b></p>
     * <pre>
     * {
     *   "title": "会议",
     *   "description": "项目评审会议",
     *   "startTime": "2024-01-15T10:00:00",
     *   "endTime": "2024-01-15T11:00:00",
     *   "color": "#3b82f6",
     *   "reminderMinutes": 15,
     *   "notifyWechat": true,
     *   "notifyQQ": false
     * }
     * </pre>
     *
     * @param event 日程信息（包含title、startTime必填）
     * @return 创建成功后的日程对象（包含自动生成的ID）
     *
     * @example
     * POST /api/schedules
     */
    @PostMapping
    public Result<ScheduleEvent> create(@RequestBody ScheduleEvent event) {
        log.info("✨ 【创建日程】开始创建日程");
        log.debug("   ├─ 标题: {}", event.getTitle());
        log.debug("   ├─ 开始时间: {}", event.getStartTime());
        log.debug("   ├─ 结束时间: {}", event.getEndTime());
        log.debug("   ├─ 颜色: {}", event.getColor());
        log.debug("   ├─ 提醒: {} 分钟前", event.getReminderMinutes());

        try {
            // 调用服务层创建
            ScheduleEvent created = scheduleEventService.create(event);

            log.info("✅ 【创建日程】创建成功 - ID: {}, 标题: {}", created.getId(), created.getTitle());

            return Result.ok(created);

        } catch (Exception e) {
            log.error("❌ 【创建日程】创建失败 - 标题: {}", event.getTitle(), e);
            return Result.fail("创建日程失败：" + e.getMessage());
        }
    }

    /**
     * 更新日程
     *
     * <p>根据ID更新日程的各个属性。</p>
     *
     * <p><b>请求体：</b></p>
     * <pre>
     * {
     *   "title": "更新后的标题",
     *   "description": "更新后的描述",
     *   "startTime": "2024-01-15T14:00:00",
     *   "endTime": "2024-01-15T15:00:00",
     *   "color": "#10b981"
     * }
     * </pre>
     *
     * @param id   日程ID
     * @param event 新的日程数据（包含要更新的字段）
     * @return 更新后的日程对象
     *
     * @example
     * PUT /api/schedules/123
     */
    @PutMapping("/{id}")
    public Result<ScheduleEvent> update(@PathVariable Long id, @RequestBody ScheduleEvent event) {
        log.info("✏️ 【更新日程】开始更新日程 - ID: {}", id);
        log.debug("   ├─ 新标题: {}", event.getTitle());
        log.debug("   ├─ 新开始时间: {}", event.getStartTime());

        try {
            if (id == null || id <= 0) {
                log.warn("   ⚠️ 参数错误: ID无效 - {}", id);
                return Result.fail("日程ID无效");
            }

            ScheduleEvent updated = scheduleEventService.update(id, event);

            log.info("✅ 【更新日程】更新成功 - ID: {}, 新标题: {}", id, updated.getTitle());

            return Result.ok(updated);

        } catch (Exception e) {
            log.error("❌ 【更新日程】更新失败 - ID: {}", id, e);
            return Result.fail("更新日程失败：" + e.getMessage());
        }
    }

    /**
     * 删除日程
     *
     * <p>将日程移动到垃圾箱，支持30天内恢复。</p>
     *
     * <p><b>注意：</b>此操作不会立即永久删除数据，而是将日程移动到垃圾箱，
     * 30天后系统会自动永久删除。</p>
     *
     * @param id     日程ID
     * @param userId 用户ID（可选，用于标识垃圾箱归属）
     * @return 操作结果
     *
     * @example
     * DELETE /api/schedules/123
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id,
                            @RequestParam(required = false, defaultValue = "1") Long userId) {

        log.info("🗑️ 【删除日程】开始删除日程 - ID: {}, 用户ID: {}", id, userId);

        try {
            if (id == null || id <= 0) {
                log.warn("   ⚠️ 参数错误: ID无效 - {}", id);
                return Result.fail("日程ID无效");
            }

            // 调用服务层删除（移动到垃圾箱）
            scheduleEventService.deleteById(id, userId);

            log.info("✅ 【删除日程】删除成功 - ID: {}（已移至垃圾箱，30天后自动永久删除）", id);

            return Result.ok(null);

        } catch (Exception e) {
            log.error("❌ 【删除日程】删除失败 - ID: {}", id, e);
            return Result.fail("删除日程失败：" + e.getMessage());
        }
    }
}
