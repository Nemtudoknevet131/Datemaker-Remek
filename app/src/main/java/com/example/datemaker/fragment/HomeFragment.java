package com.example.datemaker.fragment;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.datemaker.R;
import com.example.datemaker.adapter.HomeFragmentAdapter;
import com.example.datemaker.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewPager2 viewPager = binding.viewPager;
        viewPager.setAdapter(new HomeFragmentAdapter(this));

        view.post(() -> {
           ViewGroup.LayoutParams params = binding.tabIndicator.getLayoutParams();
           params.width = binding.tabOwnPrograms.getWidth();
           binding.tabIndicator.setLayoutParams(params);
        });

        binding.tabOwnPrograms.setOnClickListener(v -> {
            binding.viewPager.setCurrentItem(0, true);
            animateIndicator(0);
        });

        binding.tabMemoryLane.setOnClickListener(v -> {
            binding.viewPager.setCurrentItem(1, true);
            animateIndicator(1);
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                int tabWidth = binding.tabOwnPrograms.getWidth();
                binding.tabIndicator.setTranslationX((position + positionOffset) * tabWidth);
            }

            @Override
            public void onPageSelected(int position) {
                updateTabColors(position);
            }
        });
    }

    private void animateIndicator(int position) {
        int tabWidth = binding.tabOwnPrograms.getWidth();
        ObjectAnimator animator = ObjectAnimator.ofFloat(binding.tabIndicator, "translationX", position * tabWidth);
        animator.setDuration(200);
        animator.start();
    }

    private void updateTabColors(int position) {
        if (position == 0) {
            binding.tabOwnPrograms.setTextColor(Color.WHITE);
            binding.tabMemoryLane.setTextColor(Color.GRAY);
        } else {
            binding.tabOwnPrograms.setTextColor(Color.GRAY);
            binding.tabMemoryLane.setTextColor(Color.WHITE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
