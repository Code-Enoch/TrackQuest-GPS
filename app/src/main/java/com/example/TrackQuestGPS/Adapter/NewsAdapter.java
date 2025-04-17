package com.example.TrackQuestGPS.Adapter;

import android.content.Context;
import android.util.Log; // Added for debugging
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.TrackQuestGPS.Model.NewsItem;
import com.example.TrackQuestGPS.R;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {
    private Context context;
    private List<NewsItem> newsList;

    public NewsAdapter(Context context, List<NewsItem> newsList) {
        this.context = context;
        this.newsList = newsList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.news_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (newsList == null || newsList.isEmpty()) {
            Log.e("NewsAdapter", "No news items available!");
            return; // Avoid binding empty data
        }

        NewsItem item = newsList.get(position);
        holder.title.setText(item.getTitle());
        holder.description.setText(item.getDescription());

        // Add placeholder & error handling for missing images
        Glide.with(context)
                .load(item.getImageUrl())
                .placeholder(R.drawable.ic_placeholder) // Image while loading
                .error(R.drawable.ic_error) // Image if loading fails
                .into(holder.image);

        Log.d("NewsAdapter", "Binding news item: " + item.getTitle()); // Debug log
    }

    @Override
    public int getItemCount() {
        return newsList != null ? newsList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, description;
        ImageView image;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.news_title);
            description = itemView.findViewById(R.id.news_description);
            image = itemView.findViewById(R.id.news_image);
        }
    }
}