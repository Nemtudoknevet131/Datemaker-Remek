package com.example.datemaker.model;

import java.time.LocalDate;

public class DayItem {
    private LocalDate date;
    private boolean selected;

    public DayItem(LocalDate date, boolean selected) {
        this.date = date;
        this.selected = selected;
    }

    public LocalDate getDate() {
        return date;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
