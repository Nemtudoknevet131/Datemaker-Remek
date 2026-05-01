package com.example.datemaker.model;

public class DateEventRequest {
    private String date;
    private String title;
    private String description;
    private String colorHex;
    private String time;

    public DateEventRequest() {

    }

    public DateEventRequest(String date, String title, String description, String colorHex, String time) {
        this.date = date;
        this.title = title;
        this.description = description;
        this.colorHex = colorHex;
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getColorHex() {
        return colorHex;
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
