package com.movie_review.utils;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TokenUtils {
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(
        "your-very-secure-secret-key-here-at-least-32-chars".getBytes(StandardCharsets.UTF_8)
    );
    private static final long EXPIRATION_TIME = 1000 * 60 * 60;

    public static String generateToken(int userId, String username) {
        return Jwts.builder()
                .subject(username)
                .claim("user_id", userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY, Jwts.SIG.HS256)
                .compact();
    }

    public static Integer getUserIdFromToken(String token) {
        if (isTokenBlacklisted(token)) {
            return null; // Blacklisted tokens are invalid
        }
        try {
            return Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("user_id", Integer.class);
        } catch (JwtException e) {
            return null;
        }
    }

    public static String generateCsrfToken() {
        return java.util.UUID.randomUUID().toString();
    }

    public static boolean blacklistToken(String token) {
        String query = "INSERT INTO token_blacklist (token, expiry) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, token);
            stmt.setTimestamp(2, new java.sql.Timestamp(System.currentTimeMillis() + EXPIRATION_TIME));
            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean isTokenBlacklisted(String token) {
        String query = "SELECT COUNT(*) FROM token_blacklist WHERE token = ? AND expiry > NOW()";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, token);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}