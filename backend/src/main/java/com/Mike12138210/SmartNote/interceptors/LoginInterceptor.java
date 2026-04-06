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
        ThreadLocalUtil.set(claims);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        ThreadLocalUtil.remove();
    }
}