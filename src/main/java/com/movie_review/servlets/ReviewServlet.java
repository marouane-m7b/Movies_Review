package com.movie_review.servlets;

import com.google.gson.Gson;
import com.movie_review.dao.ReviewDAO;
import com.movie_review.models.Review;
import com.movie_review.utils.AuthUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet("/reviews")
public class ReviewServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Gson gson = new Gson();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String movieIdStr = request.getParameter("movie_id");
        if (movieIdStr == null || !movieIdStr.matches("\\d+")) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Valid movie_id is required.");
            return;
        }
        int movieId = Integer.parseInt(movieIdStr);
        List<ReviewDAO.ReviewWithUsername> reviews = ReviewDAO.getReviewsByMovieId(movieId);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String jsonResponse = gson.toJson(new SuccessResponse("success", "Reviews fetched successfully!", reviews));
        response.getWriter().write(jsonResponse);
    }

protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!AuthUtils.isAuthenticated(request)) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: Please provide a valid Bearer token.");
            return;
        }

        String movieIdStr = request.getParameter("movie_id");
        int userId = AuthUtils.getUserId(request);
        String ratingStr = request.getParameter("rating");
        String comment = request.getParameter("comment");

        if (movieIdStr == null || !movieIdStr.matches("\\d+")) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Valid movie_id is required.");
            return;
        }
        if (ratingStr == null || !ratingStr.matches("\\d+(\\.5)?")) { // Allow decimals ending in .5
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Rating must be a number between 1 and 5, optionally with .5.");
            return;
        }
        float rating = Float.parseFloat(ratingStr);
        if (rating < 1 || rating > 5) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Rating must be between 1 and 5.");
            return;
        }

        Review review = new Review(Integer.parseInt(movieIdStr), userId, rating, comment != null ? comment.trim() : "");
        boolean success = ReviewDAO.addReview(review);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        if (success) {
            response.setStatus(HttpServletResponse.SC_CREATED);
            String jsonResponse = gson.toJson(new SuccessResponse("success", "Review submitted successfully!", null));
            response.getWriter().write(jsonResponse);
        } else {
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to submit review.");
        }
    }

    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!AuthUtils.isAuthenticated(request)) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: Please provide a valid Bearer token.");
            return;
        }

        String body = request.getReader().lines().collect(Collectors.joining());
        Map<String, String> params = parseFormData(body);

        String reviewIdStr = params.get("review_id");
        String ratingStr = params.get("rating");
        String comment = params.get("comment");

        int userId = AuthUtils.getUserId(request);

        if (reviewIdStr == null || !reviewIdStr.matches("\\d+")) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Valid review_id is required.");
            return;
        }
        if (ratingStr == null || !ratingStr.matches("\\d+(\\.5)?")) { // Allow decimals ending in .5
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Rating must be a number between 0 and 5, optionally with .5.");
            return;
        }
        float rating = Float.parseFloat(ratingStr);
        if (rating < 0 || rating > 5) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Rating must be between 0 and 5.");
            return;
        }

        int reviewId = Integer.parseInt(reviewIdStr);
        Review existingReview = ReviewDAO.getReviewById(reviewId);
        if (existingReview == null || existingReview.getUserId() != userId) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN, "You can only edit your own reviews.");
            return;
        }

        boolean success = ReviewDAO.updateReview(reviewId, userId, rating, comment != null ? comment.trim() : "");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        if (success) {
            String jsonResponse = gson.toJson(new SuccessResponse("success", "Review updated successfully!", null));
            response.getWriter().write(jsonResponse);
        } else {
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to update review.");
        }
    }

    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!AuthUtils.isAuthenticated(request)) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: Please provide a valid Bearer token.");
            return;
        }

        String reviewIdStr = request.getParameter("review_id");
        int userId = AuthUtils.getUserId(request);

        if (reviewIdStr == null || !reviewIdStr.matches("\\d+")) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Valid review_id is required.");
            return;
        }

        int reviewId = Integer.parseInt(reviewIdStr);
        Review existingReview = ReviewDAO.getReviewById(reviewId);
        if (existingReview == null || existingReview.getUserId() != userId) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN, "You can only delete your own reviews.");
            return;
        }

        boolean success = ReviewDAO.deleteReview(reviewId, userId);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        if (success) {
            String jsonResponse = gson.toJson(new SuccessResponse("success", "Review deleted successfully!", null));
            response.getWriter().write(jsonResponse);
        } else {
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to delete review.");
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
    
    private Map<String, String> parseFormData(String formData) {
        Map<String, String> params = new HashMap<>();
        if (formData == null) return params;

        String[] pairs = formData.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                try {
                    String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8.name());
                    String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8.name());
                    params.put(key, value);
                } catch (UnsupportedEncodingException e) {
                    // Handle encoding exception
                }
            }
        }
        return params;
    }
}