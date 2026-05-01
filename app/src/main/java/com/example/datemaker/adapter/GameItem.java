package com.example.datemaker.adapter;

public class GameItem {
    public String title;
    public int imageResId;

    public GameItem(String title, int imageResId) {
        this.title = title;
        this.imageResId = imageResId;
    }

    public String getTitle() {
        return title;
    }

    public int getImageResId() {
        return imageResId;
    }
}
