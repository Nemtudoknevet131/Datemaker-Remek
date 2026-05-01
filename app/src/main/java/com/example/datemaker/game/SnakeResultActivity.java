package com.example.datemaker.game;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.datemaker.activity.GameCenterActivity;
import com.example.datemaker.databinding.ActivitySnakeResultBinding;
import com.example.datemaker.model.SnakeResultRequest;
import com.example.datemaker.model.SnakeResultResponse;
import com.example.datemaker.retrofit.ApiService;
import com.example.datemaker.retrofit.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SnakeResultActivity extends AppCompatActivity {
    private ActivitySnakeResultBinding binding;
    private ApiService apiService;

    private long coinsEarned;
    private long userBestScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySnakeResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        coinsEarned = getIntent().getLongExtra("coinsEarned", 0L);
        userBestScore = getIntent().getLongExtra("userBestScore", 0L);

        apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        binding.tvCoinsEarned.setText("Coins: " + coinsEarned);

        sendResultToServer();

        binding.btnResultDone.setOnClickListener(v -> {
            Intent intent = new Intent(SnakeResultActivity.this, GameCenterActivity.class);
            startActivity(intent);
            finish();
        });

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

    private void sendResultToServer() {
        SnakeResultRequest req = new SnakeResultRequest(
                (int) userBestScore,
                (int) coinsEarned
        );

        apiService.submitSnakeResult(req).enqueue(new Callback<SnakeResultResponse>() {
            @Override
            public void onResponse(Call<SnakeResultResponse> call, Response<SnakeResultResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(SnakeResultActivity.this, "Failed to load snake stats", Toast.LENGTH_SHORT).show();
                    return;
                }

                SnakeResultResponse stats = response.body();

                Integer partnerHigh = stats.getPartnerHighScore();
                String partnerText = (partnerHigh != null) ? "Partners high score: " + partnerHigh : "Partners high score: -";
                binding.tvPartnerHighScore.setText(partnerText);

                binding.tvYourHighScore.setText("My high score: " + stats.getMyHighScore());
            }

            @Override
            public void onFailure(Call<SnakeResultResponse> call, Throwable t) {
                Toast.makeText(SnakeResultActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}