package com.fitness.fitness.config;

import com.fitness.fitness.entity.SysLog;
import com.fitness.fitness.entity.User;
import com.fitness.fitness.mapper.SysLogMapper;
import com.fitness.fitness.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

/**
 * 记录管理员的所有写操作，成功失败都留痕
 */
@Component
public class AdminLogInterceptor implements HandlerInterceptor {

    private final SysLogMapper sysLogMapper;
    private final UserMapper userMapper;

    public AdminLogInterceptor(SysLogMapper sysLogMapper, UserMapper userMapper) {
        this.sysLogMapper = sysLogMapper;
        this.userMapper = userMapper;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        String method = request.getMethod();
        if (!"POST".equals(method) && !"PUT".equals(method) && !"DELETE".equals(method)) {
            return;
        }
        try {
            Long userId = (Long) request.getAttribute("loginUserId");
            User user = userId == null ? null : userMapper.selectById(userId);
            SysLog log = new SysLog();
            log.setUserId(userId);
            log.setUsername(user == null ? null : user.getUsername());
            log.setAction(method + " " + request.getRequestURI());
            log.setMethod(method);
            log.setUri(request.getRequestURI());
            log.setIp(clientIp(request));
            log.setStatus(response.getStatus());
            log.setCreateTime(LocalDateTime.now());
            sysLogMapper.insert(log);
        } catch (Exception ignored) {
            // 日志失败不影响主流程
        }
    }

    private String clientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            return ip.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
