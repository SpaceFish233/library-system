package com.example.librarybackend.common;

import com.example.librarybackend.entity.User;

/**
 * 使用ThreadLocal存储当前登录用户信息，方便在各层获取
 */
public class UserContext {
    private static final ThreadLocal<User> USER_HOLDER = new ThreadLocal<>();

    public static void setUser(User user) {
        USER_HOLDER.set(user);
    }

    public static User getUser() {
        return USER_HOLDER.get();
    }

    public static void remove() {
        USER_HOLDER.remove();
    }
}
