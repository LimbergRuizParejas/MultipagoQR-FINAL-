package com.example.multipagos.util;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import io.jsonwebtoken.*;

import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@Component
public class JwtUtil {
    @Value("${app.jwtSecret:MyJwtSecretKeyChangeThis}")
    private String jwtSecret;

    @Value("${app.jwtExpirationMs:3600000}")
    private int jwtExpirationMs;

    public String generateToken(String username, List<Long> companyIds, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("companyIds", companyIds);
        claims.put("roles", roles);
        return Jwts.builder()
                .setSubject(username)
                .addClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .compact();
    }

    public Claims getClaims(String token) {
        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
    }

    public boolean validate(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }
}
