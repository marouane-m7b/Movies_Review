package com.movie_review.servlets;

import com.google.gson.Gson;
import com.movie_review.dao.MovieDAO;
import com.movie_review.models.Movie;
import com.movie_review.utils.AuthUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/movies")
public class MovieServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Gson gson = new Gson();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Movie> movies = MovieDAO.getAllMovies();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String jsonResponse = gson.toJson(new SuccessResponse("success", "Movies fetched successfully!", movies));
        response.getWriter().write(jsonResponse);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!AuthUtils.isAuthenticated(request)) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: Please provide a valid Bearer token.");
            return;
        }
        if (!AuthUtils.isAdmin(request)) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN, "Only admins can add movies.");
            return;
        }

        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String releaseYearStr = request.getParameter("release_year");
        String genre = request.getParameter("genre");

        if (title == null || title.trim().isEmpty()) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Title is required.");
            return;
        }
        if (releaseYearStr == null || !releaseYearStr.matches("\\d{4}")) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Release year must be a valid 4-digit year.");
            return;
        }
        int releaseYear = Integer.parseInt(releaseYearStr);
        if (releaseYear < 1888 || releaseYear > 2100) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Release year must be between 1888 and 2100.");
            return;
        }

        Movie movie = new Movie(title.trim(), description != null ? description.trim() : "", releaseYear, genre != null ? genre.trim() : "");
        boolean success = MovieDAO.addMovie(movie);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        if (success) {
            response.setStatus(HttpServletResponse.SC_CREATED);
            String jsonResponse = gson.toJson(new SuccessResponse("success", "Movie added successfully!", movie));
            response.getWriter().write(jsonResponse);
        } else {
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to add movie.");
        }
    }

    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!AuthUtils.isAuthenticated(request)) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: Please provide a valid Bearer token.");
            return;
        }
        if (!AuthUtils.isAdmin(request)) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN, "Only admins can delete movies.");
            return;
        }

        String movieIdStr = request.getParameter("movie_id");
        if (movieIdStr == null || !movieIdStr.matches("\\d+")) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Valid movie_id is required.");
            return;
        }
        int movieId = Integer.parseInt(movieIdStr);

        boolean success = MovieDAO.deleteMovie(movieId);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        if (success) {
            String jsonResponse = gson.toJson(new SuccessResponse("success", "Movie deleted successfully!", null));
            response.getWriter().write(jsonResponse);
        } else {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Movie not found or failed to delete.");
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