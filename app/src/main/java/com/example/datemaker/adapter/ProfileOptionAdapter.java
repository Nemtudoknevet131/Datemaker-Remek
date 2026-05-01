package com.example.datemaker.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datemaker.R;
import com.example.datemaker.model.ProfileOption;

import java.util.List;

public class ProfileOptionAdapter extends RecyclerView.Adapter<ProfileOptionAdapter.OptionVH> {
    public interface OnItemClickListener {
        void onItemClick(ProfileOption option);
    }

    private final OnItemClickListener listener;
    private final List<ProfileOption> items;

    private boolean partnerEnabled = false;

    public ProfileOptionAdapter(List<ProfileOption> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void setPartnerEnabled(boolean partnerEnabled) {
        this.partnerEnabled = partnerEnabled;

        int index = -1;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).id == 2) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            notifyItemChanged(index);
        }
    }

    @NonNull
    @Override
    public OptionVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile_option, parent, false);
        return new OptionVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull OptionVH holder, int position) {
        ProfileOption option = items.get(position);
        holder.bind(option, listener, partnerEnabled);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class OptionVH extends RecyclerView.ViewHolder {
        private final ImageView icon;
        private final TextView title;
        private final View root;

        public OptionVH(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            title = itemView.findViewById(R.id.title);
            root = itemView;
        }

        public void bind(ProfileOption option, OnItemClickListener listener, boolean partnerEnabled) {
            icon.setImageResource(option.iconRes);
            title.setText(option.title);

            root.setAlpha(1f);
            root.setEnabled(true);

            if (option.id == 2 && !partnerEnabled) {
                root.setAlpha(0.4f);
                root.setEnabled(false);
            }

            root.setOnClickListener(v -> {
                if (option.id == 2 && !partnerEnabled) return;
                if (listener != null) listener.onItemClick(option);
            });
        }
    }
}
