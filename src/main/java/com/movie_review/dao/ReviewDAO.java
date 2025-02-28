package com.movie_review.dao;

import com.movie_review.models.Review;
import com.movie_review.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReviewDAO {

    // Add a new review
    public static boolean addReview(Review review) {
        String query = "INSERT INTO reviews (movie_id, user_id, rating, comment) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, review.getMovieId());
            stmt.setInt(2, review.getUserId());
            stmt.setInt(3, review.getRating());
            stmt.setString(4, review.getComment());
            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get all reviews for a specific movie
    public static List<ReviewWithUsername> getReviewsByMovieId(int movieId) {
        List<ReviewWithUsername> reviews = new ArrayList<>();
        String query = "SELECT r.*, u.username FROM reviews r JOIN users u ON r.user_id = u.user_id WHERE r.movie_id = ? ORDER BY r.review_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, movieId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ReviewWithUsername review = new ReviewWithUsername(
                        rs.getInt("movie_id"),
                        rs.getInt("user_id"),
                        rs.getInt("rating"),
                        rs.getString("comment"),
                        rs.getString("username")
                    );
                    review.setReviewId(rs.getInt("review_id"));
                    review.setReviewDate(rs.getTimestamp("review_date"));
                    reviews.add(review);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reviews;
    }

    public static class ReviewWithUsername extends Review {
        private String username;

        public ReviewWithUsername(int movieId, int userId, int rating, String comment, String username) {
            super(movieId, userId, rating, comment);
            this.username = username;
        }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
    }
    
    public static boolean updateReview(int reviewId, int userId, int rating, String comment) {
        String query = "UPDATE reviews SET rating = ?, comment = ? WHERE review_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, rating);
            stmt.setString(2, comment);
            stmt.setInt(3, reviewId);
            stmt.setInt(4, userId);
            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Review getReviewById(int reviewId) {
        String query = "SELECT * FROM reviews WHERE review_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, reviewId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Review review = new Review(
                        rs.getInt("movie_id"),
                        rs.getInt("user_id"),
                        rs.getInt("rating"),
                        rs.getString("comment")
                    );
                    review.setReviewId(rs.getInt("review_id"));
                    review.setReviewDate(rs.getTimestamp("review_date"));
                    return review;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static class RatingInfo {
        public double averageRating;
        public int reviewCount;

        public RatingInfo(double averageRating, int reviewCount) {
            this.averageRating = averageRating;
            this.reviewCount = reviewCount;
        }
    }

    public static RatingInfo getRatingInfo(int movieId) {
        String sql = "SELECT AVG(rating) AS avg_rating, COUNT(*) AS review_count FROM reviews WHERE movie_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, movieId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double avgRating = rs.getDouble("avg_rating");
                int reviewCount = rs.getInt("review_count");
                return new RatingInfo(avgRating, reviewCount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new RatingInfo(0.0, 0); // Default if no reviews
    }
}