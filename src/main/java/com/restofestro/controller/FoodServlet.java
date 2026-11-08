package com.restofestro.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.restofestro.dao.FoodDAO;
import com.restofestro.model.Food;
import com.restofestro.util.PageUtil;

@WebServlet("/menu")
public class FoodServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String state = req.getParameter("state");
        String category = req.getParameter("category");
        String search = req.getParameter("search");

        boolean hasSearch = search != null && !search.trim().isEmpty();
        boolean hasState = state != null && !state.trim().isEmpty();

        // Nothing to show: send the visitor back to the state list
        if (!hasSearch && !hasState) {
            res.sendRedirect("home.html#states");
            return;
        }

        res.setContentType("text/html;charset=UTF-8");
        PrintWriter out = res.getWriter();
        FoodDAO foodDAO = new FoodDAO();

        if (hasSearch) {
            showSearch(out, foodDAO, search.trim());
        } else if (category == null || category.trim().isEmpty()) {
            showState(out, foodDAO, state.trim());
        } else {
            showCategory(out, foodDAO, state.trim(), category.trim());
        }
    }

    // Search mode: matching dishes from every state
    private void showSearch(PrintWriter out, FoodDAO foodDAO, String keyword) {
        List<Food> results = foodDAO.searchFood(keyword);

        PageUtil.head(out, "Search", "states");
        out.println("<div class=\"page\">");
        out.println("<p class=\"crumbs\"><a href=\"home.html\">Home</a> / Search</p>");
        out.println("<h1 class=\"page-title\">Search results</h1>");
        out.println("<p class=\"page-sub\">" + results.size() + (results.size() == 1 ? " dish" : " dishes")
                + " found for &ldquo;" + PageUtil.esc(keyword) + "&rdquo;</p>");

        if (results.isEmpty()) {
            PageUtil.emptyState(out, "We couldn't find anything matching your search",
                    "Try a different word, or explore the menu state by state.",
                    "home.html#states", "Explore States");
        } else {
            printGrid(out, results);
        }

        out.println("</div>");
        PageUtil.foot(out);
    }

    // State page: category buttons first, then every dish of that state
    private void showState(PrintWriter out, FoodDAO foodDAO, String state) {
        List<Food> all = foodDAO.getFoodByState(state);

        PageUtil.head(out, state, "states");
        out.println("<div class=\"page\">");
        out.println("<p class=\"crumbs\"><a href=\"home.html\">Home</a> / " + PageUtil.esc(state) + "</p>");

        out.println("<section class=\"state-hero\"><div>");
        out.println("<h1 class=\"page-title\">" + PageUtil.esc(state) + "</h1>");
        out.println("<p class=\"page-sub\" style=\"margin:0\">" + PageUtil.esc(PageUtil.stateTagline(state)) + "</p>");
        out.println("</div><img class=\"state-hero__img\" src=\"" + PageUtil.stateImage(state) + "\" alt=\"\"></section>");

        if (all.isEmpty()) {
            PageUtil.emptyState(out, "No dishes here yet",
                    "We haven't added any dishes for this state. Please check back soon.",
                    "home.html#states", "Explore Other States");
        } else {
            out.println("<h2 class=\"section-sub-title\">Choose a category</h2>");
            printChips(out, state, null, all);
            out.println("<h2 class=\"section-sub-title\">All " + PageUtil.esc(state) + " dishes</h2>");
            printGrid(out, all);
        }

        out.println("</div>");
        PageUtil.foot(out);
    }

    // Category page: dishes of one state + category
    private void showCategory(PrintWriter out, FoodDAO foodDAO, String state, String category) {
        List<Food> foodList = foodDAO.getFoodByStateAndCategory(state, category);
        List<Food> allStateFood = foodDAO.getFoodByState(state);

        PageUtil.head(out, state + " - " + category, "states");
        out.println("<div class=\"page\">");
        out.println("<p class=\"crumbs\"><a href=\"home.html\">Home</a> / <a href=\"menu?state="
                + PageUtil.enc(state) + "\">" + PageUtil.esc(state) + "</a> / " + PageUtil.esc(category) + "</p>");
        out.println("<h1 class=\"page-title\">" + PageUtil.esc(category) + "</h1>");
        out.println("<p class=\"page-sub\">" + PageUtil.esc(state) + " &middot; "
                + PageUtil.esc(PageUtil.stateTagline(state)) + "</p>");

        printChips(out, state, category, allStateFood);

        if (foodList.isEmpty()) {
            PageUtil.emptyState(out, "No dishes in this category",
                    "Try another category from this state.", "menu?state=" + PageUtil.enc(state), "Back to " + state);
        } else {
            printGrid(out, foodList);
        }

        out.println("</div>");
        PageUtil.foot(out);
    }

    // Category buttons with a dish count; the current one is highlighted
    private void printChips(PrintWriter out, String state, String current, List<Food> allStateFood) {
        Map<String, Integer> counts = new LinkedHashMap<String, Integer>();
        for (Food f : allStateFood) {
            Integer n = counts.get(f.getCategory());
            counts.put(f.getCategory(), n == null ? 1 : n + 1);
        }

        out.println("<div class=\"chip-row\">");
        out.println("<a class=\"chip" + (current == null ? " is-on" : "") + "\" href=\"menu?state="
                + PageUtil.enc(state) + "\">All</a>");
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            boolean on = e.getKey().equals(current);
            out.println("<a class=\"chip" + (on ? " is-on" : "") + "\" href=\"menu?state=" + PageUtil.enc(state)
                    + "&amp;category=" + PageUtil.enc(e.getKey()) + "\">" + PageUtil.esc(e.getKey())
                    + " <small>" + e.getValue() + "</small></a>");
        }
        out.println("</div>");
    }

    private void printGrid(PrintWriter out, List<Food> foods) {
        out.println("<div class=\"grid-auto\">");
        for (Food f : foods) {
            out.println(PageUtil.foodCard(f));
        }
        out.println("</div>");
    }
}
