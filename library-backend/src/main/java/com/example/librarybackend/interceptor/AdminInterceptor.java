package com.example.librarybackend.interceptor;

import com.example.librarybackend.annotation.RequireAdmin;
import com.example.librarybackend.common.Result;
import com.example.librarybackend.common.UserContext;
import com.example.librarybackend.entity.User;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * 管理员权限拦截器：校验是否有@RequireAdmin注解，有则检查角色
 */
@Component
@RequiredArgsConstructor
public class AdminInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequireAdmin annotation = handlerMethod.getMethodAnnotation(RequireAdmin.class);
        if (annotation == null) {
            return true;
        }

        User user = UserContext.getUser();
        if (user == null || !"admin".equals(user.getRole())) {
            writeError(response, 403, "权限不足，需要管理员权限");
            return false;
        }

        return true;
    }

    private void writeError(HttpServletResponse response, int code, String message) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        response.setStatus(200);
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(code, message)));
    }
}
