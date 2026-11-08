package com.restofestro.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import jakarta.servlet.http.Part;

/**
 * Saves dish photos uploaded from the admin panel.
 *
 * Photos go to a folder OUTSIDE the project (your user folder / restofestro-uploads),
 * so they survive "Clean" and re-publish in Eclipse. They are served back by DishImageServlet.
 */
public class UploadUtil {

    public static final long MAX_BYTES = 3L * 1024 * 1024;   // 3 MB

    public static File dir() {
        File d = new File(System.getProperty("user.home"), "restofestro-uploads");
        if (!d.exists()) {
            d.mkdirs();
        }
        return d;
    }

    /**
     * Saves one uploaded photo.
     * Returns the value to store in food.image (for example "uploads/1700000000-appam.jpg"),
     * or null when there is no file or it is not a real JPG / PNG / WEBP image under 3 MB.
     */
    public static String saveDishImage(Part part, String dishName) throws IOException {
        if (part == null || part.getSize() <= 0 || part.getSize() > MAX_BYTES) {
            return null;
        }

        // Check the first bytes of the file, not just its name, so a renamed text file is rejected
        byte[] head = new byte[12];
        int read = 0;
        try (InputStream in = part.getInputStream()) {
            while (read < head.length) {
                int r = in.read(head, read, head.length - read);
                if (r < 0) break;
                read += r;
            }
        }
        String ext = detectExtension(head, read);
        if (ext == null) {
            return null;
        }

        String name = System.currentTimeMillis() + "-" + PageUtil.slug(dishName) + "." + ext;
        File target = new File(dir(), name);
        try (InputStream in = part.getInputStream()) {
            Files.copy(in, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        return "uploads/" + name;
    }

    private static String detectExtension(byte[] b, int n) {
        if (n >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return "jpg";
        }
        if (n >= 4 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') {
            return "png";
        }
        if (n >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return "webp";
        }
        return null;
    }

    /** Only plain file names are allowed (no folders, no ".."). */
    public static boolean isSafeName(String f) {
        return f != null && f.matches("[A-Za-z0-9._-]{1,120}") && !f.contains("..");
    }

    public static String contentTypeFor(String f) {
        String n = f.toLowerCase();
        if (n.endsWith(".png")) return "image/png";
        if (n.endsWith(".webp")) return "image/webp";
        return "image/jpeg";
    }
}
