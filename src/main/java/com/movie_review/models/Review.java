package com.movie_review.models;

import java.sql.Timestamp;

public class Review {
    private int reviewId;
    private int movieId;
    private int userId;
    private int rating;
    private String comment;
    private Timestamp reviewDate;

    // Constructor
    public Review(int movieId, int userId, int rating, String comment) {
        this.movieId = movieId;
        this.userId = userId;
        this.rating = rating;
        this.comment = comment;
    }

    // Getters and Setters
    public int getReviewId() {
        return reviewId;
    }

    public void setReviewId(int reviewId) {
        this.reviewId = reviewId;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Timestamp getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(Timestamp reviewDate) {
        this.reviewDate = reviewDate;
    }
}