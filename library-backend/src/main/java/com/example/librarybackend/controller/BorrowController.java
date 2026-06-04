package com.example.librarybackend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.librarybackend.annotation.RequireAdmin;
import com.example.librarybackend.common.Result;
import com.example.librarybackend.common.UserContext;
import com.example.librarybackend.entity.BorrowRecord;
import com.example.librarybackend.entity.User;
import com.example.librarybackend.service.BorrowService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/borrow")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;

    @PostMapping
    public Result<?> borrowBook(@RequestBody Map<String, Object> params) {
        User currentUser = UserContext.getUser();
        Long bookId = params.get("bookId") != null ? Long.valueOf(params.get("bookId").toString()) : null;
        Integer duration = params.get("duration") != null ? Integer.valueOf(params.get("duration").toString()) : null;
        String unit = params.get("unit") != null ? params.get("unit").toString() : null;
        if (bookId == null) {
            return Result.error("图书ID不能为空");
        }
        BorrowRecord record = borrowService.borrowBook(currentUser.getId(), bookId, duration, unit);
        return Result.success("借阅成功", record);
    }

    @GetMapping("/{recordId}")
    public Result<?> getRecordDetail(@PathVariable Long recordId) {
        User currentUser = UserContext.getUser();
        BorrowRecord record = borrowService.getRecordDetail(recordId);
        // 用户只能查看自己的记录，管理员可以查看所有
        if (!record.getUserId().equals(currentUser.getId()) && !"admin".equals(currentUser.getRole())) {
            return Result.error(403, "无权查看此记录");
        }
        return Result.success(record);
    }

    @PutMapping("/{recordId}/apply-return")
    public Result<?> applyReturn(@PathVariable Long recordId) {
        User currentUser = UserContext.getUser();
        borrowService.applyReturn(currentUser.getId(), recordId);
        return Result.success("归还申请已提交，等待管理员确认", null);
    }

    @RequireAdmin
    @PutMapping("/{recordId}/confirm-return")
    public Result<?> confirmReturn(@PathVariable Long recordId) {
        Map<String, Object> result = borrowService.confirmReturn(recordId);
        return Result.success("确认归还成功", result);
    }

    @GetMapping("/my")
    public Result<?> getMyRecords(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        User currentUser = UserContext.getUser();
        IPage<BorrowRecord> result = borrowService.getMyRecords(currentUser.getId(), status, page, size);
        return Result.success(result);
    }

    @RequireAdmin
    @GetMapping("/all")
    public Result<?> getAllRecords(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String bookName,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        IPage<BorrowRecord> result = borrowService.getAllRecords(username, bookName, status, page, size);
        return Result.success(result);
    }
}
