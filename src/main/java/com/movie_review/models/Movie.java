package com.movie_review.models;

public class Movie {
    private int movieId;
    private String title;
    private String description;
    private int releaseYear;
    private String genre;
    private String imageUri; // New field
    private double averageRating; // New field
    private int reviewCount;     // New field

    public Movie(String title, String description, int releaseYear, String genre) {
        this.title = title;
        this.description = description;
        this.releaseYear = releaseYear;
        this.genre = genre;
    }

    public Movie(String title, String description, int releaseYear, String genre, String imageUri) {
        this.title = title;
        this.description = description;
        this.releaseYear = releaseYear;
        this.genre = genre;
        this.imageUri = imageUri;
    }
    
    public Movie(int movieId, String title, String description, int releaseYear, String genre, String imageUri, double averageRating, int reviewCount) {
        this.movieId = movieId;
        this.title = title;
        this.description = description;
        this.releaseYear = releaseYear;
        this.genre = genre;
        this.imageUri = imageUri;
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
    }

    public int getMovieId() { return movieId; }
    public void setMovieId(int movieId) { this.movieId = movieId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getReleaseYear() { return releaseYear; }
    public void setReleaseYear(int releaseYear) { this.releaseYear = releaseYear; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public String getImageUri() { return imageUri; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }
    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }
    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
}