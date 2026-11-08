package com.restofestro.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.restofestro.util.DBConnection;

@WebServlet("/test")
public class TestServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        res.setContentType("text/html");
        PrintWriter out = res.getWriter();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT name, state, price FROM food");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                out.println(rs.getString("name") + " - " + rs.getString("state")
                        + " - Rs." + rs.getDouble("price") + "<br>");
            }

        } catch (Exception e) {
            out.println("Error: " + e.getMessage());
        }
    }
}