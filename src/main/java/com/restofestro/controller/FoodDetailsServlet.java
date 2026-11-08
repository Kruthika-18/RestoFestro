package com.restofestro.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.restofestro.dao.FoodDAO;
import com.restofestro.model.Food;
import com.restofestro.util.PageUtil;

@WebServlet("/food")
public class FoodDetailsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        res.setContentType("text/html;charset=UTF-8");
        PrintWriter out = res.getWriter();

        FoodDAO foodDAO = new FoodDAO();
        Food food = null;
        try {
            food = foodDAO.getFoodById(Integer.parseInt(req.getParameter("id")));
        } catch (NumberFormatException e) {
            // a bad id falls through to the "not found" page below
        }

        if (food == null) {
            PageUtil.head(out, "Dish not found", "states");
            out.println("<div class=\"page\">");
            PageUtil.emptyState(out, "We couldn't find that dish",
                    "It may have been removed from the menu.", "home.html#states", "Explore States");
            out.println("</div>");
            PageUtil.foot(out);
            return;
        }

        String state = food.getState();
        String category = food.getCategory();

        PageUtil.head(out, food.getName(), "states");
        out.println("<div class=\"page\">");
        out.println("<p class=\"crumbs\"><a href=\"home.html\">Home</a> / <a href=\"menu?state=" + PageUtil.enc(state)
                + "\">" + PageUtil.esc(state) + "</a> / <a href=\"menu?state=" + PageUtil.enc(state)
                + "&amp;category=" + PageUtil.enc(category) + "\">" + PageUtil.esc(category) + "</a> / "
                + PageUtil.esc(food.getName()) + "</p>");

        out.println("<div class=\"detail\">");
        out.println("<div class=\"detail__photo\">" + PageUtil.imgTag(food, null, false) + "</div>");

        out.println("<div class=\"detail__info\">");
        out.println("<p class=\"detail__eyebrow\">" + PageUtil.esc(state) + " &middot; " + PageUtil.esc(category) + "</p>");
        out.println("<h1 class=\"page-title\">" + PageUtil.esc(food.getName()) + "</h1>");
        out.println("<div class=\"detail__tags\">" + PageUtil.vegMark(food.getVegType()) + " "
                + (PageUtil.isVeg(food.getVegType()) ? "Vegetarian" : "Non-vegetarian") + "</div>");
        out.println("<p class=\"detail__price\">" + PageUtil.rupees(food.getPrice()) + "</p>");
        out.println("<p class=\"detail__desc\">" + PageUtil.esc(food.getDescription()) + "</p>");

        if (food.isAvailable()) {
            out.println("<form action=\"cart\" method=\"post\" class=\"detail__form\">");
            out.println("<input type=\"hidden\" name=\"foodId\" value=\"" + food.getId() + "\">");
            out.println("<label for=\"qty\">Quantity</label>");
            out.println("<div class=\"qty\">");
            out.println("<button type=\"button\" aria-label=\"Decrease\" "
                    + "onclick=\"var q=document.getElementById('qty');q.value=Math.max(1,(+q.value||1)-1)\">&minus;</button>");
            out.println("<input type=\"number\" id=\"qty\" name=\"quantity\" value=\"1\" min=\"1\" max=\"20\">");
            out.println("<button type=\"button\" aria-label=\"Increase\" "
                    + "onclick=\"var q=document.getElementById('qty');q.value=Math.min(20,(+q.value||1)+1)\">+</button>");
            out.println("</div>");
            out.println("<button type=\"submit\" class=\"btn btn--gold btn--block\">Add to Cart</button>");
            out.println("</form>");
        } else {
            out.println("<p class=\"muted-note\">This dish is currently unavailable.</p>");
        }
        out.println("</div>");
        out.println("</div>");

        // You may also like: same category first, then the rest of the state
        List<Food> related = new ArrayList<Food>();
        for (Food f : foodDAO.getFoodByStateAndCategory(state, category)) {
            if (f.getId() != food.getId() && related.size() < 4) related.add(f);
        }
        if (related.size() < 4) {
            for (Food f : foodDAO.getFoodByState(state)) {
                boolean dup = f.getId() == food.getId();
                for (Food r : related) {
                    if (r.getId() == f.getId()) dup = true;
                }
                if (!dup && related.size() < 4) related.add(f);
            }
        }
        if (!related.isEmpty()) {
            out.println("<h2 class=\"section-sub-title\">You may also like</h2>");
            out.println("<div class=\"grid-auto\">");
            for (Food f : related) {
                out.println(PageUtil.foodCard(f));
            }
            out.println("</div>");
        }

        out.println("</div>");
        PageUtil.foot(out);
    }
}
