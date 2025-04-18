package com.example.TrackQuestGPS.Model;

public class NewsItem {
    private String title, description, imageUrl, url;

    public NewsItem(String title, String description, String imageUrl, String url) {
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.url = (url.startsWith("http://") || url.startsWith("https://")) ? url : "https://" + url;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public String getUrl() { return url; }
}
