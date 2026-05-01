package com.example.datemaker.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.datemaker.databinding.ActivityPremiumStatusBinding;
import com.example.datemaker.model.UserDto;
import com.example.datemaker.retrofit.ApiService;
import com.example.datemaker.retrofit.RetrofitClient;
import com.example.datemaker.utils.UserSessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Collections;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PremiumStatusActivity extends AppCompatActivity {

    private ActivityPremiumStatusBinding binding;
    private ApiService apiService;
    private UserSessionManager session;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPremiumStatusBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        session = new UserSessionManager(getApplicationContext());

        binding.closeButton.setOnClickListener(v -> finish());

        binding.buttonCancelSubscription.setOnClickListener(v -> showCancelDialog());
    }

    private void showCancelDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Cancel subscription")
                .setMessage("Do you really want to cancel the subscription?")
                .setNegativeButton("No", (dialog, which) -> {
                    Toast.makeText(this, "Subscription wasn't cancelled", Toast.LENGTH_SHORT).show();
                })
                .setPositiveButton("Yes", (dialog, which) -> cancelSubscription())
                .show();
    }

    private void cancelSubscription() {
        Map<String, Boolean> body = Collections.singletonMap("premium", false);

        apiService.updateSubscription(body).enqueue(new Callback<UserDto>() {
            @Override
            public void onResponse(Call<UserDto> call, Response<UserDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    session.savePremium(false);
                    Toast.makeText(PremiumStatusActivity.this,
                            "Subscription cancelled",
                            Toast.LENGTH_SHORT
                    ).show();
                    finish();
                } else {
                    Toast.makeText(PremiumStatusActivity.this,
                            "Could not cancel subscription (" + response.code() + ")",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<UserDto> call, Throwable t) {
                Toast.makeText(PremiumStatusActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }
}