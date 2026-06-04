package com.example.librarybackend.controller;

import com.example.librarybackend.common.Result;
import com.example.librarybackend.common.UserContext;
import com.example.librarybackend.entity.Notification;
import com.example.librarybackend.entity.User;
import com.example.librarybackend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public Result<?> getMyNotifications() {
        User currentUser = UserContext.getUser();
        List<Notification> list = notificationService.getMyNotifications(currentUser.getId());
        int unreadCount = notificationService.getUnreadCount(currentUser.getId());
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("unreadCount", unreadCount);
        return Result.success(result);
    }

    @GetMapping("/unread-count")
    public Result<?> getUnreadCount() {
        User currentUser = UserContext.getUser();
        int count = notificationService.getUnreadCount(currentUser.getId());
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        return Result.success(result);
    }

    @PutMapping("/{id}/read")
    public Result<?> markAsRead(@PathVariable Long id) {
        User currentUser = UserContext.getUser();
        notificationService.markAsRead(id, currentUser.getId());
        return Result.success("已标记为已读");
    }

    @PutMapping("/read-all")
    public Result<?> markAllAsRead() {
        User currentUser = UserContext.getUser();
        notificationService.markAllAsRead(currentUser.getId());
        return Result.success("已全部标记为已读");
    }
}
