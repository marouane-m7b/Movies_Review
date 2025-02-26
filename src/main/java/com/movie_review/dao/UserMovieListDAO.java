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
        String query = "SELECT m.* FROM user_movie_lists uml JOIN movies m ON uml.movie_id = m.movie_id WHERE uml.user_id = ? AND uml.list_type = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setString(2, listType);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Movie movie = new Movie(
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getInt("release_year"),
                        rs.getString("genre"),
                        rs.getString("image_uri")
                    );
                    movie.setMovieId(rs.getInt("movie_id"));
                    movies.add(movie);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movies;
    }
}