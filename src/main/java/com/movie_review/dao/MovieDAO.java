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
    public static boolean addMovie(Movie movie) {
        String query = "INSERT INTO movies (title, description, release_year, genre, image_uri) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, movie.getTitle());
            stmt.setString(2, movie.getDescription());
            stmt.setInt(3, movie.getReleaseYear());
            stmt.setString(4, movie.getGenre());
            stmt.setString(5, movie.getImageUri());
            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

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
                    rs.getString("genre"),
                    rs.getString("image_uri")
                );
                movie.setMovieId(rs.getInt("movie_id"));
                movies.add(movie);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movies;
    }

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
                        rs.getString("genre"),
                        rs.getString("image_uri")
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

    public static List<TopMovie> getTop10Movies() {
        List<TopMovie> topMovies = new ArrayList<>();
        String query = "SELECT m.movie_id, m.title, m.description, m.release_year, m.genre, m.image_uri, AVG(r.rating) as avg_rating " +
                       "FROM movies m LEFT JOIN reviews r ON m.movie_id = r.movie_id " +
                       "GROUP BY m.movie_id, m.title, m.description, m.release_year, m.genre, m.image_uri " +
                       "ORDER BY avg_rating DESC, COUNT(r.rating) DESC " +
                       "LIMIT 10";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Movie movie = new Movie(
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getInt("release_year"),
                    rs.getString("genre"),
                    rs.getString("image_uri")
                );
                movie.setMovieId(rs.getInt("movie_id"));
                double avgRating = rs.getDouble("avg_rating");
                topMovies.add(new TopMovie(movie, avgRating));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return topMovies;
    }

    public static class TopMovie {
        private Movie movie;
        private double averageRating;

        public TopMovie(Movie movie, double averageRating) {
            this.movie = movie;
            this.averageRating = averageRating;
        }

        public Movie getMovie() { return movie; }
        public double getAverageRating() { return averageRating; }
    }
}