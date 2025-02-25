package com.movie_review.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import com.movie_review.dao.UserDAO;
import com.movie_review.utils.TokenUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.movie_review.utils.DBConnection;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Gson gson = new Gson();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String csrfToken = TokenUtils.generateCsrfToken();
        request.getSession().setAttribute("csrf_token", csrfToken);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String jsonResponse = gson.toJson(new CsrfResponse(csrfToken));
        response.getWriter().write(jsonResponse);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String csrfToken = request.getParameter("csrf_token");

        String sessionCsrf = (String) request.getSession().getAttribute("csrf_token");
        if (csrfToken == null || !csrfToken.equals(sessionCsrf)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            String jsonResponse = gson.toJson(new ResponseMessage("Invalid CSRF token."));
            response.getWriter().write(jsonResponse);
            return;
        }

        boolean success = UserDAO.validateUser(username, password);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String jsonResponse;
        if (success) {
            int userId = getUserId(username);
            if (userId != -1) {
                String bearerToken = TokenUtils.generateToken(userId, username);
                jsonResponse = gson.toJson(new LoginResponse("Login successful!", bearerToken));
            } else {
                jsonResponse = gson.toJson(new ResponseMessage("Error fetching user data."));
            }
        } else {
            jsonResponse = gson.toJson(new ResponseMessage("Invalid username or password."));
        }

        response.getWriter().write(jsonResponse);
    }

    private int getUserId(String username) {
        String query = "SELECT user_id FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("user_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private static class ResponseMessage {
        private String message;
        public ResponseMessage(String message) { this.message = message; }
        public String getMessage() { return message; }
    }

    private static class CsrfResponse {
        private String csrfToken;
        public CsrfResponse(String csrfToken) { this.csrfToken = csrfToken; }
        public String getCsrfToken() { return csrfToken; }
    }

    private static class LoginResponse {
        private String message;
        private String token;
        public LoginResponse(String message, String token) { this.message = message; this.token = token; }
        public String getMessage() { return message; }
        public String getToken() { return token; }
    }
}