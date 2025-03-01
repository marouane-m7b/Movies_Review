package com.movie_review.dao;

import com.movie_review.models.Movie; // Import the Movie model
import com.movie_review.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserMovieListDAO {

    public static boolean addToList(int userId, int movieId, String listType) {
        String query = "INSERT INTO user_movie_lists (user_id, movie_id, list_type) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, movieId);
            stmt.setString(3, listType);
            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            if (e.getSQLState().equals("23000")) { // Duplicate entry
                return false; // Already in list
            }
            e.printStackTrace();
            return false;
        }
    }

    public static boolean removeFromList(int userId, int movieId, String listType) {
        String query = "DELETE FROM user_movie_lists WHERE user_id = ? AND movie_id = ? AND list_type = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, movieId);
            stmt.setString(3, listType);
            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<Integer> getListByUser(int userId, String listType) {
        List<Integer> movieIds = new ArrayList<>();
        String query = "SELECT movie_id FROM user_movie_lists WHERE user_id = ? AND list_type = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setString(2, listType);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    movieIds.add(rs.getInt("movie_id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movieIds;
    }

	public static List<Movie> getDetailedListByUser(int userId, String listType) {
	    List<Movie> movies = new ArrayList<>();
	    List<Integer> movieIds = new ArrayList<>();
	    String query = "SELECT m.movie_id, m.title, m.description, m.release_year, m.genre, m.image_uri " +
	                  "FROM user_movie_lists uml JOIN movies m ON uml.movie_id = m.movie_id " +
	                  "WHERE uml.user_id = ? AND uml.list_type = ?";
	    
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(query)) {
	        stmt.setInt(1, userId);
	        stmt.setString(2, listType);
	        
	        System.out.println("Fetching movies for user " + userId + " and list type " + listType + "...");
	        int count = 0;
	        
	        try (ResultSet rs = stmt.executeQuery()) {
	            // First, collect all movie data without rating info
	            while (rs.next()) {
	                count++;
	                int movieId = rs.getInt("movie_id");
	                String title = rs.getString("title");
	                String description = rs.getString("description");
	                int releaseYear = rs.getInt("release_year");
	                String genre = rs.getString("genre");
	                String imageUri = rs.getString("image_uri");
	                
	                // Create movie without rating info for now
	                Movie movie = new Movie(movieId, title, description, releaseYear, genre, imageUri, 0.0, 0);
	                movies.add(movie);
	                movieIds.add(movieId);
	                
	                System.out.println("Added movie: " + title);
	            }
	            System.out.println("Total movies fetched: " + count);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return movies; // Return what we have so far if there's an error
	    }
	    
	    // Now fetch rating info for all movies after the ResultSet is closed
	    for (int i = 0; i < movies.size(); i++) {
	        Movie movie = movies.get(i);
	        int movieId = movieIds.get(i);
	        
	        try {
	            ReviewDAO.RatingInfo ratingInfo = ReviewDAO.getRatingInfo(movieId);
	            // Update the movie with rating information
	            movie.setAverageRating(ratingInfo.averageRating);
	            movie.setReviewCount(ratingInfo.reviewCount);
	        } catch (Exception e) {
	            System.out.println("Error fetching rating for movie ID " + movieId + ": " + e.getMessage());
	            // Continue processing other movies even if one fails
	        }
	    }
	    
	    return movies;
	}

}