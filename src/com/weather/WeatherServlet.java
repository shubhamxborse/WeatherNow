package com.weather;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/* ==============================================================
   File: WeatherServlet.java
   Project: WeatherNow
   Purpose: Accepts a POST request containing a city name, calls the
            OpenWeatherMap REST API, enriches the response with the
            “isFavorite” flag, persists the search in history and
            streams the resulting JSON back to the front-end.
============================================================== */
public class WeatherServlet extends HttpServlet {

    /** Personal OpenWeatherMap API key (demo use only – do not ship). */
    private static final String API_KEY = "9d8cf06531efc0a69ca27ae06fe02151";

    /**
     * Handles POST /WeatherServlet
     * Expected form field: city
     */
    @Override
    protected void doPost(HttpServletRequest req,
                          HttpServletResponse resp)
            throws ServletException, IOException {

        /* ----------------------------------------------------------
           1) Pull city from request
        ---------------------------------------------------------- */
        String city = req.getParameter("city");

        /* ----------------------------------------------------------
           2) Build remote API URL
              • URL-encode the city to guard against spaces/specials
              • Metric units for °C
        ---------------------------------------------------------- */
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q="
                + URLEncoder.encode(city, "UTF-8")
                + "&appid=" + API_KEY
                + "&units=metric";

        resp.setContentType("application/json");

        try {
            /* ------------------------------------------------------
               3) Make HTTP call to OpenWeatherMap
            ------------------------------------------------------ */
            URL url = new URL(apiUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            /* Read the full JSON body */
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream()))) {

                String line;
                while ((line = br.readLine()) != null) sb.append(line);
            }

            /* ------------------------------------------------------
               4) Persist search + enrich with favourite status
            ------------------------------------------------------ */
            DBUtil.saveSearchHistory(city);          // async UX, sync DB
            boolean fav = DBUtil.isFavorite(city);   // check favourites

            JSONObject json = new JSONObject(sb.toString());
            json.put("isFavorite", fav);             // merge extra field

            /* ------------------------------------------------------
               5) Stream merged payload back to caller
            ------------------------------------------------------ */
            try (PrintWriter out = resp.getWriter()) {
                out.print(json.toString());
            }

        } catch (Exception ex) {                     // JSON / IO / API errors
            /* Uniform error envelope for client-side handling */
            try (PrintWriter out = resp.getWriter()) {
                out.print("{\"error\":\"City not found or API error.\"}");
            }
        }
    }
}
