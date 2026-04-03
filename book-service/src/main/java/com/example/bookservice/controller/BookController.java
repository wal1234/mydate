package com.example.bookservice.controller;

import com.example.bookservice.entity.Book;
import com.example.bookservice.service.BookService;
import com.example.common.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 图书 REST 接口。
 * 统一使用 book-common 的 Result 包装返回值。
 */
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Slf4j
public class BookController {

    private final BookService bookService;

    /**
     * 根据主键查询图书。
     *
     * @param id 图书主键
     * @return Result 包装的图书信息
     */
    @GetMapping("/{id}")
    public Result<Book> getById(@PathVariable Long id) {
        log.info("请求查询图书, id={}", id);
        Book book = bookService.getById(id);
        return book != null ? Result.ok(book) : Result.fail(404, "图书不存在");
    }

    /**
     * 根据作者查询图书列表；未传 author 时返回空列表。
     *
     * @param author 作者（可选）
     * @return Result 包装的图书列表
     */
    @GetMapping
    public Result<List<Book>> listByAuthor(@RequestParam(required = false) String author) {
        if (author != null && !author.isBlank()) {
            log.info("请求按作者查询图书, author={}", author);
            return Result.ok(bookService.listByAuthor(author));
        }
        log.debug("未传 author，返回空列表");
        return Result.ok(List.of());
    }

    /**
     * 新增图书。
     *
     * @param book 图书信息（请求体）
     * @return Result 包装的保存后的图书
     */
    @PostMapping
    public Result<Book> create(@RequestBody Book book) {
        log.info("请求新增图书, title={}, author={}", book.getTitle(), book.getAuthor());
        bookService.save(book);
        return Result.ok(book);
    }

    /**
     * 更新指定图书的标题。
     *
     * @param id    图书主键
     * @param title 新标题
     * @return Result 包装是否成功
     */
    @PatchMapping("/{id}/title")
    public Result<Boolean> updateTitle(@PathVariable Long id, @RequestParam String title) {
        log.info("请求更新图书标题, id={}, title={}", id, title);
        return Result.ok(bookService.updateTitle(id, title));
    }
}
