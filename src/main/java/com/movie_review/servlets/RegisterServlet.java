package com.movie_review.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import com.google.gson.Gson; // Make sure to add Gson to your project dependencies
import com.movie_review.dao.UserDAO;
import com.movie_review.models.User;
import com.movie_review.utils.PasswordUtils;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Gson gson = new Gson();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String email = request.getParameter("email");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String jsonResponse;
        if (UserDAO.isUsernameTaken(username)) {
            jsonResponse = gson.toJson(new ResponseMessage("Username already exists. Try another."));
            response.getWriter().write(jsonResponse);
            return;
        }

        if (UserDAO.isEmailTaken(email)) {
            jsonResponse = gson.toJson(new ResponseMessage("Email already exists. Try another."));
            response.getWriter().write(jsonResponse);
            return;
        }
        
        String hashedPassword = PasswordUtils.hashPassword(password);
        User user = new User(username, hashedPassword, email);
        boolean success = UserDAO.registerUser(user);
        if (success) {
            jsonResponse = gson.toJson(new ResponseMessage("Registration successful! You can now log in."));
        } else {
            jsonResponse = gson.toJson(new ResponseMessage("Registration failed. Try again."));
        }

        response.getWriter().write(jsonResponse);
    }

    private static class ResponseMessage {
        private String message;

        public ResponseMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
