package com.example.scheduleservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.scheduleservice.entity.NoteCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface NoteCategoryMapper extends BaseMapper<NoteCategory> {

    @Select("SELECT * FROM note_category WHERE deleted = 0 AND user_id IS NULL OR user_id = #{userId} ORDER BY sort_order ASC, id ASC")
    List<NoteCategory> findAllWithDefault(@Param("userId") Long userId);
}
