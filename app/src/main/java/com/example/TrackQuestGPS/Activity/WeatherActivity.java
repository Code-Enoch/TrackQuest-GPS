package com.example.TrackQuestGPS.Activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.TrackQuestGPS.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class WeatherActivity extends AppCompatActivity {
    // UI Elements
    private EditText searchBar;
    private Button searchButton, refreshButton;
    private TextView locationText, tempText, weatherDescText, rainText, humidityText, windText;
    private ImageView weatherIcon;

    // RecyclerView Elements
    private RecyclerView forecastRecyclerView;
    private ForecastAdapter forecastAdapter;
    private List<ForecastModel> forecastList;

    // Location Client
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        // Back arrow functionality
        ImageView weatherbackArrow = findViewById(R.id.weatherbackArrow);
        weatherbackArrow.setOnClickListener(v -> finish());

        // Initialize UI elements
        searchBar = findViewById(R.id.searchBar);
        searchButton = findViewById(R.id.searchButton);
        refreshButton = findViewById(R.id.refreshButton);
        locationText = findViewById(R.id.locationText);
        tempText = findViewById(R.id.tempText);
        weatherDescText = findViewById(R.id.weatherDescText);
        rainText = findViewById(R.id.rainText);
        humidityText = findViewById(R.id.humidityText);
        windText = findViewById(R.id.windText);
        weatherIcon = findViewById(R.id.weatherIcon);

        // Initialize RecyclerView for forecast
        forecastRecyclerView = findViewById(R.id.forecastRecyclerView);
        forecastRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        forecastList = new ArrayList<>();
        forecastAdapter = new ForecastAdapter(forecastList);
        forecastRecyclerView.setAdapter(forecastAdapter);

        // Initialize Location Client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Check for an externally provided search query from MainActivity
        String passedCityQuery = getIntent().getStringExtra("searchBar");
        if (passedCityQuery != null && !passedCityQuery.trim().isEmpty()) {
            // Set the search bar's text and fetch weather for that city
            searchBar.setText(passedCityQuery);
            fetchWeatherForCity(passedCityQuery);
        } else {
            // If no query is provided, fetch weather based on the current location
            fetchCurrentLocationWeather();
        }

        // Search button functionality
        searchButton.setOnClickListener(v -> {
            String cityName = searchBar.getText().toString().trim();
            if (!cityName.isEmpty()) {
                fetchWeatherForCity(cityName);
            } else {
                searchBar.setError("Please enter a valid city name");
            }
        });

        // Refresh button functionality
        refreshButton.setOnClickListener(v -> {
            String cityName = searchBar.getText().toString().trim();
            if (!cityName.isEmpty()) {
                fetchWeatherForCity(cityName);
            } else {
                fetchCurrentLocationWeather();
            }
        });
    }

    private void fetchCurrentLocationWeather() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                fetchWeatherByCoordinates(latitude, longitude);
            } else {
                fetchWeatherForCity("London"); // Fallback to default city
            }
        }).addOnFailureListener(e -> fetchWeatherForCity("London"));
    }

    private void fetchWeatherForCity(String cityName) {
        try {
            String apiKey = "57f091efab16d73d01ec6ebcabfdbddf"; // Replace with your API key
            String urlString = "https://api.openweathermap.org/data/2.5/forecast?q="
                    + URLEncoder.encode(cityName, "UTF-8") + "&units=metric&appid=" + apiKey;
            fetchWeatherFromAPI(urlString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fetchWeatherByCoordinates(double latitude, double longitude) {
        try {
            String apiKey = "57f091efab16d73d01ec6ebcabfdbddf"; // Replace with your API key
            String urlString = "https://api.openweathermap.org/data/2.5/forecast?lat="
                    + latitude + "&lon=" + longitude + "&units=metric&appid=" + apiKey;
            fetchWeatherFromAPI(urlString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fetchWeatherFromAPI(String urlString) {
        new Thread(() -> {
            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());

                // Parse current weather data
                JSONObject cityObject = jsonResponse.getJSONObject("city");
                String cityName = cityObject.getString("name");
                String country = cityObject.getString("country");

                JSONArray forecastArray = jsonResponse.getJSONArray("list");
                JSONObject currentWeather = forecastArray.getJSONObject(0);
                String temp = currentWeather.getJSONObject("main").getString("temp") + "°C";
                String weatherDesc = currentWeather.getJSONArray("weather")
                        .getJSONObject(0).getString("description");
                String iconCode = currentWeather.getJSONArray("weather")
                        .getJSONObject(0).getString("icon");
                String iconUrl = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";
                String humidity = currentWeather.getJSONObject("main").getString("humidity") + "%";
                String windSpeed = currentWeather.getJSONObject("wind").getString("speed") + " km/h";
                String rainChance = currentWeather.has("rain")
                        ? currentWeather.getJSONObject("rain").optString("1h", "0 mm")
                        : "0 mm";

                // Update UI with current weather data
                runOnUiThread(() -> {
                    locationText.setText(cityName + ", " + country);
                    tempText.setText(temp);
                    weatherDescText.setText(weatherDesc);
                    humidityText.setText(humidity);
                    windText.setText(windSpeed);
                    rainText.setText(rainChance);
                    Glide.with(weatherIcon.getContext()).load(iconUrl).into(weatherIcon);
                });

                // Parse 6-day forecast
                HashSet<String> addedDates = new HashSet<>();
                forecastList.clear();
                for (int i = 0; i < forecastArray.length(); i++) {
                    JSONObject forecast = forecastArray.getJSONObject(i);
                    String dateTime = forecast.getString("dt_txt");
                    String date = dateTime.split(" ")[0];
                    String time = dateTime.split(" ")[1];

                    if (!addedDates.contains(date) && time.equals("12:00:00")) {
                        String forecastTemp = forecast.getJSONObject("main").getString("temp") + "°C";
                        String forecastDesc = forecast.getJSONArray("weather")
                                .getJSONObject(0).getString("main");
                        String forecastIconCode = forecast.getJSONArray("weather")
                                .getJSONObject(0).getString("icon");
                        String forecastIconUrl = "https://openweathermap.org/img/wn/"
                                + forecastIconCode + "@2x.png";

                        forecastList.add(new ForecastModel(date, forecastDesc, forecastTemp, forecastIconUrl));
                        addedDates.add(date);
                    }

                    if (forecastList.size() == 6) break;
                }

                // Notify RecyclerView adapter
                runOnUiThread(() -> forecastAdapter.notifyDataSetChanged());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}