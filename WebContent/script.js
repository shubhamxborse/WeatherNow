/* ==============================================================
   File: script.js
   Project: WeatherNow
   Purpose: Front-end logic—theme toggle, weather fetch, favourites
            management, search history, and misc. utilities.
============================================================== */

/* --------------------------------------------------------------
   RUN WHEN DOM IS READY
-------------------------------------------------------------- */
document.addEventListener("DOMContentLoaded", () => {

    /* ---------- THEME SWITCH ---------- */
    const body   = document.body;
    const toggle = document.getElementById("modeToggle");

    /* Initialise theme (persisted in localStorage) */
    const stored        = localStorage.getItem("theme");
    const defaultTheme  = stored ? stored : "light";
    body.classList.add(defaultTheme);

    /**
     * paintIcon – swaps the Feather icon based on current theme and
     *             triggers a quick rotation animation for feedback.
     */
    function paintIcon() {
        const iconName = body.classList.contains("dark") ? "sun" : "moon";

        // Insert rotating icon element
        toggle.innerHTML =
            `<i class="icon-rotate" data-feather="${iconName}"></i>`;

        // Ask Feather to replace <i> with inline SVG
        if (window.feather) {
            feather.replace();
        }

        // Remove the animation class after it finishes so it can be re-used
        setTimeout(() => {
            const icon = toggle.querySelector("i");
            if (icon) icon.classList.remove("icon-rotate");
        }, 400);
    }

    paintIcon(); // paint the initial icon

    /* Toggle handler */
    toggle.addEventListener("click", () => {
        body.classList.toggle("dark");
        body.classList.toggle("light");

        // Persist user preference
        localStorage.setItem(
            "theme",
            body.classList.contains("dark") ? "dark" : "light"
        );

        paintIcon(); // repaint icon to match new state
    });

    /* ---------- EVENT BINDINGS ---------- */
    document.getElementById("searchBtn").addEventListener("click", getWeather);

    /* Pre-load side panels */
    loadHistory();
    loadFavorites();
});

/* ==============================================================
   WEATHER SEARCH
============================================================== */
function getWeather() {
    const city   = document.getElementById("city").value.trim();
    const result = document.getElementById("result");

    /* Guard clause: empty input */
    if (!city) {
        result.innerHTML = "<p class='error'>Please enter a city!</p>";
        return;
    }

    result.textContent = "Loading…";

    /* Call back-end servlet */
    fetch("WeatherServlet", {
        method: "POST",
        body  : new URLSearchParams({ city })
    })
    .then(r => r.json())
    .then(j => {
        /* ----- Error from server ----- */
        if (j.error) {
            result.innerHTML = `<p class="error">${j.error}</p>`;
            return;
        }

        const resultCard = document.getElementById("resultCard");
        resultCard.classList.remove("hidden");

        /* Re-trigger entry animation */
        resultCard.classList.remove("animated");
        void resultCard.offsetWidth;     // force reflow
        resultCard.classList.add("animated");

        /* Weather icon (deterministic mapping) */
        const icon = getWeatherIcon(j.weather[0].main);

        /* Build result markup */
        result.innerHTML = `
      <div class="weather-box">
         <h2>${j.name}</h2>
         <div class="weather-desc">${j.weather[0].description}</div>

         <img class="weather-icon-img" src="${icon}" alt="">

         <table class="info-table">
           <tr><td class="info-label">Temperature</td>
               <td class="info-value">${Math.round(j.main.temp)} °C</td></tr>
           <tr><td class="info-label">Feels Like</td>
               <td class="info-value">${Math.round(j.main.feels_like)} °C</td></tr>
           <tr><td class="info-label">Humidity</td>
               <td class="info-value">${j.main.humidity}%</td></tr>
           <tr><td class="info-label">Wind Speed</td>
               <td class="info-value">${j.wind.speed} m/s</td></tr>
           <tr><td class="info-label">Pressure</td>
               <td class="info-value">${j.main.pressure} hPa</td></tr>
         </table>

         <button id="favBtn"
                 class="favorite-btn ${j.isFavorite ? "favorited" : ""}">
             ${j.isFavorite ? "Remove favourite" : "Add to favourites"}
         </button>
      </div>`;

        /* Bind favourite-toggle button inside freshly rendered card */
        document.getElementById("favBtn").onclick =
            () => toggleFavorite(j.name, !j.isFavorite);

        /* Refresh history sidebar (new search is auto-logged server-side) */
        loadHistory();
    })
    .catch(() => {
        result.innerHTML = "<p class='error'>Unable to fetch data.</p>";
    });
}

/* ==============================================================
   FAVOURITES
============================================================== */

/**
 * toggleFavorite – add or remove a city from server-side favourites.
 * @param {string}  city – City name.
 * @param {boolean} add  – true:add, false:remove
 */
function toggleFavorite(city, add) {
    fetch("FavoriteServlet", {
        method: "POST",
        body  : new URLSearchParams({ city, action: add ? "add" : "remove" })
    })
    .then(r => r.json())
    .then(j => {
        if (j.success) {
            loadFavorites();  // refresh list

            /* Update button state inside result card (if present) */
            const btn = document.getElementById("favBtn");
            if (btn) {
                if (add) {
                    btn.classList.add("favorited");
                    btn.textContent = "Remove favourite";
                } else {
                    btn.classList.remove("favorited");
                    btn.textContent = "Add to favourites";
                }
            }
        }
    });
}

/* Convenience wrapper for inline list button */
function removeFavorite(city) {
    toggleFavorite(city, false);
}

/* ==============================================================
   HISTORY
============================================================== */

/* Remove single history item then reload list */
function removeHistory(city) {
    fetch("HistoryServlet", {
        method: "POST",
        body  : new URLSearchParams({ city })
    })
    .then(r => r.json())
    .then(j => { if (j.success) loadHistory(); });
}

/* ==============================================================
   LIST LOADERS
   (Populate side panels on demand)
============================================================== */

/* ---------- Favourites ---------- */
function loadFavorites() {
    fetch("FavoriteServlet")
    .then(r => r.json())
    .then(arr => {
        const ul = document.getElementById("favoritesUl");
        ul.innerHTML = "";
        arr.forEach(c => {
            ul.innerHTML += `
              <li>${c}
                  <button class="list-btn"
                          onclick="removeFavorite('${c}')">&times;</button>
              </li>`;
        });
    });
}

/* ---------- History ---------- */
function loadHistory() {
    fetch("HistoryServlet")
    .then(r => r.json())
    .then(arr => {
        const ul = document.getElementById("historyUl");
        ul.innerHTML = "";
        arr.forEach(c => {
            ul.innerHTML += `
              <li>${c}
                  <button class="list-btn"
                          onclick="removeHistory('${c}')">&times;</button>
              </li>`;
        });
    });
}

/* ==============================================================
   UTILITIES
============================================================== */

/**
 * getWeatherIcon – maps OpenWeather ‘main’ strings to local SVG icons.
 * @param  {string} main – e.g., "Clear", "Clouds"
 * @return {string}      – relative asset path
 */
function getWeatherIcon(main) {
    switch (main.toLowerCase()) {
        case "clear"       : return "weather-icons/clear.svg";
        case "clouds"      : return "weather-icons/clouds.svg";
        case "rain"        : return "weather-icons/rain.svg";
        case "snow"        : return "weather-icons/snow.svg";
        case "thunderstorm": return "weather-icons/thunderstorm.svg";
        case "mist":
        case "fog"         : return "weather-icons/mist.svg";
        default            : return "weather-icons/clear.svg";
    }
}
