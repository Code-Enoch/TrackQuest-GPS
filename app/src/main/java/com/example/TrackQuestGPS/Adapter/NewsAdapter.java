package com.example.TrackQuestGPS.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
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
        return new ViewHolder(view, context, newsList);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (newsList == null || newsList.isEmpty()) {
            Log.e("NewsAdapter", "No news items available!");
            return;
        }

        NewsItem item = newsList.get(position);
        holder.title.setText(item.getTitle());
        holder.description.setText(item.getDescription());

        String url = item.getUrl();
        if (url != null && !url.trim().isEmpty()) {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url; // Ensure proper format
            }
        } else {
            Log.e("NewsAdapter", "Invalid or missing URL for item: " + item.getTitle());
        }

        Glide.with(context)
                .load(item.getImageUrl())
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_error)
                .into(holder.image);

        Log.d("NewsAdapter", "Binding news item: " + item.getTitle());
    }

    @Override
    public int getItemCount() {
        return newsList != null ? newsList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, description;
        ImageView image;
        CardView cardView; // Making the entire card clickable

        public ViewHolder(View itemView, Context context, List<NewsItem> newsList) {
            super(itemView);
            title = itemView.findViewById(R.id.news_title);
            description = itemView.findViewById(R.id.news_description);
            image = itemView.findViewById(R.id.news_image);
            cardView = itemView.findViewById(R.id.CardView); // CardView is the entire item

            // Set click listener on the entire CardView
            cardView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && position < newsList.size()) {
                    String url = newsList.get(position).getUrl();
                    Log.d("NewsAdapter", "Opening URL: " + url);

                    if (url != null && !url.trim().isEmpty()) {
                        if (!url.startsWith("http://") && !url.startsWith("https://")) {
                            url = "https://" + url; // Ensure proper format
                        }

                        if (Patterns.WEB_URL.matcher(url).matches()) {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            context.startActivity(browserIntent);
                        } else {
                            Log.e("NewsAdapter", "Invalid URL format: " + url);
                            Toast.makeText(context, "Invalid URL format!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("NewsAdapter", "Empty or null URL");
                        Toast.makeText(context, "No valid link available!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}