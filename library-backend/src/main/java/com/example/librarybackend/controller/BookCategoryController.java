package com.example.librarybackend.controller;

import com.example.librarybackend.annotation.RequireAdmin;
import com.example.librarybackend.common.Result;
import com.example.librarybackend.entity.BookCategory;
import com.example.librarybackend.service.BookCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class BookCategoryController {

    private final BookCategoryService categoryService;

    @GetMapping
    public Result<?> getFirstLevelCategories() {
        List<BookCategory> list = categoryService.getFirstLevelCategories();
        return Result.success(list);
    }

    @GetMapping("/{parentId}/children")
    public Result<?> getChildren(@PathVariable Long parentId) {
        List<BookCategory> list = categoryService.getChildrenByParentId(parentId);
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<?> getCategoryById(@PathVariable Long id) {
        BookCategory category = categoryService.getCategoryById(id);
        if (category == null) {
            return Result.error("分类不存在");
        }
        return Result.success(category);
    }

    @RequireAdmin
    @PostMapping
    public Result<?> addCategory(@RequestBody BookCategory category) {
        categoryService.addCategory(category);
        return Result.success("添加成功");
    }

    @RequireAdmin
    @PutMapping("/{id}")
    public Result<?> updateCategory(@PathVariable Long id, @RequestBody BookCategory category) {
        categoryService.updateCategory(id, category);
        return Result.success("修改成功");
    }

    @RequireAdmin
    @DeleteMapping("/{id}")
    public Result<?> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success("删除成功");
    }
}
