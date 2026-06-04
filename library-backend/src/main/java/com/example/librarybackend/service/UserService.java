package com.example.librarybackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.librarybackend.entity.User;

import java.util.Map;

public interface UserService extends IService<User> {
    Map<String, Object> register(String username, String password, String nickname, String phone);
    Map<String, Object> login(String username, String password);
    User getUserInfo(Long userId);
    void updateUserInfo(Long userId, String nickname, String phone);
    void changePassword(Long userId, String oldPassword, String newPassword);
    Map<String, Object> getNotifications(Long userId);
}
