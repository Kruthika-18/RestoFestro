package com.restofestro.admin;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.restofestro.dao.AdminDAO;
import com.restofestro.util.PageUtil;
import com.restofestro.util.PasswordUtil;

@WebServlet("/adminLogin")
public class AdminLoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String hashedPassword = PasswordUtil.hash(password);

        AdminDAO adminDAO = new AdminDAO();
        int adminId = adminDAO.validateAdmin(email, hashedPassword);

        if (adminId != -1) {
            HttpSession session = req.getSession();
            session.setAttribute("adminId", adminId);
            res.sendRedirect("admin/dashboard.html");
        } else {
            res.setContentType("text/html;charset=UTF-8");
            PrintWriter out = res.getWriter();
            PageUtil.notice(out, "Admin login failed", "Invalid admin credentials.",
                    "admin/admin-login.html", "Try again");
        }
    }
}
