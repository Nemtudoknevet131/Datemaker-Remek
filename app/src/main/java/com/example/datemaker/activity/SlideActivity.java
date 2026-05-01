package com.example.datemaker.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.datemaker.R;
import com.example.datemaker.adapter.SlideAdapter;
import com.example.datemaker.adapter.SlideItem;
import com.example.datemaker.utils.AppLaunchManager;
import com.google.android.material.button.MaterialButton;
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;

import java.util.ArrayList;
import java.util.List;

public class SlideActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private WormDotsIndicator dotsIndicator;
    private MaterialButton btnGetStarted;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        AppLaunchManager launchManager = new AppLaunchManager(this);
        // launchManager.reset();

        if (!launchManager.isFirstLaunch()) {
            Intent intent = new Intent(SlideActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_slide);

        viewPager = findViewById(R.id.viewPager);
        dotsIndicator = findViewById(R.id.dotsIndicator);
        btnGetStarted = findViewById(R.id.btnGetStarted);

        List<SlideItem> slides = new ArrayList<>();

        slides.add(new SlideItem(R.drawable.random_pic1, "Preset dates with calendar"));
        slides.add(new SlideItem(R.drawable.random_pic2, "Add your partner easily"));
        slides.add(new SlideItem(R.drawable.random_pic3, "Get reminded of your next date"));

        SlideAdapter adapter = new SlideAdapter(slides);
        viewPager.setAdapter(adapter);
        dotsIndicator.attachTo(viewPager);

        btnGetStarted.setVisibility(View.GONE);
        btnGetStarted.setAlpha(0f);
        btnGetStarted.setScaleX(0.8f);
        btnGetStarted.setScaleY(0.8f);

        LayoutInflater inflater = getLayoutInflater();
        View otherLayout = inflater.inflate(R.layout.item_slide, null);

        ImageView slideImage = otherLayout.findViewById(R.id.slide_image);
        TextView slideText = otherLayout.findViewById(R.id.slide_text);


        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == slides.size() - 1) {
                    btnGetStarted.setVisibility(View.VISIBLE);

                    btnGetStarted
                            .animate()
                            .setStartDelay(150)
                            .alpha(1f)
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(700)
                            .setInterpolator(new OvershootInterpolator(0.6f))
                            .start();

                } else {
                    btnGetStarted.animate()
                            .alpha(0f)
                            .scaleX(0.9f)
                            .scaleY(0.9f)
                            .setDuration(200)
                            .setInterpolator(new AccelerateInterpolator())
                            .withEndAction(() -> btnGetStarted.setVisibility(View.GONE))
                            .start();
                }
            }
        });

        btnGetStarted.setOnClickListener(v -> {
            launchManager.setLaunched();

            Intent intent = new Intent(SlideActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
        }
    }
}
