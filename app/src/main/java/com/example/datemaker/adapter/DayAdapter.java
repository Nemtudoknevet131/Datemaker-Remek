package com.example.datemaker.adapter;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datemaker.R;
import com.example.datemaker.databinding.ItemDayBinding;
import com.example.datemaker.model.DayItem;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;


@RequiresApi(api = Build.VERSION_CODES.O)
public class DayAdapter extends RecyclerView.Adapter<DayAdapter.DayViewHolder> {
    public interface OnDayClickListener {
        void onDayClick(LocalDate date);
    }

    private List<DayItem> days;
    private OnDayClickListener onDayClickListener;
    private int itemWidth;

    public DayAdapter(List<DayItem> days, OnDayClickListener onDayClickListener, int itemWidth) {
        this.days = days;
        this.onDayClickListener = onDayClickListener;
        this.itemWidth = itemWidth;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDayBinding binding = ItemDayBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );

        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(
                itemWidth,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        binding.getRoot().setLayoutParams(params);

        return new DayViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        DayItem item = days.get(position);
        LocalDate date = item.getDate();

        String dow = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault());

        holder.binding.tvDayOfWeek.setText(dow);
        holder.binding.tvDayNumber.setText(String.valueOf(date.getDayOfMonth()));

        holder.binding.tvDayNumber.setBackgroundResource(
                item.isSelected() ? R.drawable.bg_day_selected : R.drawable.bg_day_unselected
        );

        holder.itemView.setOnClickListener(v -> {
            for (DayItem d : days) d.setSelected(false);

            item.setSelected(true);
            notifyDataSetChanged();

            if (onDayClickListener != null) {
                onDayClickListener.onDayClick(date);
            }
        });
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        ItemDayBinding binding;

        DayViewHolder(@NonNull ItemDayBinding binding) {
            super(binding.getRoot());

            this.binding = binding;
        }
    }
}
