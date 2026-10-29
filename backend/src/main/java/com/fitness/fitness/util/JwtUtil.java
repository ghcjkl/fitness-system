package com.fitness.fitness.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expire}")
    private Long expire;

    // 获取密钥
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // 生成token，传入用户id
    public String generateToken(Long userId) {
        long now = System.currentTimeMillis();
        Date expireDate = new Date(now + expire);
        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(new Date(now))
                .setExpiration(expireDate)
                .signWith(getSecretKey())
                .compact();
    }

    // 解析token获取用户id
    public Long getUserId(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return Long.parseLong(claims.getSubject());
    }

    // 校验token是否过期/非法
    public boolean verifyToken(String token) {
        try {
            getUserId(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}