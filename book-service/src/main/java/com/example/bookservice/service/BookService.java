package com.example.bookservice.service;

import com.example.bookservice.entity.Book;
import com.example.bookservice.mapper.BookMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 图书业务服务。
 * 封装对 Book 的查询、按作者列表、更新标题与新增等操作，并统一打日志。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {

    private final BookMapper bookMapper;

    /**
     * 按主键查询图书。
     *
     * @param id 主键
     * @return 图书，不存在则 null
     */
    public Book getById(Long id) {
        log.debug("查询图书, id={}", id);
        Book book = bookMapper.selectById(id);
        if (book != null) {
            log.info("查询图书成功, id={}, title={}", id, book.getTitle());
        } else {
            log.info("图书不存在, id={}", id);
        }
        return book;
    }

    /**
     * 按作者查询未删除的图书列表。
     *
     * @param author 作者
     * @return 图书列表
     */
    public List<Book> listByAuthor(String author) {
        log.debug("按作者查询图书, author={}", author);
        List<Book> list = bookMapper.selectByAuthor(author);
        log.info("按作者查询图书完成, author={}, size={}", author, list.size());
        return list;
    }

    /**
     * 按主键更新书名。
     *
     * @param id    主键
     * @param title 新书名
     * @return 是否更新成功
     */
    public boolean updateTitle(Long id, String title) {
        log.debug("更新图书标题, id={}, title={}", id, title);
        int rows = bookMapper.updateTitleById(id, title);
        boolean ok = rows > 0;
        if (ok) {
            log.info("更新图书标题成功, id={}, title={}", id, title);
        } else {
            log.warn("更新图书标题未命中记录, id={}", id);
        }
        return ok;
    }

    /**
     * 新增图书。
     *
     * @param book 图书实体（id 可为 null，自增）
     * @return 是否插入成功
     */
    public boolean save(Book book) {
        log.debug("新增图书, title={}, author={}", book.getTitle(), book.getAuthor());
        int rows = bookMapper.insert(book);
        boolean ok = rows > 0;
        if (ok) {
            log.info("新增图书成功, id={}, title={}", book.getId(), book.getTitle());
        } else {
            log.error("新增图书失败, title={}", book.getTitle());
        }
        return ok;
    }
}
