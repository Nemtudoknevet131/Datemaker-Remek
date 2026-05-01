package com.example.datemaker.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datemaker.R;
import com.example.datemaker.model.PartnerRequestDto;

import java.util.List;

public class InboxRequestAdapter extends RecyclerView.Adapter<InboxRequestAdapter.ViewHolder> {
    public interface OnRequestAction {
        void onAction(PartnerRequestDto item);
    }

    private List<PartnerRequestDto> items;
    private OnRequestAction onAccept;
    private OnRequestAction onReject;

    public InboxRequestAdapter(List<PartnerRequestDto> items, OnRequestAction onAccept, OnRequestAction onReject) {
        this.items = items;
        this.onAccept = onAccept;
        this.onReject = onReject;
    }

    public void setItems(List<PartnerRequestDto> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InboxRequestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_partner_request, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull InboxRequestAdapter.ViewHolder holder, int position) {
        PartnerRequestDto item = items.get(position);

        String name = item.getFromUserName() != null ? item.getFromUserName() : "Someone";

        holder.txtName.setText(name);

        TextView txtMessage = holder.itemView.findViewById(R.id.txtMessage);
        txtMessage.setText(name + " has sent you a partner request");

        holder.btnAccept.setOnClickListener(v -> onAccept.onAction(item));
        holder.btnReject.setOnClickListener(v -> onReject.onAction(item));
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName;
        Button btnAccept;
        Button btnReject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}
