package com.example.librarybackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.librarybackend.entity.Book;
import com.example.librarybackend.entity.BookCategory;
import com.example.librarybackend.mapper.BookCategoryMapper;
import com.example.librarybackend.service.BookCategoryService;
import com.example.librarybackend.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookCategoryServiceImpl extends ServiceImpl<BookCategoryMapper, BookCategory> implements BookCategoryService {

    private final BookService bookService;

    @Override
    public List<BookCategory> getFirstLevelCategories() {
        LambdaQueryWrapper<BookCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BookCategory::getParentId, 0)
                .orderByAsc(BookCategory::getSortOrder);
        return list(wrapper);
    }

    @Override
    public List<BookCategory> getChildrenByParentId(Long parentId) {
        LambdaQueryWrapper<BookCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BookCategory::getParentId, parentId)
                .orderByAsc(BookCategory::getSortOrder);
        return list(wrapper);
    }

    @Override
    public void addCategory(BookCategory category) {
        // 参数校验
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new RuntimeException("分类名称不能为空");
        }
        if (category.getParentId() == null) {
            category.setParentId(0L); // 默认为一级分类
        }
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        save(category);
    }

    @Override
    public void updateCategory(Long id, BookCategory category) {
        BookCategory existing = getById(id);
        if (existing == null) {
            throw new RuntimeException("分类不存在");
        }
        category.setId(id);
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            category.setName(existing.getName());
        }
        if (category.getParentId() == null) {
            category.setParentId(existing.getParentId());
        }
        if (category.getSortOrder() == null) {
            category.setSortOrder(existing.getSortOrder());
        }
        category.setUpdateTime(LocalDateTime.now());
        updateById(category);
    }

    @Override
    public void deleteCategory(Long id) {
        // 检查是否有子分类
        LambdaQueryWrapper<BookCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BookCategory::getParentId, id);
        if (count(wrapper) > 0) {
            throw new RuntimeException("该分类下有子分类，无法删除");
        }

        // 检查是否有图书引用此分类
        LambdaQueryWrapper<Book> bookWrapper = new LambdaQueryWrapper<>();
        bookWrapper.eq(Book::getCategoryId, id);
        if (bookService.count(bookWrapper) > 0) {
            throw new RuntimeException("该分类下有图书，无法删除");
        }

        removeById(id);
    }
}
