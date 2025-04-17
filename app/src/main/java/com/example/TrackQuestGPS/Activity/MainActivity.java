package com.example.TrackQuestGPS.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.TrackQuestGPS.Adapter.NewsAdapter;
import com.example.TrackQuestGPS.Model.NewsItem;
import com.example.TrackQuestGPS.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NewsAdapter adapter;
    private List<NewsItem> newsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigation();
        MapNavigation();
        WeatherApp();
        TravelApp();

        recyclerView = findViewById(R.id.recyclerViewNews);
        newsList = new ArrayList<>();
        adapter = new NewsAdapter(this, newsList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadNewsItems(); // Initial load

        findViewById(R.id.refreshButton).setOnClickListener(v -> {
            loadNewsItems();
            Toast.makeText(MainActivity.this, "News updated!", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadNewsItems() {
        String apiKey = "635910aca070a3aabe68caff623ca9ad"; // replace with your actual GNews key
        String url = "https://gnews.io/api/v4/top-headlines?lang=en&country=us&max=10&apikey=" + apiKey;

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        newsList.clear();
                        JSONArray articles = response.getJSONArray("articles");

                        for (int i = 0; i < articles.length(); i++) {
                            JSONObject article = articles.getJSONObject(i);
                            String title = article.getString("title");
                            String description = article.optString("description", "No description available");
                            String imageUrl = article.optString("image", "");

                            newsList.add(new NewsItem(title, description, imageUrl));
                        }

                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Failed to parse news", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(MainActivity.this, "Error fetching news", Toast.LENGTH_SHORT).show()
        );

        queue.add(request);
    }

    private void MapNavigation() {
        LinearLayout mapButton = findViewById(R.id.MapButton);
        mapButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MapActivity.class)));
    }

    private void WeatherApp() {
        LinearLayout weatherButton = findViewById(R.id.WeatherButton);
        weatherButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, WeatherActivity.class)));
    }

    private void TravelApp() {
        LinearLayout travelButton = findViewById(R.id.TravelButton);
        travelButton.setOnClickListener(v -> {
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage("uk.co.nationalrail.google");
            if (launchIntent != null) {
                startActivity(launchIntent);
            } else {
                Toast.makeText(MainActivity.this, "National Rail app is not installed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void BottomNavigation() {
        LinearLayout profileButton = findViewById(R.id.profileBtn);
        profileButton.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, ProfileActivity.class)));
    }
}
