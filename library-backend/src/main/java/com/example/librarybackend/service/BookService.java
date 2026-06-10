package com.example.librarybackend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.librarybackend.entity.Book;
import org.springframework.web.multipart.MultipartFile;

public interface BookService extends IService<Book> {
    IPage<Book> searchBooks(int page, int size, String keyword, String author, Long categoryId, String publisher, String sortBy);
    Book getBookDetail(Long id);
    void addBook(Book book, MultipartFile coverImage);
    void updateBook(Long id, Book book, MultipartFile coverImage);
    void deleteBook(Long id);
}
