package com.movie_review.servlets;

import com.google.gson.Gson;
import com.movie_review.dao.MovieDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/movies/search")
public class MovieSearchServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Gson gson = new Gson();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String query = request.getParameter("q");
        if (query == null || query.trim().isEmpty()) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Search query is required.");
            return;
        }

        java.util.List<com.movie_review.models.Movie> movies = MovieDAO.searchMovies(query.trim());

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String jsonResponse = gson.toJson(new SuccessResponse("success", "Movies found!", movies));
        response.getWriter().write(jsonResponse);
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