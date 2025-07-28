package com.weather;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import org.json.JSONArray;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/* ==============================================================
   File: FavoriteServlet.java
   Project: WeatherNow
   Purpose:
     • POST /FavoriteServlet   – add or remove a city in the favourites
                                 table and return {"success":true|false}.
     • GET  /FavoriteServlet   – return the full favourites list as
                                 a JSON array (["London","Tokyo",...]).
============================================================== */
public class FavoriteServlet extends HttpServlet {

    /* --------------------------------------------------------------
       POST – Add or remove a single favourite
       Params:
         city   – city name
         action – "add" | "remove"
    -------------------------------------------------------------- */
    @Override
    protected void doPost(HttpServletRequest req,
                          HttpServletResponse resp)
            throws ServletException, IOException {

        String city   = req.getParameter("city");
        String action = req.getParameter("action");   // add | remove
        boolean ok    = false;

        /* Delegate to DB helper */
        if ("add".equalsIgnoreCase(action))          ok = DBUtil.addFavorite(city);
        else if ("remove".equalsIgnoreCase(action))  ok = DBUtil.removeFavorite(city);

        /* Respond with simple success JSON */
        resp.setContentType("application/json");
        try (PrintWriter out = resp.getWriter()) {
            out.print("{\"success\":" + ok + "}");
        }
    }

    /* --------------------------------------------------------------
       GET – Return all favourite cities
       Response: ["Berlin","Sydney",...]
    -------------------------------------------------------------- */
    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp)
            throws ServletException, IOException {

        List<String> favs = DBUtil.getAllFavorites();

        resp.setContentType("application/json");
        try (PrintWriter out = resp.getWriter()) {
            out.print(new JSONArray(favs).toString());
        }
    }
}
