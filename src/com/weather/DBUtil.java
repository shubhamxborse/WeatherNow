package com.weather;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DBUtil – small static-helper class that hides all JDBC plumbing
 * for the WeatherNow demo.
 *
 * Responsibilities
 *  • Persist / fetch search-history records
 *  • Maintain favourites list
 *  • Provide simple CRUD-style helpers that the servlets can call
 */
public final class DBUtil {

    /* ------------------------------------------------------------------
       JDBC connection properties
    ------------------------------------------------------------------ */
    private static final String URL  = "jdbc:mysql://localhost:3306/weatherapp";
    private static final String USER = "root";
    private static final String PASS = "mysqlroot";

    /* ------------------------------------------------------------------
       Driver bootstrap – executed once when class is first loaded
    ------------------------------------------------------------------ */
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");   // MySQL 8.x driver
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);               // unrecoverable
        }
    }

    /** Hide constructor – utility class has only static members. */
    private DBUtil() {}

    /* ==============================================================
                           SEARCH HISTORY
       ============================================================== */

    /**
     * Insert a new search row (NOW() timestamped).
     */
    public static void saveSearchHistory(String city) {
        String sql = "INSERT INTO search_history (city,search_time) VALUES (?,NOW())";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, city);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Return last 10 searched cities (most-recent first).
     */
    public static List<String> getSearchHistory() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT city FROM search_history "
                + "ORDER BY search_time DESC LIMIT 10";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             Statement st  = conn.createStatement();
             ResultSet rs  = st.executeQuery(sql)) {

            while (rs.next()) list.add(rs.getString("city"));

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Delete a specific city from history.
     * @return true if at least one row was removed
     */
    public static boolean removeSearchHistory(String city) {
        String sql = "DELETE FROM search_history WHERE city = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, city);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /* ==============================================================
                              FAVOURITES
       ============================================================== */

    /**
     * Check if a city already exists in favourites table.
     */
    public static boolean isFavorite(String city) {
        String sql = "SELECT 1 FROM favorites WHERE city = ? LIMIT 1";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, city);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();        // true if at least one row
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Add city to favourites (no-op if already present).
     */
    public static boolean addFavorite(String city) {
        if (isFavorite(city)) return true;   // avoid duplicate key error

        String sql = "INSERT INTO favorites (city) VALUES (?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, city);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Remove city from favourites.
     */
    public static boolean removeFavorite(String city) {
        String sql = "DELETE FROM favorites WHERE city = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, city);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Fetch all favourite cities (alphabetical).
     */
    public static List<String> getAllFavorites() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT city FROM favorites ORDER BY city";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             Statement st  = conn.createStatement();
             ResultSet rs  = st.executeQuery(sql)) {

            while (rs.next()) list.add(rs.getString("city"));

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
