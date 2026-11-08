package com.restofestro.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.restofestro.dao.CartDAO;
import com.restofestro.dao.OrderDAO;
import com.restofestro.model.Food;
import com.restofestro.util.PageUtil;

@WebServlet("/checkout")
public class CheckoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Shows the checkout page: address + payment form beside the order summary
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            res.sendRedirect("login.html");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        List<Food> cartItems = new CartDAO().getCartItems(userId);

        if (cartItems.isEmpty()) {
            res.sendRedirect("cart");
            return;
        }

        res.setContentType("text/html;charset=UTF-8");
        PrintWriter out = res.getWriter();

        double total = 0;
        for (Food item : cartItems) {
            total += item.getPrice() * item.getCartQuantity();
        }

        PageUtil.head(out, "Checkout", "cart");
        out.println("<div class=\"page\">");
        out.println("<p class=\"crumbs\"><a href=\"cart\">Cart</a> / Checkout</p>");
        out.println("<h1 class=\"page-title\">Checkout</h1>");
        out.println("<p class=\"page-sub\">Tell us where to deliver and how you'd like to pay.</p>");

        out.println("<div class=\"checkout-layout\">");

        // left: delivery + payment
        out.println("<form class=\"panel\" action=\"checkout\" method=\"post\">");
        out.println("<h2 class=\"section-sub-title\" style=\"margin-top:0\">Delivery Address</h2>");
        out.println("<label for=\"address\">Full address</label>");
        out.println("<textarea id=\"address\" name=\"address\" rows=\"4\" maxlength=\"255\" required "
                + "placeholder=\"House / street, area, city, pincode\"></textarea>");

        out.println("<h2 class=\"section-sub-title\" style=\"margin-top:24px\">Payment Method</h2>");
        out.println("<div class=\"radio-row\">");
        out.println("<label><input type=\"radio\" name=\"paymentMethod\" value=\"UPI\" checked> UPI</label>");
        out.println("<label><input type=\"radio\" name=\"paymentMethod\" value=\"Card\"> Card</label>");
        out.println("<label><input type=\"radio\" name=\"paymentMethod\" value=\"COD\"> Cash on Delivery</label>");
        out.println("</div>");
        out.println("<p class=\"muted-note\">Payment is simulated. No real money is charged and no card details are collected.</p>");
        out.println("<button type=\"submit\" class=\"btn btn--gold btn--block\" style=\"margin-top:20px\">Place Order &middot; "
                + PageUtil.rupees(total) + "</button>");
        out.println("</form>");

        // right: order summary
        out.println("<aside class=\"panel summary\">");
        out.println("<h2>Order Summary</h2>");
        out.println("<ul class=\"summary__items\">");
        for (Food item : cartItems) {
            out.println("<li>" + PageUtil.imgTag(item, null, false)
                    + "<span>" + PageUtil.esc(item.getName()) + "<small>Qty " + item.getCartQuantity() + " &times; "
                    + PageUtil.rupees(item.getPrice()) + "</small></span>"
                    + "<span>" + PageUtil.rupees(item.getPrice() * item.getCartQuantity()) + "</span></li>");
        }
        out.println("</ul>");
        out.println("<div class=\"sum-row sum-row--total\"><span>Total</span><strong>" + PageUtil.rupees(total)
                + "</strong></div>");
        out.println("<p class=\"muted-note\"><a class=\"link-gold\" href=\"cart\">Edit cart</a></p>");
        out.println("</aside>");

        out.println("</div>");
        out.println("</div>");
        PageUtil.foot(out);
    }

    // Places the order
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            res.sendRedirect("login.html");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        String address = req.getParameter("address");
        String paymentMethod = req.getParameter("paymentMethod");

        // Basic server-side checks (the form already enforces these in the browser)
        boolean validPayment = "UPI".equals(paymentMethod) || "Card".equals(paymentMethod)
                || "COD".equals(paymentMethod);
        if (address == null || address.trim().isEmpty() || address.length() > 255 || !validPayment) {
            res.sendRedirect("checkout");
            return;
        }
        address = address.trim();

        CartDAO cartDAO = new CartDAO();
        List<Food> cartItems = cartDAO.getCartItems(userId);

        if (cartItems.isEmpty()) {
            res.sendRedirect("cart");
            return;
        }

        double total = 0;
        for (Food item : cartItems) {
            total += item.getPrice() * item.getCartQuantity();
        }

        OrderDAO orderDAO = new OrderDAO();
        int orderId = orderDAO.placeOrder(userId, address, paymentMethod, cartItems);

        res.setContentType("text/html;charset=UTF-8");
        PrintWriter out = res.getWriter();

        if (orderId == -1) {
            PageUtil.head(out, "Order failed", "cart");
            out.println("<div class=\"page\">");
            PageUtil.emptyState(out, "Something went wrong",
                    "We couldn't place your order. Your cart is unchanged, please try again.", "cart", "Back to Cart");
            out.println("</div>");
            PageUtil.foot(out);
            return;
        }

        String payLabel = "COD".equals(paymentMethod) ? "Cash on Delivery" : paymentMethod;

        PageUtil.head(out, "Order Confirmed", "orders");
        out.println("<div class=\"page page--narrow confirm\">");
        out.println("<div class=\"confirm__icon\">&#10003;</div>");
        out.println("<h1>Order Confirmed!</h1>");
        out.println("<p class=\"page-sub\">Your delicious meal is on its way.</p>");

        out.println("<dl class=\"kv\">");
        out.println("<div><dt>Order ID</dt><dd>RF" + (1000 + orderId) + "</dd></div>");
        out.println("<div><dt>Payment</dt><dd>" + PageUtil.esc(payLabel) + "</dd></div>");
        out.println("<div><dt>Delivery address</dt><dd>" + PageUtil.esc(address) + "</dd></div>");
        out.println("<div><dt>Order total</dt><dd>" + PageUtil.rupees(total) + "</dd></div>");
        out.println("</dl>");

        out.println("<ol class=\"steps\">");
        out.println("<li class=\"is-done\">Order Placed</li><li>Preparing</li><li>Out for Delivery</li><li>Delivered</li>");
        out.println("</ol>");

        out.println("<div class=\"confirm__btns\">");
        out.println("<a class=\"btn btn--gold\" href=\"orders\">Track Order</a>");
        out.println("<a class=\"btn btn--outline\" href=\"home.html#states\">Continue Shopping</a>");
        out.println("</div>");
        out.println("</div>");
        PageUtil.foot(out);
    }
}
