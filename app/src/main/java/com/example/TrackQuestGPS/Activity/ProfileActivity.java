package com.example.TrackQuestGPS.Activity;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.TrackQuestGPS.R;

public class ProfileActivity extends AppCompatActivity {

        private RecyclerView.Adapter adapterNewsList;
        private RecyclerView recyclerViewNews;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_profile);

            // Initialize UI elements
            ImageView profileBackButton = findViewById(R.id.profileBackButton); // Ensure this matches your layout XML

            // Set click listener for back arrow
            profileBackButton.setOnClickListener(v -> finish()); // Closes WeatherActivity

        }
    }
