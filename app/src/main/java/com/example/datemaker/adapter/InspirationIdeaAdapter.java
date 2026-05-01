package com.example.datemaker.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.datemaker.R;
import com.example.datemaker.model.IdeaDto;

import java.util.ArrayList;
import java.util.List;

public class InspirationIdeaAdapter extends RecyclerView.Adapter<InspirationIdeaAdapter.IdeaViewHolder> {

    public interface OnIdeaActionListener {
        void onLikeClicked(IdeaDto idea);
        void onDislikeClicked(IdeaDto idea);
        void onIdeaLongClicked(IdeaDto idea);
    }

    private final Context context;
    private final OnIdeaActionListener listener;
    private List<IdeaDto> ideas = new ArrayList<>();

    public InspirationIdeaAdapter(Context context, OnIdeaActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void submitList(List<IdeaDto> newIdeas) {
        this.ideas = newIdeas != null ? newIdeas : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public IdeaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_inspiration_idea, parent, false);
        return new IdeaViewHolder(v);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull IdeaViewHolder holder, int position) {
        IdeaDto idea = ideas.get(position);

        holder.tvIdeaTitle.setText(idea.getTitle());
        holder.tvLikeCount.setText(String.valueOf(idea.getLikeCount()));

        String baseUrl = com.example.datemaker.retrofit.RetrofitClient.BASE_URL;
        String imgPath = idea.getImageUrl();

        baseUrl = baseUrl.replaceFirst("/*$", "");
        imgPath = imgPath.replaceFirst("^/*", "");
        String fullImageUrl = baseUrl + "/" + imgPath;

        android.util.Log.d("IMAGE_DEBUG", "Glide próbálja betölteni: " + fullImageUrl);

        Glide.with(context)
                .load(fullImageUrl)
                .centerCrop()
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(holder.ivIdea);

        holder.btnLike.setTextColor(Color.WHITE);
        holder.btnDislike.setTextColor(Color.parseColor("#E0E0E0"));

        String reaction = idea.getCurrentUserReaction();
        if ("LIKE".equals(reaction)) {
            holder.btnLike.setTextColor(Color.parseColor("#FF6FD8"));
        } else if ("DISLIKE".equals(reaction)) {
            holder.btnDislike.setTextColor(Color.parseColor("#FF6FD8"));
        }

        holder.btnLike.setOnClickListener(v -> {
            if (listener != null) listener.onLikeClicked(idea);
        });

        holder.btnDislike.setOnClickListener(v -> {
            if (listener != null) listener.onDislikeClicked(idea);
        });

        holder.ivIdea.setClickable(true);
        holder.ivIdea.setFocusable(true);

        holder.ivIdea.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onIdeaLongClicked(idea);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return ideas != null ? ideas.size() : 0;
    }

    static class IdeaViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIdea;
        TextView tvIdeaTitle;
        TextView btnLike;
        TextView tvLikeCount;
        TextView btnDislike;

        IdeaViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIdea = itemView.findViewById(R.id.ivIdea);
            tvIdeaTitle = itemView.findViewById(R.id.tvIdeaTitle);
            btnLike = itemView.findViewById(R.id.btnLike);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            btnDislike = itemView.findViewById(R.id.btnDislike);
        }
    }
}