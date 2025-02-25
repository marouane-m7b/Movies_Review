package com.movie_review.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.movie_review.models.User;
import com.movie_review.utils.DBConnection;
import com.movie_review.utils.PasswordUtils;

public class UserDAO {

    // Register new user
	public static boolean registerUser(User user) {
	    String query = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
	    Connection conn = null;
	    PreparedStatement stmt = null;
	
	    try {
	        conn = DBConnection.getConnection();
	        stmt = conn.prepareStatement(query);
	
	        stmt.setString(1, user.getUsername());
	        stmt.setString(2, user.getPassword());
	        stmt.setString(3, user.getEmail());
	
	        int rowsInserted = stmt.executeUpdate();
	        return rowsInserted > 0;
	
	    } catch (SQLException e) {
	        e.printStackTrace(); // Print SQL error
	        return false;
	    } finally {
	        // Close resources
	        try {
	            if (stmt != null) stmt.close();
	            if (conn != null) conn.close(); // Close connection to avoid leaks
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }
	}

	public static boolean validateUser(String username, String password) {
	    boolean isValid = false;
	    try (Connection connection = DBConnection.getConnection()) {
	        String sql = "SELECT password FROM users WHERE username = ?";
	        try (PreparedStatement statement = connection.prepareStatement(sql)) {
	            statement.setString(1, username);
	            try (ResultSet resultSet = statement.executeQuery()) {
	                if (resultSet.next()) {
	                    String hashedPassword = resultSet.getString("password");
	                    // Verify the password (assuming you use a method for hashing)
	                    isValid = PasswordUtils.verifyPassword(password, hashedPassword);
	                }
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return isValid;
	}


    // Check if username exists
    public static boolean isUsernameTaken(String username) {
        String query = "SELECT user_id FROM users WHERE username = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // If there is a result, username is taken

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // Check if email exists
    public static boolean isEmailTaken(String email) {
        String query = "SELECT user_id FROM users WHERE email = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // If there is a result, email is taken

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static User getUserById(int userId) {
        String query = "SELECT user_id, username, email, is_admin FROM users WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User(rs.getInt("user_id"), rs.getString("username"), rs.getString("email"));
                    user.setAdmin(rs.getBoolean("is_admin"));
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
