package com.example.TrackQuestGPS.Activity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ForecastModel {
    private String date; // Stores the date (yyyy-mm-dd)
    private String description; // Weather description
    private String temperature; // Temperature
    private String iconUrl; // Weather icon URL

    // Constructor
    public ForecastModel(String date, String description, String temperature, String iconUrl) {
        this.date = date;
        this.description = description;
        this.temperature = temperature;
        this.iconUrl = iconUrl;
    }

    // Method to get the day of the week
    public String getDayOfWeek() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            Date parsedDate = sdf.parse(date); // Parse the date string
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.ENGLISH); // Format to day of the week
            return dayFormat.format(parsedDate); // Returns the day of the week (e.g., "Monday")
        } catch (Exception e) {
            e.printStackTrace();
            return "Unknown"; // Fallback if parsing fails
        }
    }

    // Getters for other fields
    public String getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public String getTemperature() {
        return temperature;
    }

    public String getIconUrl() {
        return iconUrl;
    }
}