package com.restofestro.admin;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.restofestro.dao.OrderDAO;
import com.restofestro.model.Order;
import com.restofestro.util.PageUtil;

@WebServlet("/adminOrders")
public class AdminOrderServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String[] STATUSES = { "Confirmed", "Preparing", "Out for Delivery", "Delivered" };

    private boolean notAdmin(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("adminId") == null) {
            res.sendRedirect("admin/admin-login.html");
            return true;
        }
        return false;
    }

    // Updates the status when the admin submits the dropdown form
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        if (notAdmin(req, res)) return;

        String status = req.getParameter("status");
        boolean validStatus = false;
        for (String s : STATUSES) {
            if (s.equals(status)) validStatus = true;
        }

        try {
            int orderId = Integer.parseInt(req.getParameter("orderId"));
            if (validStatus) {
                new OrderDAO().updateStatus(orderId, status);
            }
        } catch (NumberFormatException e) {
            // ignore bad input and show the list again
        }

        res.sendRedirect("adminOrders");
    }

    // Shows every order with a status dropdown
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        if (notAdmin(req, res)) return;

        OrderDAO orderDAO = new OrderDAO();
        List<Order> orders = orderDAO.getAllOrders();

        res.setContentType("text/html;charset=UTF-8");
        PrintWriter out = res.getWriter();

        PageUtil.adminHead(out, "Manage Orders", "orders");
        out.println("<h1 class=\"page-title\">Manage Orders</h1>");
        out.println("<p class=\"page-sub\">" + orders.size() + (orders.size() == 1 ? " order" : " orders") + "</p>");

        if (orders.isEmpty()) {
            out.println("<div class=\"empty\"><h3>No orders yet</h3><p>Orders placed by customers will appear here.</p></div>");
            PageUtil.adminFoot(out);
            return;
        }

        SimpleDateFormat fmt = new SimpleDateFormat("dd MMM yyyy, hh:mm a");

        out.println("<section class=\"panel\"><div class=\"table-wrap\"><table>");
        out.println("<thead><tr><th>Order</th><th>User</th><th>Date</th><th>Total</th><th>Payment</th>"
                + "<th>Address</th><th>Status</th></tr></thead><tbody>");

        for (Order order : orders) {
            String date = order.getOrderDate() == null ? "" : fmt.format(order.getOrderDate());

            out.println("<tr>");
            out.println("<td>RF" + (1000 + order.getId()) + "</td>");
            out.println("<td>#" + order.getUserId() + "</td>");
            out.println("<td>" + PageUtil.esc(date) + "</td>");
            out.println("<td>" + PageUtil.rupees(order.getTotal()) + "</td>");
            out.println("<td>" + PageUtil.esc(order.getPaymentMethod()) + "</td>");
            out.println("<td>" + PageUtil.esc(order.getAddress()) + "</td>");

            out.println("<td><form action=\"adminOrders\" method=\"post\">");
            out.println("<input type=\"hidden\" name=\"orderId\" value=\"" + order.getId() + "\">");
            out.println("<select name=\"status\">");
            for (String s : STATUSES) {
                String selected = s.equals(order.getStatus()) ? " selected" : "";
                out.println("<option value=\"" + s + "\"" + selected + ">" + s + "</option>");
            }
            out.println("</select>");
            out.println("<button type=\"submit\" class=\"btn btn--outline btn--sm\">Update</button>");
            out.println("</form></td>");
            out.println("</tr>");
        }

        out.println("</tbody></table></div></section>");
        PageUtil.adminFoot(out);
    }
}
