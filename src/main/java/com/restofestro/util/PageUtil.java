package com.restofestro.util;

import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.regex.Pattern;

import com.restofestro.model.Food;

/**
 * Shared HTML pieces so every servlet-generated page has the same header,
 * footer and food cards as home.html. Also escapes text before printing it,
 * so a dish name or an address can never inject HTML into a page.
 */
public class PageUtil {

    private static final String FONTS =
        "https://fonts.googleapis.com/css2?family=Cormorant+Garamond:ital,wght@0,500;0,600;1,500;1,600"
        + "&amp;family=Jost:wght@400;500;600&amp;display=swap";

    // Photos are matched on the dish name first, then fall back to the state photo.
    private static final Pattern DOSA = Pattern.compile("dosa|dosai|uttapam|pesarattu|adai", Pattern.CASE_INSENSITIVE);
    private static final Pattern BIRYANI = Pattern.compile("biryani|biriyani|pulao|pulav|fried rice|rice", Pattern.CASE_INSENSITIVE);
    private static final Pattern IDLI = Pattern.compile("idli|idly|vada|vadai|medu|bonda|sambar", Pattern.CASE_INSENSITIVE);
    private static final Pattern PAROTTA = Pattern.compile("parotta|parota|porotta|paratha|chapati|appam|puttu", Pattern.CASE_INSENSITIVE);
    private static final Pattern RAGI = Pattern.compile("ragi|mudde|millet|ambali", Pattern.CASE_INSENSITIVE);

    // ---------- small helpers ----------

