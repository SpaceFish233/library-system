package com.example.librarybackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.librarybackend.entity.BookCategory;

import java.util.List;

public interface BookCategoryService extends IService<BookCategory> {
    List<BookCategory> getFirstLevelCategories();
    List<BookCategory> getChildrenByParentId(Long parentId);
    BookCategory getCategoryById(Long id);
    void addCategory(BookCategory category);
    void updateCategory(Long id, BookCategory category);
    void deleteCategory(Long id);
}
