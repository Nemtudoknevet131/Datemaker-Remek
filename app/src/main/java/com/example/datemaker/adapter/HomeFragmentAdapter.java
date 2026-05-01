package com.example.datemaker.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.datemaker.fragment.MemoryLaneFragment;
import com.example.datemaker.fragment.OwnProgramsFragment;

public class HomeFragmentAdapter extends FragmentStateAdapter {
    public HomeFragmentAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new OwnProgramsFragment();
        } else {
            return new MemoryLaneFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
