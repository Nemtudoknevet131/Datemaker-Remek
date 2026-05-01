package com.example.datemaker.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.compose.ui.graphics.vector.ImageVector;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datemaker.R;

import java.util.List;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(GameItem item);
    }

    private List<GameItem> items;
    private OnItemClickListener listener;

    public GameAdapter(List<GameItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_game_card, parent, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        GameItem item = items.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class GameViewHolder extends RecyclerView.ViewHolder {
        private ImageView gameImage;
        private TextView gameTitle;

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            gameImage = itemView.findViewById(R.id.gameImage);
            gameTitle = itemView.findViewById(R.id.gameTitle);
        }

        public void bind(final GameItem item, final OnItemClickListener listener) {
            gameTitle.setText(item.getTitle());
            gameImage.setImageResource(item.getImageResId());

            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onItemClick(item);
                    }
                }
            });
        }
    }
}
