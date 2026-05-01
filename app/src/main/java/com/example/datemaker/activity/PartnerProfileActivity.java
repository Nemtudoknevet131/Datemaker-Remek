package com.example.datemaker.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.datemaker.R;
import com.example.datemaker.databinding.ActivityPartnerProfileBinding;
import com.example.datemaker.model.PartnerResponse;
import com.example.datemaker.model.User;
import com.example.datemaker.model.UserDto;
import com.example.datemaker.retrofit.ApiService;
import com.example.datemaker.retrofit.RetrofitClient;
import com.example.datemaker.utils.UserSessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PartnerProfileActivity extends AppCompatActivity {
    private ActivityPartnerProfileBinding binding;
    private ApiService apiService;
    private long partnerId = -1L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPartnerProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        UserSessionManager session = new UserSessionManager(this);
        long userId = session.getUserId();

        apiService = RetrofitClient.getRetrofitInstance(PartnerProfileActivity.this).create(ApiService.class);

        loadPartner(userId);

        binding.btnDelete.setOnClickListener(v -> {

            new AlertDialog.Builder(this)
                    .setTitle("Remove partner")
                    .setMessage("Are you sure you want to remove your partner?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        apiService.removePartner(userId).enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(PartnerProfileActivity.this, "Partner removed successfully", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    Toast.makeText(PartnerProfileActivity.this, "Error removing partner", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Toast.makeText(PartnerProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        binding.btnMessage.setOnClickListener(v -> {
            if (partnerId == -1L) {
                Toast.makeText(this, "Partner not loaded yet", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("partnerId", partnerId);
            intent.putExtra("partnerName", binding.tvName.getText().toString());
            startActivity(intent);
        });

        binding.btnSeeMore.setOnClickListener(v -> {
            if (partnerId == -1) {
                Toast.makeText(this, "Partner not loaded yet", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(PartnerProfileActivity.this, RelationshipDetailsActivity.class);
            intent.putExtra("partnerName", binding.tvName.getText().toString());
            intent.putExtra("partnerId", partnerId);
            startActivity(intent);
        });
    }

    private void loadPartner(long userId) {
        apiService.getPartner(userId).enqueue(new Callback<PartnerResponse>() {
            @Override
            public void onResponse(Call<PartnerResponse> call, Response<PartnerResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(PartnerProfileActivity.this, "Yo single bitch you have no partner", Toast.LENGTH_SHORT).show();
                    binding.tvSubtitle.setText("");
                    return;
                }

                PartnerResponse pr = response.body();

                if (!pr.isHasPartner() || pr.getPartner() == null) {
                    binding.tvName.setText("You have no partner hahahahahahah");
                    binding.tvSubtitle.setText("");
                    return;
                }

                User partner = pr.getPartner();
                partnerId = partner.getId();

                String fullName = buildName(partner);
                binding.tvName.setText(fullName);

                if (partner.getAvatarUrl() != null && !partner.getAvatarUrl().isEmpty()) {
                    Glide.with(PartnerProfileActivity.this)
                            .load(partner.getAvatarUrl())
                            .placeholder(R.drawable.profile_filled)
                            .error(R.drawable.profile_filled)
                            .circleCrop()
                            .into(binding.ivAvatar);
                } else {
                    binding.ivAvatar.setImageResource(R.drawable.profile_filled);
                }

                String togetherSince = pr.getRelationshipStartDate();
                if (togetherSince != null && !togetherSince.isEmpty()) {
                    binding.tvSubtitle.setText("Together since: " + togetherSince);
                } else {
                    binding.tvSubtitle.setText("");
                }
            }

            @Override
            public void onFailure(Call<PartnerResponse> call, Throwable t) {
                Toast.makeText(PartnerProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String buildName(User user) {
        StringBuilder sb = new StringBuilder();
        if (user.getFirstName() != null) {
            sb.append(user.getFirstName());
        }
        if (user.getLastName() != null && !user.getLastName().isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(user.getLastName());
        }

        if (sb.length() == 0 && user.getUsername() != null) {
            sb.append(user.getUsername());
        }

        if (sb.length() == 0) {
            sb.append("Partner");
        }
        return sb.toString();
    }

    private String formatCreatedAt(String raw) {
        if (raw.length() >= 10) {
            return raw.substring(0, 10);
        }
        return raw;
    }
}
