package com.restofestro.controller;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.restofestro.dao.UserDAO;
import com.restofestro.model.User;
import com.restofestro.util.PageUtil;
import com.restofestro.util.PasswordUtil;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        String name = req.getParameter("name");
        String email = req.getParameter("email");
        String phone = req.getParameter("phone");
        String password = req.getParameter("password");

        UserDAO userDAO = new UserDAO();

        res.setContentType("text/html;charset=UTF-8");

        if (userDAO.emailExists(email)) {
            PrintWriter out = res.getWriter();
            PageUtil.notice(out, "Already registered",
                    "This email is already registered. Please log in instead.", "login.html", "Go to Login");
            return;
        }

        String hashedPassword = PasswordUtil.hash(password);

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(hashedPassword);

        boolean success = userDAO.registerUser(user);

        if (success) {
            res.sendRedirect("login.html");
        } else {
            PrintWriter out = res.getWriter();
            PageUtil.notice(out, "Registration failed",
                    "Registration failed. Please try again.", "register.html", "Back to Register");
        }
    }
}
