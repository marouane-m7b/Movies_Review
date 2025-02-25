package com.movie_review.servlets;

import com.google.gson.Gson;
import com.movie_review.dao.UserDAO;
import com.movie_review.models.User;
import com.movie_review.utils.AuthUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Gson gson = new Gson();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        System.out.println("Auth Header: " + authHeader); // Debug

        if (!AuthUtils.isAuthenticated(request)) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: Please provide a valid Bearer token.");
            return;
        }

        int userId = AuthUtils.getUserId(request);
        System.out.println("Extracted User ID: " + userId); // Debug

        User user = UserDAO.getUserById(userId);
        if (user != null) {
            user.setUserId(userId); // Ensure userId is set in the response
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        if (user != null) {
            String jsonResponse = gson.toJson(new SuccessResponse("success", "Profile fetched successfully!", user));
            response.getWriter().write(jsonResponse);
        } else {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "User not found.");
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