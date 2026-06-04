package com.example.librarybackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.librarybackend.entity.Notification;

import java.util.List;
import java.util.Map;

public interface NotificationService extends IService<Notification> {
    void sendNotification(Long userId, String content, String type, Long relatedId);
    List<Notification> getMyNotifications(Long userId);
    int getUnreadCount(Long userId);
    void markAsRead(Long notificationId, Long userId);
    void markAllAsRead(Long userId);
}
