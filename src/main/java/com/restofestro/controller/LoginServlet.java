package com.restofestro.controller;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.restofestro.dao.UserDAO;
import com.restofestro.model.User;
import com.restofestro.util.PageUtil;
import com.restofestro.util.PasswordUtil;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String email = req.getParameter("email");
        String password = req.getParameter("password");

        String hashedPassword = PasswordUtil.hash(password);

        UserDAO userDAO = new UserDAO();
        User user = userDAO.validateLogin(email, hashedPassword);

        if (user != null) {
            HttpSession session = req.getSession();
            session.setAttribute("user", user);
            session.setAttribute("userId", user.getId());

            res.sendRedirect("home.html");
        } else {
            res.setContentType("text/html;charset=UTF-8");
            PrintWriter out = res.getWriter();
            PageUtil.notice(out, "Login failed", "Invalid email or password.", "login.html", "Try again");
        }
    }
}
