package com.movie_review.servlets;

import java.io.IOException;
import java.sql.Connection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import com.movie_review.utils.DBConnection;

@WebServlet("/test-db") // URL to access this servlet
public class TestDBServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        
        try {
            Connection conn = DBConnection.getConnection();
            if (conn != null) {
                response.getWriter().println("<h3>✅ Database Connection Successful!</h3>");
            } else {
                response.getWriter().println("<h3>❌ Database Connection Failed!</h3>");
            }
        } catch (Exception e) {
            response.getWriter().println("<h3>❌ Error: " + e.getMessage() + "</h3>");
        }
    }
}
