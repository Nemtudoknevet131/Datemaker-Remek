package com.example.datemaker.activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.datemaker.R;
import com.example.datemaker.databinding.ActivityRelationshipDetailsBinding;
import com.example.datemaker.model.LoveArrowsResultResponse;
import com.example.datemaker.model.PartnerResponse;
import com.example.datemaker.model.SnakeResultResponse;
import com.example.datemaker.retrofit.ApiService;
import com.example.datemaker.retrofit.RetrofitClient;
import com.example.datemaker.utils.UserSessionManager;

import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RelationshipDetailsActivity extends AppCompatActivity {

    private ActivityRelationshipDetailsBinding binding;
    private ApiService apiService;
    private long userId;
    private long partnerId;

    // Display names for the combined label: "<userDisplayName> + <partnerDisplayName>"
    private String userDisplayName = "You";
    private String partnerDisplayName = "Partner";
    private int coupleCoins = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRelationshipDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        UserSessionManager session = new UserSessionManager(this);
        userId = session.getUserId();

        partnerId = getIntent().getLongExtra("partnerId", -1L);
        String partnerName = getIntent().getStringExtra("partnerName");

        String myAvatarUrl = session.getAvatarUrl();

        Glide.with(RelationshipDetailsActivity.this)
                .load(myAvatarUrl)
                .placeholder(R.drawable.profile_filled)
                .error(R.drawable.profile_filled)
                .circleCrop()
                .into(binding.ivMeAvatar);

        apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        loadGameStats();

        partnerDisplayName = partnerName != null && !partnerName.trim().isEmpty()
                ? partnerName
                : "Partner";

        binding.tvCombinedNames.setText(userDisplayName + " + " + partnerDisplayName);

        loadRelationshipInfo();

        binding.tvTogetherSince.setOnClickListener(v -> showDatePicker());

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

    private void loadRelationshipInfo() {
        apiService.getPartner(userId).enqueue(new Callback<PartnerResponse>() {
            @Override
            public void onResponse(Call<PartnerResponse> call, Response<PartnerResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(RelationshipDetailsActivity.this, "Failed to load partner", Toast.LENGTH_SHORT).show();
                    return;
                }

                PartnerResponse pr = response.body();

                if (!pr.isHasPartner() || pr.getPartner() == null) {
                    binding.tvTogetherSince.setText("No partner yet.");
                    // Keep the combined names as the default "You + Partner"
                    return;
                }

                String date = pr.getRelationshipStartDate();
                if (date != null && !date.isEmpty()) {
                    binding.tvTogetherSince.setText("Together since: " + date);
                } else {
                    binding.tvTogetherSince.setText("Tap here to set your special date");
                }

                String first = pr.getPartner().getFirstName();
                String last = pr.getPartner().getLastName();
                String username = pr.getPartner().getUsername();

                String displayName;

                if (first != null && !first.trim().isEmpty()) {
                    displayName = first + (last != null && !last.trim().isEmpty() ? " " + last : "");
                } else if (last != null && !last.trim().isEmpty()) {
                    displayName = last;
                } else if (username != null && !username.trim().isEmpty()) {
                    displayName = username;
                } else {
                    displayName = "Partner";
                }

                String partnerAvatarUrl = pr.getPartner().getAvatarUrl();

                Glide.with(RelationshipDetailsActivity.this)
                        .load(partnerAvatarUrl)
                        .placeholder(R.drawable.profile_filled)
                        .error(R.drawable.profile_filled)
                        .circleCrop()
                        .into(binding.ivPartnerAvatar);

                // Update partner display name and refresh combined label
                partnerDisplayName = displayName;
                binding.tvCombinedNames.setText(userDisplayName + " + " + partnerDisplayName);
            }

            @Override
            public void onFailure(Call<PartnerResponse> call, Throwable t) {
                Toast.makeText(RelationshipDetailsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDatePicker() {
        final Calendar cal = Calendar.getInstance();

        DatePickerDialog picker = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String dateStr = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);

                    apiService.setRelationshipDate(userId, dateStr).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                binding.tvTogetherSince.setText("Together since " + dateStr);
                                Toast.makeText(RelationshipDetailsActivity.this, "Date saved", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(RelationshipDetailsActivity.this, "Failed to save date", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(RelationshipDetailsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );

        picker.show();
    }

    private void loadGameStats() {
        final int[] totalCoins = {0};

        apiService.getLoveArrowsSummary().enqueue(new Callback<LoveArrowsResultResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoveArrowsResultResponse> call,
                                   @NonNull Response<LoveArrowsResultResponse> response) {
                if (!response.isSuccessful() || response.body() == null) return;

                LoveArrowsResultResponse stats = response.body();

                Integer couple = stats.getCoupleCoins();
                if (couple == null) {
                    couple = stats.getMyCoins();
                }

                totalCoins[0] += couple;
                binding.tvCoinsTotal.setText(String.valueOf(totalCoins[0]));
            }

            @Override
            public void onFailure(Call<LoveArrowsResultResponse> call, Throwable t) { }
        });

        apiService.getSnakeSummary().enqueue(new Callback<SnakeResultResponse>() {
            @Override
            public void onResponse(Call<SnakeResultResponse> call,
                                   Response<SnakeResultResponse> response) {
                if (!response.isSuccessful() || response.body() == null) return;

                SnakeResultResponse stats = response.body();

                Integer couple = stats.getCoupleCoins();
                if (couple == null) {
                    couple = stats.getMyCoins();
                }

                totalCoins[0] += couple;
                binding.tvCoinsTotal.setText(String.valueOf(totalCoins[0]));
            }

            @Override
            public void onFailure(Call<SnakeResultResponse> call, Throwable t) { }
        });
    }
}