package com.weather;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import java.io.*;
import java.util.List;

import org.json.JSONArray;

/* ==============================================================
   File: HistoryServlet.java
   Project: WeatherNow
   Purpose:
     • GET  – return the last 10 searched cities as a JSON array.
     • POST – delete a specific city from search-history and return
              {"success":true|false}.
============================================================== */
public class HistoryServlet extends HttpServlet {

    /* --------------------------------------------------------------
       GET  /HistoryServlet
       Response: ["Paris","Tokyo", ...]  (max 10 items)
    -------------------------------------------------------------- */
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        List<String> history = DBUtil.getSearchHistory();   // fetch from DB
        JSONArray     arr    = new JSONArray(history);      // wrap in JSON

        response.setContentType("application/json");

        try (PrintWriter out = response.getWriter()) {
            out.print(arr.toString());
        }
    }

    /* --------------------------------------------------------------
       POST /HistoryServlet
       Param:  city   – name to remove
       Return: {"success":true|false}
    -------------------------------------------------------------- */
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String  city    = request.getParameter("city");
        boolean success = DBUtil.removeSearchHistory(city); // DB delete

        response.setContentType("application/json");

        try (PrintWriter out = response.getWriter()) {
            out.print("{\"success\":" + success + "}");
        }
    }
}
