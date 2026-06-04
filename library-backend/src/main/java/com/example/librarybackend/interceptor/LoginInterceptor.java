package com.example.librarybackend.interceptor;

import com.example.librarybackend.common.UserContext;
import com.example.librarybackend.entity.User;
import com.example.librarybackend.mapper.UserMapper;
import com.example.librarybackend.util.JwtUtil;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * 登录拦截器：校验JWT token，解析用户信息存入ThreadLocal
 */
@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行OPTIONS预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            writeError(response, 401, "未登录，请先登录");
            return false;
        }

        token = token.substring(7);
        try {
            if (jwtUtil.isTokenExpired(token)) {
                writeError(response, 401, "登录已过期，请重新登录");
                return false;
            }

            Long userId = jwtUtil.getUserId(token);
            User user = userMapper.selectById(userId);
            if (user == null) {
                writeError(response, 401, "用户不存在");
                return false;
            }

            // 将用户信息存入ThreadLocal
            UserContext.setUser(user);
            return true;
        } catch (Exception e) {
            writeError(response, 401, "token无效，请重新登录");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求结束后清除ThreadLocal，防止内存泄漏
        UserContext.remove();
    }

    private void writeError(HttpServletResponse response, int code, String message) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        response.setStatus(200);
        response.getWriter().write(objectMapper.writeValueAsString(
                com.example.librarybackend.common.Result.error(code, message)
        ));
    }
}
