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
import java.util.List;

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
        if (ratingStr == null || !ratingStr.matches("\\d+")) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Rating must be a number.");
            return;
        }
        int rating = Integer.parseInt(ratingStr);
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

        String reviewIdStr = request.getParameter("review_id");
        int userId = AuthUtils.getUserId(request);
        String ratingStr = request.getParameter("rating");
        String comment = request.getParameter("comment");

        if (reviewIdStr == null || !reviewIdStr.matches("\\d+")) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Valid review_id is required.");
            return;
        }
        if (ratingStr == null || !ratingStr.matches("\\d+")) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Rating must be a number.");
            return;
        }
        int rating = Integer.parseInt(ratingStr);
        if (rating < 1 || rating > 5) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Rating must be between 1 and 5.");
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