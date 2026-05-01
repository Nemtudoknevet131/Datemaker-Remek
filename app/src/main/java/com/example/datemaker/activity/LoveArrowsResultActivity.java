package com.example.datemaker.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.datemaker.databinding.ActivityLoveArrowsResultBinding;
import com.example.datemaker.model.LoveArrowsResultRequest;
import com.example.datemaker.model.LoveArrowsResultResponse;
import com.example.datemaker.retrofit.ApiService;
import com.example.datemaker.retrofit.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoveArrowsResultActivity extends AppCompatActivity{
    private ActivityLoveArrowsResultBinding binding;
    private ApiService apiService;

    private int baseScore;
    private int baseCoins;
    private boolean watchedDoubleFromUnity;

    private boolean doubledHere = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoveArrowsResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        baseScore = getIntent().getIntExtra("score", 0);
        baseCoins = getIntent().getIntExtra("coins", baseScore);
        watchedDoubleFromUnity = getIntent().getBooleanExtra("watchedDouble", false);

        binding.tvLastScore.setText("Score: " + baseScore);
        binding.tvCoinsEarned.setText("Coins: " + baseCoins);

        binding.btnDoubleCoins.setOnClickListener(v -> {
            if (doubledHere || watchedDoubleFromUnity) {
                Toast.makeText(this, "Already doubled", Toast.LENGTH_SHORT).show();
                return;
            }

            //TODO making admob call here

            baseCoins *= 2;
            doubledHere = true;
            binding.tvCoinsEarned.setText("Coins: " + baseCoins);
            binding.btnDoubleCoins.setEnabled(false);
            binding.btnDoubleCoins.setText("Coins x2 ✔");
        });

        binding.btnConfirm.setOnClickListener(v -> sendResultToBackend());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());

            int bottom = Math.max(systemBars.bottom, ime.bottom);

            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    bottom
            );

            return insets;
        });
    }

    private void sendResultToBackend() {
        boolean watchedDouble = watchedDoubleFromUnity || doubledHere;
        LoveArrowsResultRequest req = new LoveArrowsResultRequest(baseScore, baseCoins, watchedDouble);

        apiService.submitLoveArrowsResult(req).enqueue(new Callback<LoveArrowsResultResponse>() {
            @Override
            public void onResponse(Call<LoveArrowsResultResponse> call, Response<LoveArrowsResultResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(LoveArrowsResultActivity.this, "Failed to save score", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                LoveArrowsResultResponse resp = response.body();

                String partnerHigh = resp.getPartnerHighScore() != null ? String.valueOf(resp.getPartnerHighScore()) : "-";
                String highs = "You: " + resp.getHighScore() + "\nPartner: " + partnerHigh;
                binding.tvHighScore.setText(highs);

                Integer coupleCoins = resp.getCoupleCoins();
                if (coupleCoins == null) {
                    coupleCoins = resp.getMyCoins();
                }

                binding.tvCoupleCoins.setText("Total together: " + coupleCoins);

                binding.btnConfirm.setText("Close");
                binding.btnConfirm.setOnClickListener(v -> finish());
            }

            @Override
            public void onFailure(Call<LoveArrowsResultResponse> call, Throwable t) {
                Toast.makeText(LoveArrowsResultActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}