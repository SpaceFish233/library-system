package com.example.librarybackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.librarybackend.entity.ReserveRecord;
import com.example.librarybackend.entity.User;
import com.example.librarybackend.mapper.UserMapper;
import com.example.librarybackend.service.ReserveService;
import com.example.librarybackend.service.UserService;
import com.example.librarybackend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final JwtUtil jwtUtil;
    private final ReserveService reserveService;

    @Override
    public Map<String, Object> register(String username, String password, String nickname, String phone) {
        // 参数校验
        if (username == null || username.trim().isEmpty()) {
            throw new RuntimeException("用户名不能为空");
        }
        if (password == null || password.length() < 6) {
            throw new RuntimeException("密码长度不能少于6位");
        }
        if (nickname == null || nickname.trim().isEmpty()) {
            nickname = username; // 默认使用用户名作为昵称
        }

        // 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username.trim());
        if (count(wrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 创建用户
        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
        user.setNickname(nickname.trim());
        user.setPhone(phone);
        user.setRole("user");
        user.setCreditScore(100);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        save(user);

        // 生成token返回，实现注册后自动登录
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", getUserInfo(user.getId()));
        return result;
    }

    @Override
    public Map<String, Object> login(String username, String password) {
        // 参数校验
        if (username == null || username.trim().isEmpty()) {
            throw new RuntimeException("用户名不能为空");
        }
        if (password == null || password.isEmpty()) {
            throw new RuntimeException("密码不能为空");
        }

        // 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username.trim());
        User user = getOne(wrapper);

        if (user == null || !BCrypt.checkpw(password, user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 生成token返回
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", getUserInfo(user.getId()));
        return result;
    }

    @Override
    public User getUserInfo(Long userId) {
        User user = getById(userId);
        if (user != null) {
            user.setPassword(null); // 脱敏密码
        }
        return user;
    }

    @Override
    public void updateUserInfo(Long userId, String nickname, String phone) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (nickname != null && !nickname.trim().isEmpty()) {
            user.setNickname(nickname.trim());
        }
        if (phone != null) {
            user.setPhone(phone);
        }
        user.setUpdateTime(LocalDateTime.now());
        updateById(user);
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        // 参数校验
        if (oldPassword == null || oldPassword.isEmpty()) {
            throw new RuntimeException("原密码不能为空");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException("新密码长度不能少于6位");
        }
        if (oldPassword.equals(newPassword)) {
            throw new RuntimeException("新密码不能与原密码相同");
        }

        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!BCrypt.checkpw(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }

        // 更新密码
        user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        user.setUpdateTime(LocalDateTime.now());
        updateById(user);
    }

    @Override
    public Map<String, Object> getNotifications(Long userId) {
        Map<String, Object> result = new HashMap<>();
        // 获取待领取的预约通知
        List<ReserveRecord> readyList = reserveService.getReadyReservations(userId);
        result.put("readyReservations", readyList);
        result.put("hasNotification", !readyList.isEmpty());
        return result;
    }
}
