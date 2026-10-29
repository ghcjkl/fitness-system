package com.fitness.fitness.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.fitness.entity.User;
import com.fitness.fitness.mapper.UserMapper;
import com.fitness.fitness.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminInterceptor implements HandlerInterceptor {

    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    public AdminInterceptor(UserMapper userMapper, ObjectMapper objectMapper) {
        this.userMapper = userMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Long userId = (Long) request.getAttribute("loginUserId");
        User user = userId == null ? null : userMapper.selectById(userId);
        if (user == null || !"ADMIN".equals(user.getRole())
                || (user.getStatus() != null && user.getStatus() == 0)) {
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write(objectMapper.writeValueAsString(Result.fail("无管理员权限")));
            return false;
        }
        return true;
    }
}
