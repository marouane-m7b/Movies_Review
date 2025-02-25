package com.movie_review.utils;

import com.movie_review.dao.UserDAO;
import com.movie_review.models.User;

import jakarta.servlet.http.HttpServletRequest;

public class AuthUtils {
    public static boolean isAuthenticated(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }
        String token = authHeader.substring(7);
        return TokenUtils.getUserIdFromToken(token) != null;
    }

    public static int getUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return -1;
        }
        String token = authHeader.substring(7);
        Integer userId = TokenUtils.getUserIdFromToken(token);
        return userId != null ? userId : -1;
    }

    public static boolean isAdmin(HttpServletRequest request) {
        int userId = getUserId(request);
        if (userId == -1) {
            return false;
        }
        User user = UserDAO.getUserById(userId);
        return user != null && user.isAdmin();
    }
}