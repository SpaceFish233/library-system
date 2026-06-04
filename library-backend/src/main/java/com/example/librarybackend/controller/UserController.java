package com.example.librarybackend.controller;

import com.example.librarybackend.common.Result;
import com.example.librarybackend.common.UserContext;
import com.example.librarybackend.entity.User;
import com.example.librarybackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public Result<?> register(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        String nickname = params.get("nickname");
        String phone = params.get("phone");
        Map<String, Object> result = userService.register(username, password, nickname, phone);
        return Result.success("注册成功", result);
    }

    @PostMapping("/login")
    public Result<?> login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        Map<String, Object> result = userService.login(username, password);
        return Result.success("登录成功", result);
    }

    @GetMapping("/info")
    public Result<?> getUserInfo() {
        User currentUser = UserContext.getUser();
        User user = userService.getUserInfo(currentUser.getId());
        return Result.success(user);
    }

    @PutMapping("/info")
    public Result<?> updateUserInfo(@RequestBody Map<String, String> params) {
        User currentUser = UserContext.getUser();
        String nickname = params.get("nickname");
        String phone = params.get("phone");
        userService.updateUserInfo(currentUser.getId(), nickname, phone);
        return Result.success("更新成功");
    }

    @PutMapping("/password")
    public Result<?> changePassword(@RequestBody Map<String, String> params) {
        User currentUser = UserContext.getUser();
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        userService.changePassword(currentUser.getId(), oldPassword, newPassword);
        return Result.success("密码修改成功");
    }

    @GetMapping("/notifications")
    public Result<?> getNotifications() {
        User currentUser = UserContext.getUser();
        Map<String, Object> notifications = userService.getNotifications(currentUser.getId());
        return Result.success(notifications);
    }
}
