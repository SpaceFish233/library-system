package com.example.librarybackend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.librarybackend.annotation.RequireAdmin;
import com.example.librarybackend.common.Result;
import com.example.librarybackend.entity.Book;
import com.example.librarybackend.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping
    public Result<?> searchBooks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String publisher,
            @RequestParam(defaultValue = "newest") String sortBy) {
        IPage<Book> result = bookService.searchBooks(page, size, keyword, author, categoryId, publisher, sortBy);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<?> getBookDetail(@PathVariable Long id) {
        Book book = bookService.getBookDetail(id);
        if (book == null) {
            return Result.error("图书不存在");
        }
        return Result.success(book);
    }

    @RequireAdmin
    @PostMapping
    public Result<?> addBook(
            @RequestPart("book") Book book,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage) {
        bookService.addBook(book, coverImage);
        return Result.success("添加成功");
    }

    @RequireAdmin
    @PutMapping("/{id}")
    public Result<?> updateBook(
            @PathVariable Long id,
            @RequestPart("book") Book book,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage) {
        bookService.updateBook(id, book, coverImage);
        return Result.success("修改成功");
    }

    @RequireAdmin
    @DeleteMapping("/{id}")
    public Result<?> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return Result.success("删除成功");
    }
}
