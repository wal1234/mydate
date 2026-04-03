package com.example.bookservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 图书实体，对应表 book。
 * 主键自增，逻辑删除字段为 deleted。
 */
@Data
@TableName("book")
public class Book {

    /** 主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 书名 */
    private String title;
    /** 作者 */
    private String author;
    /** ISBN 编号 */
    private String isbn;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 逻辑删除：0-未删除，1-已删除 */
    @TableLogic
    private Integer deleted;
}
