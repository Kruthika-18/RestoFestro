package com.restofestro.controller;

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
import com.restofestro.model.Food;
import com.restofestro.model.Order;
import com.restofestro.util.PageUtil;

@WebServlet("/orders")
public class OrderServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Order status in the order the admin moves it through
    private static final String[] STEP_STATUS = { "Confirmed", "Preparing", "Out for Delivery", "Delivered" };
    private static final String[] STEP_LABEL = { "Order Placed", "Preparing", "Out for Delivery", "Delivered" };

    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            res.sendRedirect("login.html");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        OrderDAO orderDAO = new OrderDAO();
        List<Order> orders = orderDAO.getOrdersByUser(userId);

        res.setContentType("text/html;charset=UTF-8");
        PrintWriter out = res.getWriter();

        PageUtil.head(out, "My Orders", "orders");
        out.println("<div class=\"page page--narrow\">");
        out.println("<h1 class=\"page-title\">My Orders</h1>");

        if (orders.isEmpty()) {
            out.println("<p class=\"page-sub\">Your order history will appear here.</p>");
            PageUtil.emptyState(out, "You haven't placed any orders yet",
                    "Explore the five states and order your first meal.", "home.html#states", "Start Ordering");
            out.println("</div>");
            PageUtil.foot(out);
            return;
        }

        out.println("<p class=\"page-sub\">" + orders.size() + (orders.size() == 1 ? " order" : " orders") + "</p>");

        SimpleDateFormat fmt = new SimpleDateFormat("dd MMM yyyy, hh:mm a");

        for (Order order : orders) {
            String status = order.getStatus() == null ? "Confirmed" : order.getStatus();
            int stepIndex = 0;
            for (int i = 0; i < STEP_STATUS.length; i++) {
                if (STEP_STATUS[i].equals(status)) stepIndex = i;
            }
            String payLabel = "COD".equals(order.getPaymentMethod()) ? "Cash on Delivery" : order.getPaymentMethod();
            String date = order.getOrderDate() == null ? "" : fmt.format(order.getOrderDate());

            out.println("<article class=\"order\">");
            out.println("<header><h2>Order RF" + (1000 + order.getId()) + "</h2>"
                    + "<span class=\"status" + ("Delivered".equals(status) ? " status--delivered" : "") + "\">"
                    + PageUtil.esc(status) + "</span></header>");
            out.println("<p class=\"order__meta\">" + PageUtil.esc(date) + " &middot; " + PageUtil.esc(payLabel) + "</p>");

            out.println("<ul>");
            for (Food item : orderDAO.getOrderItems(order.getId())) {
                out.println("<li><span>" + PageUtil.esc(item.getName()) + " &times; " + item.getCartQuantity()
                        + "</span><span>" + PageUtil.rupees(item.getPrice() * item.getCartQuantity()) + "</span></li>");
            }
            out.println("</ul>");

            out.println("<div class=\"order__total\"><span>Total</span><span>" + PageUtil.rupees(order.getTotal())
                    + "</span></div>");

            out.println("<ol class=\"steps\">");
            for (int i = 0; i < STEP_LABEL.length; i++) {
                out.println("<li" + (i <= stepIndex ? " class=\"is-done\"" : "") + ">" + STEP_LABEL[i] + "</li>");
            }
            out.println("</ol>");

            out.println("<p class=\"order__addr\">Delivering to: " + PageUtil.esc(order.getAddress()) + "</p>");
            out.println("</article>");
        }

        out.println("</div>");
        PageUtil.foot(out);
    }
}
