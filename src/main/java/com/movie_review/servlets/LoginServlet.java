package com.movie_review.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import com.movie_review.dao.UserDAO;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Gson gson = new Gson();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        boolean success = UserDAO.validateUser(username, password);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String jsonResponse;
        if (success) {
            jsonResponse = gson.toJson(new ResponseMessage("Login successful!"));
        } else {
            jsonResponse = gson.toJson(new ResponseMessage("Invalid username or password."));
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
