package com.example.datemaker.adapter;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.datemaker.R;
import com.example.datemaker.model.ProgramItem;

import java.util.List;

public class ProgramsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnAddClickListener {
        void onAddClicker(int position);
    }

    public interface OnBoxLongClickListener {
        void onBoxLongClick(int position);
    }

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_ADD = 1;

    private final List<ProgramItem> programItems;
    private final OnAddClickListener onAddClickListener;
    private final OnBoxLongClickListener onBoxLongClickListener;

    public ProgramsAdapter(List<ProgramItem> programItems,
                           OnAddClickListener onAddClickListener,
                           OnBoxLongClickListener onBoxLongClickListener) {
        this.programItems = programItems;
        this.onAddClickListener = onAddClickListener;
        this.onBoxLongClickListener = onBoxLongClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        return programItems.get(position).isPlaceHolder() ? TYPE_ADD : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ADD) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_program_add, parent, false);
            return new AddViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_program_image, parent, false);
            return new ImageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ProgramItem programItem = programItems.get(position);

        if (holder instanceof AddViewHolder) {
            ((AddViewHolder) holder).bind(position);
        } else if (holder instanceof ImageViewHolder) {
            ((ImageViewHolder) holder).bind(programItem, position);
        }
    }

    @Override
    public int getItemCount() {
        return programItems.size();
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textViewCaption;
        ImageButton buttonMore;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.boxImage);
            textViewCaption = itemView.findViewById(R.id.boxTextView);
            buttonMore = itemView.findViewById(R.id.buttonMore);
        }

        void bind(ProgramItem programItem, int position) {
            textViewCaption.setText(programItem.getCaption());

            Uri uri = programItem.getImageUri();

            Glide.with(imageView.getContext())
                    .load(uri)
                    .centerCrop()
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e,
                                                    Object model,
                                                    Target<Drawable> target,
                                                    boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource,
                                                       Object model,
                                                       Target<Drawable> target,
                                                       DataSource dataSource,
                                                       boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(imageView);

            // show dialog for delete
            buttonMore.setOnClickListener(v -> {
                if (onBoxLongClickListener != null) onBoxLongClickListener.onBoxLongClick(position);
            });
        }
    }

    class AddViewHolder extends RecyclerView.ViewHolder {

        AddViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        void bind(int position) {
            itemView.setOnClickListener(v -> {
                if (onAddClickListener != null) onAddClickListener.onAddClicker(position);
            });
        }
    }
}
