package com.Mike12138210.SmartNote.interceptors;

import com.Mike12138210.SmartNote.utils.JwtUtil;
import com.Mike12138210.SmartNote.utils.ThreadLocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 放行 OPTIONS 请求（CORS 预检）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            response.setStatus(401);
            return false;
        }
        // 去除 "Bearer " 前缀（如果存在）
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Map<String, Object> claims = jwtUtil.parseToken(token);
        if (claims == null) {
            response.setStatus(401);
            return false;
        }
        Object userIdObj = claims.get("userId");
        if(userIdObj == null){
            response.setStatus(401);
            return false;
        }
        Long userId = ((Number)userIdObj).longValue();
        ThreadLocalUtil.set(userId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        ThreadLocalUtil.remove();
    }
}