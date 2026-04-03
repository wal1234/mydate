package com.example.scheduleservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.scheduleservice.entity.ScheduleEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 日程事件 Mapper，所有 SQL 均在对应 XML 中实现。
 */
@Mapper
public interface ScheduleEventMapper extends BaseMapper<ScheduleEvent> {

    /**
     * 按时间范围查询未删除的日程（与 start/end 有交集的记录）。
     */
    List<ScheduleEvent> selectByTimeRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * 按时间范围统计数量，用于分页 total。
     */
    long countByTimeRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * 按时间范围分页查询。
     */
    List<ScheduleEvent> selectByTimeRangePaged(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                                               @Param("offset") int offset, @Param("limit") int limit);
}
