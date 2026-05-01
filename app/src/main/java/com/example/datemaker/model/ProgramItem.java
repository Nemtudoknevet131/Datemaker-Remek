package com.example.datemaker.model;

import android.net.Uri;

import com.google.gson.annotations.SerializedName;

public class ProgramItem {
    private long id;
    @SerializedName("imageUrl")
    private String imageUrl;
    private transient Uri imageUri;
    private String caption;
    private boolean isPlaceHolder;

    public ProgramItem(boolean isPlaceHolder) {
        this.isPlaceHolder = isPlaceHolder;
    }

    public ProgramItem(Uri imageUri, String caption) {
        this.imageUri = imageUri;
        this.caption = caption;
    }

    public ProgramItem(long id, String imageUrl, String caption) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.caption = caption;
        this.isPlaceHolder = false;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Uri getImageUri() {
        if (imageUri != null) return imageUri;
        if (imageUrl != null) return Uri.parse(imageUrl);
        return null;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public boolean isPlaceHolder() {
        return isPlaceHolder;
    }

    public void setIsPlaceHolder(boolean isPlaceHolder) {
        this.isPlaceHolder = isPlaceHolder;
    }
}
