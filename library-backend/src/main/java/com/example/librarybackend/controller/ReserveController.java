package com.example.librarybackend.controller;

import com.example.librarybackend.annotation.RequireAdmin;
import com.example.librarybackend.common.Result;
import com.example.librarybackend.common.UserContext;
import com.example.librarybackend.entity.BorrowRecord;
import com.example.librarybackend.entity.ReserveRecord;
import com.example.librarybackend.entity.User;
import com.example.librarybackend.service.ReserveService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reserve")
@RequiredArgsConstructor
public class ReserveController {

    private final ReserveService reserveService;

    @PostMapping("/{bookId}")
    public Result<?> reserveBook(@PathVariable Long bookId) {
        User currentUser = UserContext.getUser();
        reserveService.reserveBook(currentUser.getId(), bookId);
        return Result.success("预约成功");
    }

    @GetMapping("/my")
    public Result<?> getMyReservations() {
        User currentUser = UserContext.getUser();
        List<ReserveRecord> list = reserveService.getMyReservations(currentUser.getId());
        return Result.success(list);
    }

    @GetMapping("/ready")
    public Result<?> getReadyReservations() {
        User currentUser = UserContext.getUser();
        List<ReserveRecord> list = reserveService.getReadyReservations(currentUser.getId());
        return Result.success(list);
    }

    @PutMapping("/{reserveId}/claim")
    public Result<?> claimReservation(@PathVariable Long reserveId, @RequestBody(required = false) java.util.Map<String, Object> params) {
        User currentUser = UserContext.getUser();
        Integer duration = params != null && params.get("duration") != null ? Integer.valueOf(params.get("duration").toString()) : null;
        String unit = params != null && params.get("unit") != null ? params.get("unit").toString() : null;
        BorrowRecord borrowRecord = reserveService.claimReservation(currentUser.getId(), reserveId, duration, unit);
        return Result.success("领取成功，已自动借阅", borrowRecord);
    }

    @PutMapping("/{reserveId}/cancel")
    public Result<?> cancelReservation(@PathVariable Long reserveId) {
        User currentUser = UserContext.getUser();
        reserveService.cancelReservation(currentUser.getId(), reserveId);
        return Result.success("取消成功");
    }

    @GetMapping("/waiting-count/{bookId}")
    public Result<?> getWaitingCount(@PathVariable Long bookId) {
        int count = reserveService.getWaitingCount(bookId);
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        return Result.success(result);
    }

    @RequireAdmin
    @GetMapping("/all")
    public Result<?> getAllReservations() {
        List<ReserveRecord> list = reserveService.getAllReservations();
        return Result.success(list);
    }
}
