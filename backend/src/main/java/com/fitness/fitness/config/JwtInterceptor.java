package com.fitness.fitness.config;

import com.fitness.fitness.util.JwtUtil;
import com.fitness.fitness.vo.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    public JwtInterceptor(JwtUtil jwtUtil, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行登录、注册接口
        String uri = request.getRequestURI();
        if (uri.contains("/user/login") || uri.contains("/user/register")) {
            return true;
        }

        String token = request.getHeader("token");
        if (token == null || token.isEmpty()) {
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write(objectMapper.writeValueAsString(Result.fail("未登录，请先登录")));
            return false;
        }
        try {
            // 校验token
            Long userId = jwtUtil.getUserId(token);
            request.setAttribute("loginUserId", userId);
            return true;
        } catch (Exception e) {
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write(objectMapper.writeValueAsString(Result.fail("token失效，请重新登录")));
            return false;
        }
    }
}