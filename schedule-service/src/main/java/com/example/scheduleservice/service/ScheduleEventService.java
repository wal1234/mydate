package com.example.scheduleservice.service;

import com.example.common.exception.BusinessException;
import com.example.common.result.PageResult;
import com.example.common.result.ResultCode;
import com.example.scheduleservice.entity.ScheduleEvent;
import com.example.scheduleservice.mapper.ScheduleEventMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 日程事件业务服务
 *
 * <p>提供日程事件的完整CRUD操作，以及按时间范围查询等业务功能。
 * 所有操作均记录详细日志，便于问题追踪和系统监控。</p>
 *
 * <p><b>主要功能：</b></p>
 * <ul>
 *   <li>日程创建、查询、更新、删除</li>
 *   <li>按日期范围查询日程</li>
 *   <li>分页查询支持</li>
 *   <li>支持软删除到垃圾箱</li>
 * </ul>
 *
 * @author Schedule Service
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleEventService {

    private final ScheduleEventMapper scheduleEventMapper;
    private final TrashService trashService;

    /**
     * 创建新的日程事件
     *
     * <p>创建一个新的日程记录，包含标题、描述、时间、颜色、提醒等属性。</p>
     *
     * @param event 日程事件实体对象，包含日程的所有属性
     *              <ul>
     *                <li>title: 日程标题（必填）</li>
     *                <li>startTime: 开始时间（必填）</li>
     *                <li>endTime: 结束时间（可选）</li>
     *                <li>description: 日程描述（可选）</li>
     *                <li>color: 日程颜色（可选）</li>
     *                <li>reminderMinutes: 提醒分钟数（可选）</li>
     *              </ul>
     * @return 创建成功的日程事件对象，包含自动生成的ID
     * @throws BusinessException 如果创建失败，抛出业务异常
     *
     * @example
     * <pre>
     * ScheduleEvent event = new ScheduleEvent();
     * event.setTitle("团队会议");
     * event.setStartTime(LocalDateTime.now().plusHours(1));
     * event.setEndTime(LocalDateTime.now().plusHours(2));
     * event.setDescription("讨论项目进度");
     * ScheduleEvent created = service.create(event);
     * </pre>
     */
    public ScheduleEvent create(ScheduleEvent event) {
        // 验证必填字段
        if (event.getTitle() == null || event.getTitle().trim().isEmpty()) {
            log.warn("创建日程失败：标题为空");
            throw new BusinessException(ResultCode.PARAM_INVALID.getCode(), "日程标题不能为空");
        }

        if (event.getStartTime() == null) {
            log.warn("创建日程失败：开始时间为空");
            throw new BusinessException(ResultCode.PARAM_INVALID.getCode(), "开始时间不能为空");
        }

        // 设置默认值
        if (event.getColor() == null || event.getColor().isEmpty()) {
            event.setColor("#3b82f6"); // 默认蓝色
        }

        // 保存到数据库
        scheduleEventMapper.insert(event);
        log.info("✅ 日程创建成功 - ID: {}, 标题: {}", event.getId(), event.getTitle());

        return event;
    }

    /**
     * 根据ID查询日程详情
     *
     * <p>通过日程的唯一标识符获取完整的日程信息。</p>
     *
     * @param id 日程的唯一标识符
     * @return 日程事件对象，如果不存在则返回null
     *
     * @example
     * <pre>
     * ScheduleEvent event = service.getById(1L);
     * if (event != null) {
     *     System.out.println(event.getTitle());
     * }
     * </pre>
     */
    public ScheduleEvent getById(Long id) {
        if (id == null) {
            log.warn("查询日程失败：ID参数为空");
            return null;
        }

        ScheduleEvent event = scheduleEventMapper.selectById(id);

        if (event == null) {
            log.debug("查询日程详情 - ID: {} 未找到", id);
        } else {
            log.debug("查询日程详情 - ID: {}, 标题: {}", id, event.getTitle());
        }

        return event;
    }

    /**
     * 查询指定时间范围内的所有日程
     *
     * <p>获取在指定开始时间和结束时间范围内的所有日程列表，按开始时间升序排列。</p>
     *
     * @param start 范围开始时间（必填）
     * @param end   范围结束时间（必填）
     * @return 日程列表，如果范围内没有日程则返回空列表
     *
     * @example
     * <pre>
     * List&lt;ScheduleEvent&gt; events = service.getByDateRange(
     *     LocalDateTime.of(2024, 1, 1, 0, 0),
     *     LocalDateTime.of(2024, 1, 31, 23, 59)
     * );
     * </pre>
     */
    public List<ScheduleEvent> getByDateRange(LocalDateTime start, LocalDateTime end) {
        // 参数验证
        if (start == null || end == null) {
            log.warn("日期范围查询失败：开始时间或结束时间为空");
            throw new BusinessException(ResultCode.PARAM_INVALID.getCode(), "开始时间和结束时间不能为空");
        }

        if (start.isAfter(end)) {
            log.warn("日期范围查询失败：开始时间 {} 晚于结束时间 {}", start, end);
            throw new BusinessException(ResultCode.PARAM_INVALID.getCode(), "开始时间不能晚于结束时间");
        }

        // 执行查询
        List<ScheduleEvent> events = scheduleEventMapper.selectByTimeRange(start, end);
        log.info("📅 日期范围查询完成 - 从 {} 到 {} 共找到 {} 条日程", start, end, events.size());

        return events;
    }

    /**
     * 查询指定时间范围内的所有日程
     *
     * <p>获取在指定开始时间和结束时间范围内的所有日程列表。</p>
     *
     * @param start 范围开始时间
     * @param end   范围结束时间
     * @return 日程列表
     */
    public List<ScheduleEvent> listByTimeRange(LocalDateTime start, LocalDateTime end) {
        return scheduleEventMapper.selectByTimeRange(start, end);
    }

    /**
     * 分页查询指定时间范围内的日程
     *
     * @param start 范围开始时间
     * @param end   范围结束时间
     * @param page  页码
     * @param size  每页数量
     * @return 分页结果
     */
    public PageResult<ScheduleEvent> listByTimeRangePaged(LocalDateTime start, LocalDateTime end, int page, int size) {
        long total = scheduleEventMapper.countByTimeRange(start, end);
        int offset = (page - 1) * size;
        List<ScheduleEvent> events = scheduleEventMapper.selectByTimeRangePaged(start, end, offset, size);
        return new PageResult<>(events, total, page, size);
    }

    /**
     * 更新日程信息
     *
     * <p>根据ID更新日程的各个属性，保留原ID和创建时间。</p>
     *
     * @param id        日程ID（必填）
     * @param newEvent  新的日程数据，包含要更新的字段
     *                   <p>可更新的字段：</p>
     *                   <ul>
     *                     <li>title: 日程标题</li>
     *                     <li>description: 日程描述</li>
     *                     <li>startTime: 开始时间</li>
     *                     <li>endTime: 结束时间</li>
     *                     <li>color: 日程颜色</li>
     *                     <li>reminderMinutes: 提醒分钟数</li>
     *                   </ul>
     * @return 更新后的日程对象
     * @throws BusinessException 如果日程不存在或更新失败
     *
     * @example
     * <pre>
     * ScheduleEvent update = new ScheduleEvent();
     * update.setTitle("修改后的标题");
     * update.setDescription("修改后的描述");
     * ScheduleEvent updated = service.update(1L, update);
     * </pre>
     */
    public ScheduleEvent update(Long id, ScheduleEvent newEvent) {
        // 参数验证
        if (id == null) {
            log.warn("更新日程失败：ID为空");
            throw new BusinessException(ResultCode.PARAM_INVALID.getCode(), "日程ID不能为空");
        }

        // 查询原日程
        ScheduleEvent existing = scheduleEventMapper.selectById(id);
        if (existing == null) {
            log.warn("更新日程失败：日程不存在 - ID: {}", id);
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "日程不存在");
        }

        // 记录变更前的信息
        log.debug("开始更新日程 - ID: {}, 原标题: {}", id, existing.getTitle());

        // 更新非空字段
        if (newEvent.getTitle() != null) {
            existing.setTitle(newEvent.getTitle());
        }
        if (newEvent.getDescription() != null) {
            existing.setDescription(newEvent.getDescription());
        }
        if (newEvent.getStartTime() != null) {
            existing.setStartTime(newEvent.getStartTime());
        }
        if (newEvent.getEndTime() != null) {
            existing.setEndTime(newEvent.getEndTime());
        }
        if (newEvent.getColor() != null) {
            existing.setColor(newEvent.getColor());
        }
        if (newEvent.getReminderMinutes() != null) {
            existing.setReminderMinutes(newEvent.getReminderMinutes());
        }
        if (newEvent.getNotifyWechat() != null) {
            existing.setNotifyWechat(newEvent.getNotifyWechat());
        }
        if (newEvent.getNotifyQQ() != null) {
            existing.setNotifyQQ(newEvent.getNotifyQQ());
        }

        // 保存更新
        scheduleEventMapper.updateById(existing);
        log.info("✅ 日程更新成功 - ID: {}, 新标题: {}", id, existing.getTitle());

        return existing;
    }

    /**
     * 移动日程到垃圾箱
     *
     * <p>将指定日程移动到垃圾箱，而非直接删除。垃圾箱中的日程可在30天内恢复，
     * 超过30天后将自动永久删除。</p>
     *
     * <p>此方法会：</p>
     * <ol>
     *   <li>将日程完整数据保存到垃圾箱记录</li>
     *   <li>从日程表中物理删除该记录</li>
     * </ol>
     *
     * @param id     日程ID（必填）
     * @param userId 用户ID（必填），用于标识垃圾箱归属
     * @throws BusinessException 如果日程不存在或删除失败
     *
     * @see TrashService#moveToTrash
     * @see TrashCleanupScheduler
     *
     * @example
     * <pre>
     * // 删除日程到垃圾箱
     * service.deleteById(1L, 1L);
     *
     * // 用户可以在垃圾箱页面恢复或永久删除
     * </pre>
     */
    public void deleteById(Long id, Long userId) {
        log.debug("🗑️ 移动日程到垃圾箱 - ID: {}", id);

        // 参数验证
        if (id == null) {
            log.warn("删除日程失败：ID为空");
            throw new BusinessException(ResultCode.PARAM_INVALID.getCode(), "日程ID不能为空");
        }

        if (userId == null) {
            log.warn("删除日程失败：用户ID为空");
            throw new BusinessException(ResultCode.PARAM_INVALID.getCode(), "用户ID不能为空");
        }

        // 查询要删除的日程
        ScheduleEvent existing = scheduleEventMapper.selectById(id);
        if (existing == null) {
            log.warn("🗑️ 删除日程失败：日程不存在 - ID: {}", id);
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "日程不存在");
        }

        // 记录原标题用于日志
        String title = existing.getTitle();

        // 移动到垃圾箱（保存完整数据用于恢复）
        trashService.moveToTrash(
                userId,
                "schedule",
                id,
                title,
                existing.getDescription(),
                existing
        );

        // 物理删除日程记录
        scheduleEventMapper.deleteById(id);

        log.info("✅ 日程已移动到垃圾箱 - ID: {}, 标题: {}, 保留期: 30天", id, title);
    }
}
