package com.example.bookservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.bookservice.entity.Book;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 图书 Mapper 接口。
 * <p>继承 BaseMapper 提供基础 CRUD；自定义方法均在 {@code resources/mapper/BookMapper.xml} 中实现 SQL。</p>
 *
 * @see com.example.bookservice.entity.Book
 */
@Mapper
public interface BookMapper extends BaseMapper<Book> {

    /**
     * 按作者查询未删除的图书列表。
     *
     * @param author 作者名
     * @return 图书列表，无则返回空列表
     */
    List<Book> selectByAuthor(@Param("author") String author);

    /**
     * 按主键更新书名（仅更新未删除记录）。
     *
     * @param id    主键
     * @param title 新书名
     * @return 影响行数
     */
    int updateTitleById(@Param("id") Long id, @Param("title") String title);
}
