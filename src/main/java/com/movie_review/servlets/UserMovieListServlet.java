package com.movie_review.servlets;

import com.google.gson.Gson;
import com.movie_review.dao.UserMovieListDAO;
import com.movie_review.models.Movie;
import com.movie_review.utils.AuthUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/lists")
public class UserMovieListServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Gson gson = new Gson();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!AuthUtils.isAuthenticated(request)) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: Please provide a valid Bearer token.");
            return;
        }

        int userId = AuthUtils.getUserId(request);
        String listType = request.getParameter("type");
        if (listType == null || !isValidListType(listType)) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Valid list type (watchlist, watched, dropped, favorites) is required.");
            return;
        }

        List<Movie> movies = UserMovieListDAO.getDetailedListByUser(userId, listType);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String jsonResponse = gson.toJson(new SuccessResponse("success", listType + " fetched successfully!", movies));
        response.getWriter().write(jsonResponse);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!AuthUtils.isAuthenticated(request)) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: Please provide a valid Bearer token.");
            return;
        }

        int userId = AuthUtils.getUserId(request);
        String movieIdStr = request.getParameter("movie_id");
        String listType = request.getParameter("type");

        if (movieIdStr == null || !movieIdStr.matches("\\d+")) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Valid movie_id is required.");
            return;
        }
        if (listType == null || !isValidListType(listType)) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Valid list type (watchlist, watched, dropped, favorites) is required.");
            return;
        }

        int movieId = Integer.parseInt(movieIdStr);
        boolean success = UserMovieListDAO.addToList(userId, movieId, listType);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        if (success) {
            response.setStatus(HttpServletResponse.SC_CREATED);
            String jsonResponse = gson.toJson(new SuccessResponse("success", "Movie added to " + listType + "!", null));
            response.getWriter().write(jsonResponse);
        } else {
            sendError(response, HttpServletResponse.SC_CONFLICT, "Movie already in " + listType + " or failed to add.");
        }
    }

    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!AuthUtils.isAuthenticated(request)) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: Please provide a valid Bearer token.");
            return;
        }

        int userId = AuthUtils.getUserId(request);
        String movieIdStr = request.getParameter("movie_id");
        String listType = request.getParameter("type");

        if (movieIdStr == null || !movieIdStr.matches("\\d+")) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Valid movie_id is required.");
            return;
        }
        if (listType == null || !isValidListType(listType)) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Valid list type (watchlist, watched, dropped, favorites) is required.");
            return;
        }

        int movieId = Integer.parseInt(movieIdStr);
        boolean success = UserMovieListDAO.removeFromList(userId, movieId, listType);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        if (success) {
            String jsonResponse = gson.toJson(new SuccessResponse("success", "Movie removed from " + listType + "!", null));
            response.getWriter().write(jsonResponse);
        } else {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Movie not found in " + listType + " or failed to remove.");
        }
    }

    private boolean isValidListType(String listType) {
        return "watchlist".equals(listType) || "watched".equals(listType) || "dropped".equals(listType) || "favorites".equals(listType);
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