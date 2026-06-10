package com.example.librarybackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.librarybackend.entity.Book;
import com.example.librarybackend.entity.BorrowRecord;
import com.example.librarybackend.mapper.BookMapper;
import com.example.librarybackend.service.BookService;
import com.example.librarybackend.service.BorrowService;
import com.example.librarybackend.service.ReserveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class BookServiceImpl extends ServiceImpl<BookMapper, Book> implements BookService {

    @Value("${file.upload-path}")
    private String uploadPath;

    @Lazy
    @Autowired
    private BorrowService borrowService;

    @Lazy
    @Autowired
    private ReserveService reserveService;

    @Override
    public IPage<Book> searchBooks(int page, int size, String keyword, String author, Long categoryId, String publisher, String sortBy) {
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Book::getTitle, keyword);
        }
        if (StringUtils.hasText(author)) {
            wrapper.like(Book::getAuthor, author);
        }
        if (categoryId != null) {
            wrapper.eq(Book::getCategoryId, categoryId);
        }
        if (StringUtils.hasText(publisher)) {
            wrapper.like(Book::getPublisher, publisher);
        }
        switch (sortBy) {
            case "title":
                wrapper.orderByAsc(Book::getTitle);
                break;
            case "author":
                wrapper.orderByAsc(Book::getAuthor);
                break;
            case "stock":
                wrapper.orderByDesc(Book::getStock);
                break;
            default:
                wrapper.orderByDesc(Book::getCreateTime);
                break;
        }
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public Book getBookDetail(Long id) {
        return getById(id);
    }

    @Override
    public void addBook(Book book, MultipartFile coverImage) {
        // 参数校验
        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            throw new RuntimeException("书名不能为空");
        }
        if (book.getStock() == null || book.getStock() < 0) {
            book.setStock(0);
        }

        if (coverImage != null && !coverImage.isEmpty()) {
            String fileName = saveCoverImage(coverImage);
            book.setCoverImage(fileName);
        }
        book.setCreateTime(LocalDateTime.now());
        book.setUpdateTime(LocalDateTime.now());
        save(book);
    }

    @Override
    public void updateBook(Long id, Book book, MultipartFile coverImage) {
        Book existingBook = getById(id);
        if (existingBook == null) {
            throw new RuntimeException("图书不存在");
        }

        book.setId(id);
        // 如果没有上传新封面，保留原有封面
        if (coverImage == null || coverImage.isEmpty()) {
            book.setCoverImage(existingBook.getCoverImage());
        } else {
            String fileName = saveCoverImage(coverImage);
            book.setCoverImage(fileName);
        }
        // 如果没有设置库存，保留原有库存
        if (book.getStock() == null) {
            book.setStock(existingBook.getStock());
        }

        book.setUpdateTime(LocalDateTime.now());
        updateById(book);

        // 补货触发预约流转：库存从0变为>0时，通知预约用户
        if (existingBook.getStock() == 0 && book.getStock() != null && book.getStock() > 0) {
            reserveService.processReturnReservation(id);
        }
    }

    @Override
    public void deleteBook(Long id) {
        // 检查是否有未还的借阅记录
        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BorrowRecord::getBookId, id)
                .in(BorrowRecord::getStatus, "BORROWED", "OVERDUE");
        long count = borrowService.count(wrapper);
        if (count > 0) {
            throw new RuntimeException("该图书有未归还的借阅记录，无法删除");
        }
        removeById(id);
    }

    private String saveCoverImage(MultipartFile file) {
        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
        String fileName = UUID.randomUUID().toString() + extension;

        // 确保目录存在
        File dir = new File(uploadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 保存文件
        try {
            file.transferTo(new File(dir, fileName));
        } catch (IOException e) {
            log.error("文件上传失败：{}", e.getMessage());
            throw new RuntimeException("文件上传失败");
        }

        return fileName;
    }
}
