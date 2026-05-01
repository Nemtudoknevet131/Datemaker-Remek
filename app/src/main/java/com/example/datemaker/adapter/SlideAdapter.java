package com.example.datemaker.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datemaker.R;

import java.util.List;

public class SlideAdapter extends RecyclerView.Adapter<SlideAdapter.SlideViewHolder> {

    private List<SlideItem> slideItems;

    public SlideAdapter(List<SlideItem> slideItems) {
        this.slideItems = slideItems;
    }

    @NonNull
    @Override
    public SlideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_slide, parent, false);
        return new SlideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SlideViewHolder holder, int position) {
        SlideItem slideItem = slideItems.get(position);
        holder.imageView.setImageResource(slideItem.getImageResId());
        holder.textView.setText(slideItem.getTitle());
    }

    public int getItemCount() {
        return slideItems.size();
    }

    static class SlideViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;

        public SlideViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.slide_image);
            textView = itemView.findViewById(R.id.slide_text);
        }
    }
}
