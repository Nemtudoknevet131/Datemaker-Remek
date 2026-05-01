package com.example.datemaker.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datemaker.R;
import com.example.datemaker.model.CategoryDto;
import com.example.datemaker.model.IdeaDto;

import java.util.ArrayList;
import java.util.List;

public class InspirationCategoryAdapter extends RecyclerView.Adapter<InspirationCategoryAdapter.CategoryViewHolder> {

    private final Context context;
    private final InspirationIdeaAdapter.OnIdeaActionListener onIdeaActionListener;
    private List<CategoryDto> categories = new ArrayList<>();

    public InspirationCategoryAdapter(Context context,
                                      InspirationIdeaAdapter.OnIdeaActionListener onIdeaActionListener) {
        this.context = context;
        this.onIdeaActionListener = onIdeaActionListener;
    }

    public void submitList(List<CategoryDto> newCategories) {
        this.categories = newCategories != null ? newCategories : new ArrayList<>();
        notifyDataSetChanged(); // ✅ ensures RecyclerView updates
    }

    public List<CategoryDto> getCategories() {
        return categories;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_inspiration_category, parent, false);
        return new CategoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryDto category = categories.get(position);
        holder.bind(category);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageButton btnScrollRight, btnScrollLeft;
        TextView tvCategoryName;
        RecyclerView rvIdeas;
        InspirationIdeaAdapter ideaAdapter;

        int currentScrollPosition = 0;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            rvIdeas = itemView.findViewById(R.id.rvIdeas);
            btnScrollRight = itemView.findViewById(R.id.btnScrollRight);
            btnScrollLeft = itemView.findViewById(R.id.btnScrollLeft);

            LinearLayoutManager lm = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
            rvIdeas.setLayoutManager(lm);

            btnScrollRight.setOnClickListener(v -> {
                if (currentScrollPosition < ideaAdapter.getItemCount() - 1) {
                    currentScrollPosition++;
                    rvIdeas.smoothScrollToPosition(currentScrollPosition);
                }
            });

            btnScrollLeft.setOnClickListener(v -> {
                if (currentScrollPosition > 0) {
                    currentScrollPosition--;
                    rvIdeas.smoothScrollToPosition(currentScrollPosition);
                }
            });

            rvIdeas.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        currentScrollPosition = lm.findFirstVisibleItemPosition();
                        updateHeaderUI(lm);
                    }
                }

                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    updateHeaderUI(lm);
                }
            });

            rvIdeas.setHasFixedSize(false);

            ideaAdapter = new InspirationIdeaAdapter(context, onIdeaActionListener);
            rvIdeas.setAdapter(ideaAdapter);
        }

        private void updateHeaderUI(LinearLayoutManager lm) {
            int first = lm.findFirstVisibleItemPosition();
            int last = lm.findLastVisibleItemPosition();
            int count = ideaAdapter.getItemCount();

            if (first <= 0) {
                btnScrollLeft.setVisibility(View.INVISIBLE);
                btnScrollRight.setVisibility(View.VISIBLE);
                tvCategoryName.setGravity(android.view.Gravity.START);
            } else if (last >= count - 1) {
                btnScrollLeft.setVisibility(View.VISIBLE);
                btnScrollRight.setVisibility(View.INVISIBLE);
                tvCategoryName.setGravity(android.view.Gravity.END);
            } else {
                btnScrollLeft.setVisibility(View.VISIBLE);
                btnScrollRight.setVisibility(View.VISIBLE);
                tvCategoryName.setGravity(android.view.Gravity.CENTER);
            }
        }

        void bind(CategoryDto category) {
            tvCategoryName.setText(category.getName());
            List<IdeaDto> ideas = category.getIdeas();
            ideaAdapter.submitList(ideas);
        }
    }
}