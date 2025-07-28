## Overview
**WeatherNow** is a lightweight, single-page dashboard that delivers real-time city-level weather information.  
The interface is built with HTML 5, CSS 3 and modern JavaScript, while a slim Jakarta-Servlet back end proxies the OpenWeatherMap REST API and persists user data (search history and favourites) in MySQL.

## Key Features
-  **Instant Search** – Live temperature, humidity, pressure and wind speed.
-  **Dark / Light Theme** – One-click toggle, preference stored in `localStorage`.
-  **Favourites** – Add / Remove cities; data saved in the database.
-  **Search History** – Last 10 look-ups shown and removable.
-  **Responsive UI** – Adapts from large desktops to 320 px phones.
-  **Framework-Free** – No React, Spring, ORM or other heavy dependencies.

## Technology Stack

| Layer        | Technology                                                                    |
|--------------|-------------------------------------------------------------------------------|
| Front End    | HTML 5, CSS 3, JavaScript |
| Back End     | Core Java, Jakarta Servlets 6.0  |
| Database     | MySQL 8.0 or newer                                                     |
| Runtime      | Apache Tomcat 10+, JDK 17+                 |

---

## Prerequisites
1. **JDK 17** or newer  
2. **Apache Tomcat 10+**
3. **MySQL 8.0** with a schema named `weatherapp`  
4. An **OpenWeatherMap API Key**
5. JAR dependencies 
   - jakarta.servlet-api-5.0.0.jar 
   - json-20231013.jar
   - mysql-connector-j-9.0.0.jar 

## Project Structure

- WeatherNow  
  - src/com/weather     
          - DBUtil.java  
          - WeatherServlet.java  
          - FavoriteServlet.java  
          - HistoryServlet.java  
  - webapp  
        - index.html  
        - style.css  
        - script.js  
        - weather-icons/ (`*.svg`)
  - WEB-INF
     - web.xml
     - lib
          - mysql-connector-j-9.0.0.jar 
          - jakarta.servlet-api-5.0.0.jar 
          - json-20231013.jar
  - README.md


## How it works 
**1. Open the web page.**

  The browser shows the search bar and two empty lists (Favourites and History).
  A small script decides whether to show the page in dark or light mode, based on what you picked last time.


**2. Type a city name and press Search.**

  The page sends that city name to a tiny Java program on the server called WeatherServlet.
  WeatherServlet asks OpenWeatherMap for the current weather of that city.
  It also saves the city name in a search_history table so it can appear later in your History list.
  It checks another table, favorites, to see if the city is already one of your favourites.
  Finally, it sends the weather data (plus a yes/no “is favourite” flag) back to your browser.


**3. The browser receives the data.**

  It shows the weather card (temperature, humidity, etc.).
  It adds the city to the History list on the side.
  If the city is already a favourite, the “Add to favourites” button is shown as “Remove favourite.”


**4. Click Add to favourites (or Remove favourite).**

  The page tells another server program, FavoriteServlet, to add or remove that city in the favorites table.
  When the server confirms, the browser updates the Favourites list immediately.


**5. Click the “X” next to a history entry.**

  The page asks HistoryServlet to delete that city from the search_history table.
  After success, the list refreshes without the deleted item.


**6. Click the Dark mode icon.**
   
  The page simply flips between dark and light style colours and remembers your choice in the browser’s local storage—no server call needed.
  
  

---

## Contributors
| Name | Role  |
|------|---------------------|
| **Shubham Borse** | Original author & full-stack implementation |


