package com.restofestro.admin;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import com.restofestro.dao.FoodDAO;
import com.restofestro.model.Food;
import com.restofestro.util.PageUtil;
import com.restofestro.util.UploadUtil;

// @MultipartConfig lets this servlet receive photo uploads (max 3 MB per file)
@WebServlet("/adminFood")
@MultipartConfig(maxFileSize = 3 * 1024 * 1024, maxRequestSize = 4 * 1024 * 1024)
public class AdminFoodServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String[] STATES = { "Andhra Pradesh", "Telangana", "Karnataka", "Tamil Nadu", "Kerala" };
    private static final String[] CATEGORIES = { "Breakfast", "Meals", "Main Course", "Curries", "Snacks", "Specials" };
    private static final String ACCEPT = "image/jpeg,image/png,image/webp";

    private boolean notAdmin(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("adminId") == null) {
            res.sendRedirect("admin/admin-login.html");
            return true;
        }
        return false;
    }

    // Handles Add, Change photo and Delete
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        if (notAdmin(req, res)) return;

        String action = req.getParameter("action");
        FoodDAO foodDAO = new FoodDAO();
        String msg = "";

        try {
            if ("delete".equals(action)) {
                int id = Integer.parseInt(req.getParameter("id"));
                foodDAO.deleteFood(id);
                msg = "deleted";

            } else if ("photo".equals(action)) {
                // Change the photo of an existing dish
                int id = Integer.parseInt(req.getParameter("id"));
                Food food = foodDAO.getFoodById(id);
                String saved = UploadUtil.saveDishImage(req.getPart("image"), food == null ? "" : food.getName());
                if (food != null && saved != null) {
                    food.setImage(saved);
                    foodDAO.updateFood(food);
                    msg = "photo_ok";
                } else {
                    msg = "photo_bad";
                }

            } else {
                // Add a new dish (the photo is optional)
                Food food = new Food();
                food.setName(req.getParameter("name"));
                food.setState(req.getParameter("state"));
                food.setCategory(req.getParameter("category"));
                food.setDescription(req.getParameter("description"));
                food.setPrice(Double.parseDouble(req.getParameter("price")));
                food.setVegType(req.getParameter("vegType"));
                food.setAvailable(true);

                String saved = UploadUtil.saveDishImage(req.getPart("image"), food.getName());
                food.setImage(saved != null ? saved : "images/placeholder.jpg");

                foodDAO.addFood(food);
                msg = saved != null ? "added" : "added_nophoto";
            }
        } catch (NumberFormatException e) {
            msg = "bad_input";
        } catch (IllegalStateException e) {
            msg = "too_big";   // file larger than 3 MB
        }

        res.sendRedirect("adminFood?msg=" + msg);
    }

    private String flash(String code) {
        if ("added".equals(code)) return "Dish added with its photo.";
        if ("added_nophoto".equals(code)) return "Dish added. No valid photo was uploaded, so it uses a default photo.";
        if ("photo_ok".equals(code)) return "Photo updated.";
        if ("photo_bad".equals(code)) return "That file was not accepted. Please choose a JPG, PNG or WEBP image under 3 MB.";
        if ("too_big".equals(code)) return "That photo is larger than 3 MB. Please choose a smaller one.";
        if ("deleted".equals(code)) return "Dish deleted.";
        if ("bad_input".equals(code)) return "Please check the price and try again.";
        return null;
    }

    // Shows the add form + the list of every dish
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        if (notAdmin(req, res)) return;

        FoodDAO foodDAO = new FoodDAO();
        List<Food> foodList = foodDAO.getAllFood();

        res.setContentType("text/html;charset=UTF-8");
        PrintWriter out = res.getWriter();

        PageUtil.adminHead(out, "Manage Food", "food");
        out.println("<h1 class=\"page-title\">Manage Food</h1>");
        out.println("<p class=\"page-sub\">" + foodList.size() + " dishes on the menu</p>");

        String message = flash(req.getParameter("msg"));
        if (message != null) {
            boolean good = !("photo_bad".equals(req.getParameter("msg")) || "too_big".equals(req.getParameter("msg"))
                    || "bad_input".equals(req.getParameter("msg")));
            out.println("<p class=\"flash" + (good ? "" : " flash--bad") + "\">" + PageUtil.esc(message) + "</p>");
        }

        out.println("<section class=\"panel\" style=\"margin-bottom:28px\">");
        out.println("<h2 class=\"section-sub-title\" style=\"margin-top:0\">Add New Dish</h2>");
        out.println("<form action=\"adminFood\" method=\"post\" enctype=\"multipart/form-data\">");
        out.println("<div class=\"form-grid\">");

        out.println("<div><label for=\"name\">Name</label><input type=\"text\" id=\"name\" name=\"name\" required></div>");

        out.println("<div><label for=\"state\">State</label><select id=\"state\" name=\"state\">");
        for (String s : STATES) {
            out.println("<option>" + s + "</option>");
        }
        out.println("</select></div>");

        out.println("<div><label for=\"category\">Category</label>"
                + "<input type=\"text\" id=\"category\" name=\"category\" list=\"cats\" required>"
                + "<datalist id=\"cats\">");
        for (String c : CATEGORIES) {
            out.println("<option value=\"" + c + "\">");
        }
        out.println("</datalist></div>");

        out.println("<div><label for=\"price\">Price (&#8377;)</label>"
                + "<input type=\"number\" id=\"price\" step=\"0.01\" min=\"0\" name=\"price\" required></div>");
        out.println("<div><label for=\"vegType\">Type</label><select id=\"vegType\" name=\"vegType\">"
                + "<option>Veg</option><option>Non-Veg</option></select></div>");
        out.println("<div><label for=\"description\">Description</label>"
                + "<input type=\"text\" id=\"description\" name=\"description\" maxlength=\"255\"></div>");
        out.println("<div class=\"span-3\"><label for=\"image\">Photo (optional &middot; JPG, PNG or WEBP &middot; up to 3 MB)</label>"
                + "<input type=\"file\" id=\"image\" name=\"image\" accept=\"" + ACCEPT + "\" class=\"file-input\"></div>");

        out.println("</div>");
        out.println("<button type=\"submit\" class=\"btn btn--gold\" style=\"margin-top:18px\">Add Dish</button>");
        out.println("</form>");
        out.println("</section>");

        out.println("<section class=\"panel\">");
        out.println("<h2 class=\"section-sub-title\" style=\"margin-top:0\">All Dishes</h2>");
        out.println("<div class=\"table-wrap\"><table>");
        out.println("<thead><tr><th>Dish</th><th>State</th><th>Category</th><th>Type</th><th>Price</th>"
                + "<th>Status</th><th>Photo &amp; actions</th></tr></thead><tbody>");
        for (Food food : foodList) {
            out.println("<tr>");
            out.println("<td><div class=\"dish-cell\">" + PageUtil.imgTag(food, "thumb", true) + "<span>"
                    + PageUtil.esc(food.getName()) + "</span></div></td>");
            out.println("<td>" + PageUtil.esc(food.getState()) + "</td>");
            out.println("<td>" + PageUtil.esc(food.getCategory()) + "</td>");
            out.println("<td>" + PageUtil.vegMark(food.getVegType()) + " " + PageUtil.esc(food.getVegType()) + "</td>");
            out.println("<td>" + PageUtil.rupees(food.getPrice()) + "</td>");
            out.println("<td>" + (food.isAvailable() ? "Available" : "Hidden") + "</td>");

            out.println("<td><div class=\"row-actions\">");
            out.println("<form action=\"adminFood\" method=\"post\" enctype=\"multipart/form-data\">"
                    + "<input type=\"hidden\" name=\"action\" value=\"photo\">"
                    + "<input type=\"hidden\" name=\"id\" value=\"" + food.getId() + "\">"
                    + "<input type=\"file\" name=\"image\" accept=\"" + ACCEPT + "\" class=\"file-input\" required>"
                    + "<button type=\"submit\" class=\"btn btn--outline btn--sm\">Change photo</button></form>");
            out.println("<form action=\"adminFood\" method=\"post\" "
                    + "onsubmit=\"return confirm('Delete this dish?')\">"
                    + "<input type=\"hidden\" name=\"action\" value=\"delete\">"
                    + "<input type=\"hidden\" name=\"id\" value=\"" + food.getId() + "\">"
                    + "<button type=\"submit\" class=\"btn btn--danger btn--sm\">Delete</button></form>");
            out.println("</div></td>");
            out.println("</tr>");
        }
        out.println("</tbody></table></div>");
        out.println("</section>");

        PageUtil.adminFoot(out);
    }
}
