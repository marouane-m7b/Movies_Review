package com.movie_review.servlets;

import com.google.gson.Gson;
import com.movie_review.utils.AuthUtils;
import com.movie_review.utils.TokenUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Gson gson = new Gson();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!AuthUtils.isAuthenticated(request)) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: Please provide a valid Bearer token.");
            return;
        }

        String authHeader = request.getHeader("Authorization");
        String token = authHeader.substring(7); // Remove "Bearer "
        boolean success = TokenUtils.blacklistToken(token);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        if (success) {
            String jsonResponse = gson.toJson(new SuccessResponse("success", "Logged out successfully!", null));
            response.getWriter().write(jsonResponse);
        } else {
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to logout.");
        }
    }

    private static class SuccessResponse {
        private String status;
        private String message;
        private Object data;
        public SuccessResponse(String status, String message, Object data) {
            this.status = status;
            this.message = message;
            this.data = data;
        }
        public String getStatus() { return status; }
        public String getMessage() { return message; }
        public Object getData() { return data; }
    }

    private static class ErrorResponse {
        private String status;
        private String message;
        public ErrorResponse(String status, String message) {
            this.status = status;
            this.message = message;
        }
        public String getStatus() { return status; }
        public String getMessage() { return message; }
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);
        String jsonResponse = gson.toJson(new ErrorResponse("error", message));
        response.getWriter().write(jsonResponse);
    }
}