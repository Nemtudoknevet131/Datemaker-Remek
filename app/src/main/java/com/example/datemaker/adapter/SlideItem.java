package com.example.datemaker.adapter;

public class SlideItem {
    private int imageResId;
    private String title;

    public SlideItem(int imageResId, String title) {
        this.imageResId = imageResId;
        this.title = title;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getTitle() {
        return title;
    }
}
