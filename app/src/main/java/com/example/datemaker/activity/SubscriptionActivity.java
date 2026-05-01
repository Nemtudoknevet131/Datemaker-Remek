package com.example.datemaker.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.datemaker.R;
import com.example.datemaker.databinding.ActivitySubscriptionBinding;
import com.example.datemaker.model.UserDto;
import com.example.datemaker.retrofit.ApiService;
import com.example.datemaker.retrofit.RetrofitClient;
import com.example.datemaker.utils.UserSessionManager;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubscriptionActivity extends AppCompatActivity {

    private ActivitySubscriptionBinding binding;
    private ApiService apiService;
    private UserSessionManager session;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySubscriptionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        session = new UserSessionManager(getApplicationContext());

        binding.closeButton.setOnClickListener(v -> finish());

        binding.continueButton.setOnClickListener(v -> subscribe());

        binding.continueLimited.setOnClickListener(v -> finish());

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat c =
                new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        c.hide(WindowInsetsCompat.Type.systemBars());
        c.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
    }

    private void subscribe() {
        Map<String, Boolean> body = new HashMap<>();
        body.put("premium", true);

        apiService.updateSubscription(body).enqueue(new Callback<UserDto>() {
            @Override
            public void onResponse(Call<UserDto> call, Response<UserDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserDto user = response.body();

                    session.savePremium(user.isPremium());
                    session.saveUserAvatar(response.body().getAvatarUrl());

                    Toast.makeText(SubscriptionActivity.this, "You're now a premium user! Enjoy!", Toast.LENGTH_SHORT).show();

                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(SubscriptionActivity.this, "Could not set premium: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserDto> call, Throwable t) {
                Toast.makeText(SubscriptionActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
