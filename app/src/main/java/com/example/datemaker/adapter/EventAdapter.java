package com.example.datemaker.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.compose.material3.AlertDialogKt;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datemaker.R;
import com.example.datemaker.databinding.ItemEventBinding;
import com.example.datemaker.model.DateEventDto;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    public interface OnColorChangeListener {
        void onColorChange(DateEventDto event, String newColorHex);
    }

    public interface OnEventLongClickListener {
        void onEventLongClick(DateEventDto event);
    }

    private List<DateEventDto> events = new ArrayList<>();
    private OnColorChangeListener onColorChangeListener;
    private OnEventLongClickListener onEventLongClickListener;

    public EventAdapter(OnColorChangeListener onColorChangeListener, OnEventLongClickListener onEventLongClickListener) {
        this.onColorChangeListener = onColorChangeListener;
        this.onEventLongClickListener = onEventLongClickListener;
    }

    public void setEvents(List<DateEventDto> events) {
        this.events = events != null ? events : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemEventBinding binding = ItemEventBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );

        return new EventViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        DateEventDto event = events.get(position);

        holder.binding.tvOwnerName.setText(event.getOwnerName());
        holder.binding.tvEventTitle.setText(event.getTitle());
        holder.binding.tvEventDescription.setText(event.getDescription());

        if (event.getTime() != null && !event.getTime().isBlank()) {
            holder.binding.tvDateTime.setText(event.getTime());
            holder.binding.tvDateTime.setVisibility(View.VISIBLE);
        } else {
            holder.binding.tvDateTime.setVisibility(View.GONE);
        }

        MaterialCardView card = (MaterialCardView) holder.binding.getRoot();

        try {
            String color = event.getColorHex() != null ? event.getColorHex() : "#9C27B0";
            card.setCardBackgroundColor(Color.parseColor(color));
        } catch (Exception e) {
            card.setCardBackgroundColor(Color.WHITE);
        }

        holder.binding.btnColorPicker.setOnClickListener(v -> {
            if (onColorChangeListener != null) {
                showColorDialog(v.getContext(), event);
            }
        });

        card.setOnLongClickListener(v -> {
            if (onEventLongClickListener != null && event.isCanDelete()) {
                onEventLongClickListener.onEventLongClick(event);
                return true;
            }

            return false;
        });
    }

    private void showColorDialog(Context context, DateEventDto event) {
        String[] colorNames = {"Red", "Pink", "Purple", "Blue", "Green", "Yellow", "Orange"};
        String[] colorHexes = {"#F44336", "#E91E63", "#9C27B0", "#2196F3", "#4CAF50", "#FFEB3B", "#FF9800"};

        new AlertDialog.Builder(context)
                .setTitle(R.string.change_color)
                .setItems(colorNames, (dialog, which) -> {
                    String selectedColor = colorHexes[which];
                    if (onColorChangeListener != null) {
                        onColorChangeListener.onColorChange(event, selectedColor);
                    }
                }).show();
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        ItemEventBinding binding;

        EventViewHolder(@NonNull ItemEventBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
