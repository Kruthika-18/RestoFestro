package com.restofestro.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.restofestro.util.UploadUtil;

/** Sends back a dish photo that the admin uploaded: /dishimg?f=<file name> */
@WebServlet("/dishimg")
public class DishImageServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String name = req.getParameter("f");
        if (!UploadUtil.isSafeName(name)) {
            res.sendError(404);
            return;
        }

        File file = new File(UploadUtil.dir(), name);
        if (!file.isFile()) {
            res.sendError(404);
            return;
        }

        res.setContentType(UploadUtil.contentTypeFor(name));
        res.setContentLengthLong(file.length());
        res.setHeader("Cache-Control", "public, max-age=86400");
        Files.copy(file.toPath(), res.getOutputStream());
    }
}
