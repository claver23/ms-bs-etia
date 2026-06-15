package com.etlions.ms_bs_etia.auth.service;

import com.etlions.ms_bs_etia.auth.persistence.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(
            @Value("${etia.jwt.secret}") String secret,
            @Value("${etia.jwt.expiration-hours:24}") long expirationHours) {
        // Pad secret to at least 32 bytes for HMAC-SHA256
        String padded = secret;
        while (padded.getBytes(StandardCharsets.UTF_8).length < 32) {
            padded = padded + secret;
        }
        this.key = Keys.hmacShaKeyFor(padded.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationHours * 3600 * 1000;
    }

    public String generateToken(UserEntity user) {
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("companyId", user.getCompanyId() != null ? user.getCompanyId() : "")
                .claim("rol", user.getRol())
                .claim("userId", user.getId())
                .claim("nombre", user.getNombre())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractCompanyId(String token) {
        return extractAllClaims(token).get("companyId", String.class);
    }

    public String extractRol(String token) {
        return extractAllClaims(token).get("rol", String.class);
    }

    public String extractUserId(String token) {
        return extractAllClaims(token).get("userId", String.class);
    }
}

