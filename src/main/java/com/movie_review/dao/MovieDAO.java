package com.movie_review.dao;

import com.movie_review.models.Movie;
import com.movie_review.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MovieDAO {

    // Add a new movie
    public static boolean addMovie(Movie movie) {
        String query = "INSERT INTO movies (title, description, release_year, genre) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, movie.getTitle());
            stmt.setString(2, movie.getDescription());
            stmt.setInt(3, movie.getReleaseYear());
            stmt.setString(4, movie.getGenre());
            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get all movies
    public static List<Movie> getAllMovies() {
        List<Movie> movies = new ArrayList<>();
        String query = "SELECT * FROM movies";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Movie movie = new Movie(
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getInt("release_year"),
                    rs.getString("genre")
                );
                movie.setMovieId(rs.getInt("movie_id"));
                movies.add(movie);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movies;
    }
    
    public static boolean deleteMovie(int movieId) {
        String query = "DELETE FROM movies WHERE movie_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, movieId);
            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get movie by ID
    public static Movie getMovieById(int movieId) {
        String query = "SELECT * FROM movies WHERE movie_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, movieId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Movie movie = new Movie(
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getInt("release_year"),
                        rs.getString("genre")
                    );
                    movie.setMovieId(rs.getInt("movie_id"));
                    return movie;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static List<Movie> searchMovies(String query) {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT * FROM movies WHERE title LIKE ? OR genre LIKE ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String searchTerm = "%" + query + "%";
            stmt.setString(1, searchTerm);
            stmt.setString(2, searchTerm);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Movie movie = new Movie(
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getInt("release_year"),
                        rs.getString("genre")
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