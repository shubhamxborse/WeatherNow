CREATE DATABASE IF NOT EXISTS weatherapp;
USE weatherapp;

-- Record search history
CREATE TABLE IF NOT EXISTS search_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    city VARCHAR(100),
    search_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Cities marked as favorites
CREATE TABLE IF NOT EXISTS favorites (
    id INT AUTO_INCREMENT PRIMARY KEY,
    city VARCHAR(100) UNIQUE
);
