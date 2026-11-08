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
import com.restofestro.model.Food;
import com.restofestro.util.PageUtil;

@WebServlet("/cart")
public class CartServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Add / update / remove all arrive here as POST forms
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            res.sendRedirect("login.html");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        String action = req.getParameter("action");
        CartDAO cartDAO = new CartDAO();

        try {
            if ("update".equals(action)) {
                int cartId = Integer.parseInt(req.getParameter("cartId"));
                int quantity = Integer.parseInt(req.getParameter("quantity"));
                if (quantity >= 1) {
                    cartDAO.updateQuantity(cartId, quantity);
                }

            } else if ("remove".equals(action)) {
                int cartId = Integer.parseInt(req.getParameter("cartId"));
                cartDAO.removeItem(cartId);

            } else {
                // Default: add to cart (from a dish card or the food details page)
                int foodId = Integer.parseInt(req.getParameter("foodId"));
                int quantity = Integer.parseInt(req.getParameter("quantity"));
                if (quantity >= 1) {
                    cartDAO.addToCart(userId, foodId, quantity);
                }
            }
        } catch (NumberFormatException e) {
            // ignore bad input and just show the cart again
        }

        res.sendRedirect("cart");
    }

    // Viewing the cart
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            res.sendRedirect("login.html");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        CartDAO cartDAO = new CartDAO();
        List<Food> items = cartDAO.getCartItems(userId);

        res.setContentType("text/html;charset=UTF-8");
        PrintWriter out = res.getWriter();

        PageUtil.head(out, "Your Cart", "cart");
        out.println("<div class=\"page\">");
        out.println("<h1 class=\"page-title\">Your Cart</h1>");

        if (items.isEmpty()) {
            out.println("<p class=\"page-sub\">Your cart is waiting for something delicious.</p>");
            PageUtil.emptyState(out, "No delicious dishes here yet",
                    "Pick a state and add a few dishes to get started.", "home.html#states", "Explore Menu");
            out.println("</div>");
            PageUtil.foot(out);
            return;
        }

        int count = 0;
        double total = 0;
        for (Food item : items) {
            count += item.getCartQuantity();
            total += item.getPrice() * item.getCartQuantity();
        }
        out.println("<p class=\"page-sub\">" + count + (count == 1 ? " item" : " items") + " in your cart</p>");

        out.println("<div class=\"cart-layout\">");

        // left: the dishes
        out.println("<section class=\"panel\">");
        for (Food item : items) {
            // item.getId() is the CART ROW id (see CartDAO.getCartItems)
            int cartId = item.getId();
            int qty = item.getCartQuantity();
            double lineTotal = item.getPrice() * qty;

            out.println("<div class=\"cart-item\">");
            out.println(PageUtil.imgTag(item, "cart-item__img", false));
            out.println("<div class=\"cart-item__info\">");
            out.println("<div class=\"cart-item__name\">" + PageUtil.esc(item.getName()) + " "
                    + PageUtil.vegMark(item.getVegType()) + "</div>");
            out.println("<div class=\"cart-item__sub\">" + PageUtil.esc(item.getState()) + " &middot; "
                    + PageUtil.rupees(item.getPrice()) + " each</div>");
            out.println("</div>");

            out.println("<form action=\"cart\" method=\"post\" class=\"cart-item__qty\">");
            out.println("<input type=\"hidden\" name=\"action\" value=\"update\">");
            out.println("<input type=\"hidden\" name=\"cartId\" value=\"" + cartId + "\">");
            out.println("<input type=\"number\" name=\"quantity\" value=\"" + qty
                    + "\" min=\"1\" max=\"99\" aria-label=\"Quantity\">");
            out.println("<button type=\"submit\" class=\"btn btn--outline btn--sm\">Update</button>");
            out.println("</form>");

            out.println("<div class=\"cart-item__total\">" + PageUtil.rupees(lineTotal) + "</div>");

            out.println("<form action=\"cart\" method=\"post\" "
                    + "onsubmit=\"return confirm('Remove this dish from your cart?')\">");
            out.println("<input type=\"hidden\" name=\"action\" value=\"remove\">");
            out.println("<input type=\"hidden\" name=\"cartId\" value=\"" + cartId + "\">");
            out.println("<button type=\"submit\" class=\"btn btn--danger btn--sm\">Remove</button>");
            out.println("</form>");
            out.println("</div>");
        }
        out.println("</section>");

        // right: order summary
        out.println("<aside class=\"panel summary\">");
        out.println("<h2>Order Summary</h2>");
        out.println("<div class=\"sum-row\"><span>Items</span><span>" + count + "</span></div>");
        out.println("<div class=\"sum-row\"><span>Subtotal</span><span>" + PageUtil.rupees(total) + "</span></div>");
        out.println("<div class=\"sum-row sum-row--total\"><span>Total</span><strong>" + PageUtil.rupees(total)
                + "</strong></div>");
        out.println("<a class=\"btn btn--gold btn--block\" href=\"checkout\">Proceed to Checkout</a>");
        out.println("<p class=\"muted-note\">No delivery fee or tax is added to your order.</p>");
        out.println("<p class=\"muted-note\"><a class=\"link-gold\" href=\"home.html#states\">Continue shopping</a></p>");
        out.println("</aside>");

        out.println("</div>");
        out.println("</div>");
        PageUtil.foot(out);
    }
}
