package com.Mike12138210.SmartNote.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Date;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class JwtUtil {
    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secretBase64;

    @Value("${jwt.expiration}")
    private Long expiration;

    private Algorithm algorithm;

    @PostConstruct
    public void init() {
        // 将 Base64 编码的密钥解码为字节数组
        byte[] keyBytes = Base64.getDecoder().decode(secretBase64);
        algorithm = Algorithm.HMAC256(keyBytes);
        log.info("JWT 初始化成功，过期时间: {} 毫秒", expiration);
    }

    // 生成 JWT，将 claims 嵌套在 "claims" 字段中（兼容现有的拦截器）
    public String genToken(Map<String, Object> claims) {
        return JWT.create()
                .withClaim("claims", claims)
                .withExpiresAt(new Date(System.currentTimeMillis() + expiration))
                .sign(algorithm);
    }

     // 解析 JWT，返回 claims 中的 Map
     // @return 若解析失败返回 null
    public Map<String, Object> parseToken(String token) {
        try {
            DecodedJWT jwt = JWT.require(algorithm).build().verify(token);
            return jwt.getClaim("claims").asMap();
        } catch (Exception e) {
            log.error("JWT解析失败",e);
            return null;
        }
    }
}