package com.example.scheduleservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.scheduleservice.entity.Note;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface NoteMapper extends BaseMapper<Note> {

    @Select("SELECT * FROM note WHERE deleted = 0 AND content_type = #{contentType} ORDER BY is_pinned DESC, updated_at DESC")
    List<Note> findByContentType(@Param("contentType") String contentType);

    @Select("SELECT * FROM note WHERE deleted = 0 AND category_id = #{categoryId} ORDER BY is_pinned DESC, updated_at DESC")
    List<Note> findByCategory(@Param("categoryId") Long categoryId);
}
