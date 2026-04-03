package com.example.scheduleservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.scheduleservice.entity.DailyDocument;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 每日文档 Mapper，所有 SQL 均在对应 XML 中实现。
 */
@Mapper
public interface DailyDocumentMapper extends BaseMapper<DailyDocument> {

    /**
     * 按文档日期查询单篇（同一 user 下 doc_date 唯一）。
     */
    DailyDocument selectByDocDate(@Param("docDate") LocalDate docDate);

    /**
     * 按日期区间查询文档列表，按 doc_date 升序。
     */
    List<DailyDocument> selectByDateRange(@Param("from") LocalDate from, @Param("to") LocalDate to);

    /**
     * 按日期区间统计数量。
     */
    long countByDateRange(@Param("from") LocalDate from, @Param("to") LocalDate to);

    /**
     * 按日期区间分页查询。
     */
    List<DailyDocument> selectByDateRangePaged(@Param("from") LocalDate from, @Param("to") LocalDate to,
                                              @Param("offset") int offset, @Param("limit") int limit);
}