    /** Escapes text for safe printing inside HTML. */
    public static String esc(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '&': sb.append("&amp;"); break;
                case '<': sb.append("&lt;"); break;
                case '>': sb.append("&gt;"); break;
                case '"': sb.append("&quot;"); break;
                case '\'': sb.append("&#39;"); break;
                default: sb.append(c);
            }
        }
        return sb.toString();
    }

    /** URL-encodes a value used inside a link, e.g. "Tamil Nadu" -> "Tamil+Nadu". */
    public static String enc(String s) {
        try {
            return URLEncoder.encode(s == null ? "" : s, "UTF-8");
        } catch (Exception e) {
            return "";
        }
    }

    /** 120.0 -> &#8377;120   and   120.5 -> &#8377;120.50 */
    public static String rupees(double v) {
        if (v == Math.floor(v)) {
            return "&#8377;" + String.format(Locale.ENGLISH, "%,d", (long) v);
        }
        return "&#8377;" + String.format(Locale.ENGLISH, "%,.2f", v);
    }

    public static boolean isVeg(String vegType) {
        if (vegType == null) return false;
        String t = vegType.toLowerCase();
        return t.contains("veg") && !t.contains("non");
    }

    public static String vegMark(String vegType) {
        boolean veg = isVeg(vegType);
        return "<span class=\"vegmark " + (veg ? "vegmark--veg" : "vegmark--non") + "\" title=\""
                + (veg ? "Vegetarian" : "Non-vegetarian") + "\"><span></span></span>";
    }

    public static String stateImage(String state) {
        if ("Telangana".equals(state)) return "images/state-telangana.jpg";
        if ("Karnataka".equals(state)) return "images/state-karnataka.jpg";
        if ("Tamil Nadu".equals(state)) return "images/state-tamilnadu.jpg";
        if ("Kerala".equals(state)) return "images/state-kerala.jpg";
        return "images/state-andhra.jpg";
    }

    public static String stateTagline(String state) {
        if ("Andhra Pradesh".equals(state)) return "Bold spices and unforgettable flavors";
        if ("Telangana".equals(state)) return "Rich heritage, unique taste";
        if ("Karnataka".equals(state)) return "Traditional flavors, modern delight";
        if ("Tamil Nadu".equals(state)) return "Simple ingredients, extraordinary taste";
        if ("Kerala".equals(state)) return "Fresh, healthy, full of flavor";
        return "Discover the flavors of this region";
    }

    /** Picks a photo for a dish: by dish name, else by state. */
    public static String foodImage(String name, String state) {
        String n = name == null ? "" : name;
        if (DOSA.matcher(n).find()) return "images/dish-masala-dosa.jpg";
        if (BIRYANI.matcher(n).find()) return "images/dish-biryani.jpg";
        if (IDLI.matcher(n).find()) return "images/dish-idli-vada.jpg";
        if (PAROTTA.matcher(n).find()) return "images/dish-parotta.jpg";
        if (RAGI.matcher(n).find()) return "images/dish-ragi.jpg";
        return stateImage(state);
    }

    /** "Appam with Stew" -> "appam-with-stew" (used for photo file names). */
    public static String slug(String name) {
        String s = name == null ? "" : name.toLowerCase().replaceAll("[^a-z0-9]+", "-");
        s = s.replaceAll("^-+|-+$", "");
        if (s.length() > 40) s = s.substring(0, 40).replaceAll("-+$", "");
        return s.isEmpty() ? "dish" : s;
    }

    /**
     * Where a dish's own photo lives:
     *  - uploaded from the admin panel  -> dishimg?f=<file>   (stored in the uploads folder)
     *  - otherwise                      -> images/dishes/<dish-name>.jpg
     */
    public static String dishImageSrc(Food f) {
        String img = f.getImage();
        if (img != null && img.startsWith("uploads/")) {
            return "dishimg?f=" + enc(img.substring("uploads/".length()));
        }
        return "images/dishes/" + slug(f.getName()) + ".jpg";
    }

    /** A complete &lt;img&gt; tag: the dish's own photo, or a shared photo if that file does not exist yet. */
    public static String imgTag(Food f, String cssClass, boolean lazy) {
        String fallback = foodImage(f.getName(), f.getState());
        return "<img" + (cssClass != null ? " class=\"" + cssClass + "\"" : "")
                + " src=\"" + dishImageSrc(f) + "\" alt=\"" + esc(f.getName()) + "\""
                + (lazy ? " loading=\"lazy\"" : "")
                + " onerror=\"this.onerror=null;this.src='" + fallback + "'\">";
    }

    // ---------- page shell for customer pages ----------

    private static String navLink(String active, String key, String href, String label) {
        return "<a href=\"" + href + "\"" + (key.equals(active) ? " class=\"is-active\"" : "") + ">" + label + "</a>";
    }

    /** Opens a customer page: head + sticky header + &lt;main&gt;. active = home | states | orders | cart */
    public static void head(PrintWriter out, String title, String active) {
        out.println("<!DOCTYPE html>");
        out.println("<html lang=\"en\">");
        out.println("<head>");
        out.println("<meta charset=\"UTF-8\">");
        out.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");
        out.println("<title>" + esc(title) + " - RestoFestro</title>");
        out.println("<link rel=\"preconnect\" href=\"https://fonts.googleapis.com\">");
        out.println("<link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin>");
        out.println("<link href=\"" + FONTS + "\" rel=\"stylesheet\">");
        out.println("<link rel=\"stylesheet\" href=\"css/style.css\">");
        out.println("</head>");
        out.println("<body>");
        out.println("<header class=\"site-header\"><div class=\"site-header__bar\">");
        out.println("<a href=\"home.html\" class=\"logo\"><span class=\"logo__name\">RestoFestro</span>"
                + "<span class=\"logo__tag\">Taste the Traditions of South India</span></a>");
        out.println("<button class=\"nav-toggle\" type=\"button\" "
                + "onclick=\"document.getElementById('nav').classList.toggle('is-open')\">Menu</button>");
        out.println("<nav class=\"site-nav\" id=\"nav\">");
        out.println(navLink(active, "home", "home.html", "Home"));
        out.println(navLink(active, "states", "home.html#states", "States"));
        out.println(navLink(active, "orders", "orders", "My Orders"));
        out.println(navLink(active, "cart", "cart", "Cart"));
        out.println("<a href=\"logout\">Logout</a>");
        out.println("<a href=\"home.html#states\" class=\"btn btn--gold btn--sm\">Order Now</a>");
        out.println("</nav></div></header>");
        out.println("<main>");
    }

    /** Closes a customer page: footer + closing tags. */
    public static void foot(PrintWriter out) {
        out.println("</main>");
        out.println("<footer class=\"site-footer\">");
        out.println("<div class=\"site-footer__grid\">");
        out.println("<div><a href=\"home.html\" class=\"logo\"><span class=\"logo__name\">RestoFestro</span>"
                + "<span class=\"logo__tag\">Taste the Traditions of South India</span></a>"
                + "<p style=\"margin-top:12px\">South Indian regional food ordering, one state at a time.</p></div>");
        out.println("<div><h2>Quick Links</h2><ul>"
                + "<li><a href=\"home.html#states\">States</a></li>"
                + "<li><a href=\"home.html#dishes\">Dishes</a></li>"
                + "<li><a href=\"cart\">Cart</a></li>"
                + "<li><a href=\"orders\">My Orders</a></li></ul></div>");
        out.println("<div><h2>Account</h2><ul><li><a href=\"logout\">Logout</a></li></ul></div>");
        out.println("</div>");
        out.println("<p class=\"site-footer__copy\">&copy; 2026 RestoFestro. All Rights Reserved.</p>");
        out.println("</footer>");
        out.println("</body>");
        out.println("</html>");
    }

    /** A friendly empty / message box with one button. */
    public static void emptyState(PrintWriter out, String title, String text, String href, String label) {
        out.println("<div class=\"empty\">");
        out.println("<h3>" + esc(title) + "</h3>");
        out.println("<p>" + esc(text) + "</p>");
        out.println("<a class=\"btn btn--gold\" href=\"" + href + "\">" + esc(label) + "</a>");
        out.println("</div>");
    }

    /** One dish card, used by the menu, search results and "You may also like". */
    public static String foodCard(Food f) {
        String link = "food?id=" + f.getId();
        StringBuilder sb = new StringBuilder();
        sb.append("<article class=\"food-card\">");
        sb.append("<a class=\"food-card__photo\" href=\"").append(link).append("\">");
        sb.append(imgTag(f, null, true)).append("</a>");
        sb.append("<div class=\"food-card__body\">");
        sb.append("<h3 class=\"food-card__name\"><a href=\"").append(link).append("\">")
          .append(esc(f.getName())).append("</a> ").append(vegMark(f.getVegType())).append("</h3>");
        sb.append("<p class=\"food-card__desc\">").append(esc(f.getDescription())).append("</p>");
        sb.append("<div class=\"food-card__meta\"><span class=\"food-card__state\">")
          .append(esc(f.getState())).append(" &middot; ").append(esc(f.getCategory()))
          .append("</span><span class=\"food-card__price\">").append(rupees(f.getPrice())).append("</span></div>");
        if (f.isAvailable()) {
            sb.append("<form action=\"cart\" method=\"post\">");
            sb.append("<input type=\"hidden\" name=\"foodId\" value=\"").append(f.getId()).append("\">");
            sb.append("<input type=\"hidden\" name=\"quantity\" value=\"1\">");
            sb.append("<button type=\"submit\" class=\"btn btn--gold btn--block btn--sm\">Add to Cart</button></form>");
        } else {
            sb.append("<span class=\"btn btn--outline btn--block btn--sm\">Unavailable</span>");
        }
        sb.append("</div></article>");
        return sb.toString();
    }

    // ---------- small standalone card page (errors on login / register) ----------

    public static void notice(PrintWriter out, String title, String message, String href, String linkText) {
        out.println("<!DOCTYPE html>");
        out.println("<html lang=\"en\">");
        out.println("<head>");
        out.println("<meta charset=\"UTF-8\">");
        out.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");
        out.println("<title>" + esc(title) + " - RestoFestro</title>");
        out.println("<link href=\"" + FONTS + "\" rel=\"stylesheet\">");
        out.println("<link rel=\"stylesheet\" href=\"css/style.css\">");
        out.println("</head>");
        out.println("<body>");
        out.println("<div class=\"auth-page\"><div class=\"auth-card\">");
        out.println("<a href=\"home.html\" class=\"logo\"><span class=\"logo__name\">RestoFestro</span>"
                + "<span class=\"logo__tag\">Taste the Traditions of South India</span></a>");
        out.println("<p class=\"notice__msg\">" + message + "</p>");
        out.println("<a class=\"btn btn--gold\" href=\"" + href + "\">" + esc(linkText) + "</a>");
        out.println("</div></div>");
        out.println("</body>");
        out.println("</html>");
    }

    // ---------- page shell for admin pages ----------

    private static String adminLink(String active, String key, String href, String label) {
        return "<a href=\"" + href + "\"" + (key.equals(active) ? " class=\"is-active\"" : "") + ">" + label + "</a>";
    }

    /** active = dashboard | food | orders */
    public static void adminHead(PrintWriter out, String title, String active) {
        out.println("<!DOCTYPE html>");
        out.println("<html lang=\"en\">");
        out.println("<head>");
        out.println("<meta charset=\"UTF-8\">");
        out.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");
        out.println("<title>" + esc(title) + " - RestoFestro Admin</title>");
        out.println("<link href=\"" + FONTS + "\" rel=\"stylesheet\">");
        out.println("<link rel=\"stylesheet\" href=\"css/style.css\">");
        out.println("</head>");
        out.println("<body>");
        out.println("<nav class=\"admin-nav\">");
        out.println("<span class=\"admin-brand\">RestoFestro Admin</span>");
        out.println(adminLink(active, "dashboard", "admin/dashboard.html", "Dashboard"));
        out.println(adminLink(active, "food", "adminFood", "Manage Food"));
        out.println(adminLink(active, "orders", "adminOrders", "Manage Orders"));
        out.println("<a href=\"adminLogout\">Logout</a>");
        out.println("</nav>");
        out.println("<main class=\"page\">");
    }

    public static void adminFoot(PrintWriter out) {
        out.println("</main>");
        out.println("</body>");
        out.println("</html>");
    }
}
