package com.equityledger.security.jwt;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
    // 256-bit base64 encoded secret for HMAC-SHA256
    private static final String SECRET = "4qhq8LrEBfYcaRHxhdb9zURb2rf8e7Ud+8lO0Yw2/xI=";
    private static final long ACCESS_EXPIRATION = 1000 * 60 * 15; // 15 mins
    private static final long REFRESH_EXPIRATION = 1000 * 60 * 60 * 24 * 7; // 7 days

    public String generateAccessToken(String username, String role) {
        return buildToken(username, role, ACCESS_EXPIRATION);
    }

    public String generateRefreshToken(String username) {
        return buildToken(username, null, REFRESH_EXPIRATION);
    }

    private String buildToken(String username, String role, long expiration) {
        Map<String, Object> claims = new HashMap<>();
        if (role != null) claims.put("role", role);

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }
    
    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
    }
}