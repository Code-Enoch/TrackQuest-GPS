package com.example.TrackQuestGPS.Activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.TrackQuestGPS.R;

import java.util.List;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ViewHolder> {
    private final List<ForecastModel> forecastList;

    // Constructor
    public ForecastAdapter(List<ForecastModel> forecastList) {
        this.forecastList = forecastList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.forecast_item, parent, false); // Ensure forecast_item.xml exists
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ForecastModel forecast = forecastList.get(position);

        // Display the day of the week
        holder.dayText.setText(forecast.getDayOfWeek());

        // Display the temperature
        holder.tempText.setText(forecast.getTemperature());

        // Load the weather icon dynamically using Glide
        Glide.with(holder.weatherIcon.getContext())
                .load(forecast.getIconUrl())
                .placeholder(R.drawable.ic_placeholder) // Replace with your placeholder drawable
                .error(R.drawable.ic_error) // Replace with your error drawable
                .into(holder.weatherIcon);
    }

    @Override
    public int getItemCount() {
        return forecastList.size();
    }

    // ViewHolder class to hold and manage the forecast item layout
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dayText, tempText;
        ImageView weatherIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            dayText = itemView.findViewById(R.id.dayText); // Ensure these IDs match forecast_item.xml
            tempText = itemView.findViewById(R.id.tempText);
            weatherIcon = itemView.findViewById(R.id.weatherIcon);
        }
    }
}
