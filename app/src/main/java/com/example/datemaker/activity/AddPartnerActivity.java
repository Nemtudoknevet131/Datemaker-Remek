package com.example.datemaker.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.datemaker.R;
import com.example.datemaker.model.PartnerSearchResponse;
import com.example.datemaker.model.User;
import com.example.datemaker.model.UserDto;
import com.example.datemaker.retrofit.ApiService;
import com.example.datemaker.retrofit.RetrofitClient;
import com.example.datemaker.utils.UserSessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddPartnerActivity extends AppCompatActivity {

    private TextInputEditText searchBar;
    private MaterialButton searchButton, addPartnerConfirmButton;
    private CardView resultCard;
    private TextView userNameText;
    private ImageView profileImage;

    private ApiService apiService;

    private UserDto lastFoundUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_partner);

        searchBar = findViewById(R.id.searchBar);
        searchButton = findViewById(R.id.searchButton);
        addPartnerConfirmButton = findViewById(R.id.addPartnerConfirmButton);
        resultCard = findViewById(R.id.resultCard);
        userNameText = findViewById(R.id.userNameText);
        profileImage = findViewById(R.id.profileImage);

        resultCard.setVisibility(View.GONE);

        apiService = RetrofitClient.getRetrofitInstance(AddPartnerActivity.this).create(ApiService.class);

        searchButton.setOnClickListener(v -> searchUser());
    }

    private void searchUser() {
        if (searchBar == null || searchBar.getText() == null) {
            return;
        }

        String query = searchBar.getText().toString().trim();

        if (query.isEmpty()) {
            Toast.makeText(AddPartnerActivity.this, "Please enter an email address or a username.", Toast.LENGTH_SHORT).show();
            return;
        }

        UserSessionManager session = new UserSessionManager(this);
        long userId = session.getUserId();

        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.searchPartner(userId, query).enqueue(new Callback<PartnerSearchResponse>() {
            @Override
            public void onResponse(Call<PartnerSearchResponse> call, Response<PartnerSearchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PartnerSearchResponse resp = response.body();
                    lastFoundUser = resp.getUser();

                    String first = lastFoundUser.getFirstName();
                    String last = lastFoundUser.getLastName();

                    String displayName;

                    if (first != null && !first.isBlank() && last != null && !last.isBlank()) {
                        displayName = first + " " + last;
                    } else if (first != null && !first.isBlank()) {
                        displayName = first;
                    } else if (lastFoundUser.getUsername() != null && !lastFoundUser.getUsername().isBlank()) {
                        displayName = lastFoundUser.getUsername();
                    } else {
                        displayName = lastFoundUser.getEmail();
                    }

                    Glide.with(AddPartnerActivity.this)
                            .load(lastFoundUser.getAvatarUrl())
                            .placeholder(R.drawable.profile_filled)
                            .error(R.drawable.profile_filled)
                            .circleCrop()
                            .into(profileImage);

                    userNameText.setText(displayName);
                    userNameText.setTextColor(ContextCompat.getColor(AddPartnerActivity.this, R.color.white));
                    resultCard.setVisibility(View.VISIBLE);

                    if (resp.isAlreadyPartnered()) {
                        addPartnerConfirmButton.setEnabled(false);
                        addPartnerConfirmButton.setText("Partnered");
                        addPartnerConfirmButton.setBackgroundColor(ContextCompat.getColor(AddPartnerActivity.this, R.color.gray));
                        addPartnerConfirmButton.setOnClickListener(null);
                    } else if (resp.isPendingRequestFromMe()) {
                        addPartnerConfirmButton.setEnabled(false);
                        addPartnerConfirmButton.setText("Request sent");
                        addPartnerConfirmButton.setBackgroundColor(ContextCompat.getColor(AddPartnerActivity.this, R.color.gray));
                        addPartnerConfirmButton.setOnClickListener(null);
                    } else {
                        addPartnerConfirmButton.setEnabled(true);
                        addPartnerConfirmButton.setText("Add");
                        addPartnerConfirmButton.setBackgroundColor(ContextCompat.getColor(AddPartnerActivity.this, R.color.purple_500));
                        addPartnerConfirmButton.setOnClickListener(v -> addPartner(lastFoundUser.getEmail()));
                    }
                } else {
                    Toast.makeText(AddPartnerActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                    resultCard.setVisibility(View.GONE);
                    addPartnerConfirmButton.setEnabled(false);
                }
            }

            @Override
            public void onFailure(Call<PartnerSearchResponse> call, Throwable t) {
                Toast.makeText(AddPartnerActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                resultCard.setVisibility(View.GONE);
                addPartnerConfirmButton.setEnabled(false);
            }
        });

    }

    private void addPartner(String partnerEmail) {
        UserSessionManager session = new UserSessionManager(this);
        long userId = session.getUserId();

        if (userId == -1) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.sendPartnerRequest(userId, partnerEmail).enqueue(new Callback<Void>() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddPartnerActivity.this, "Partner request sent.", Toast.LENGTH_SHORT).show();

                    addPartnerConfirmButton.setText("Request sent");
                    addPartnerConfirmButton.setEnabled(false);
                    addPartnerConfirmButton.setBackgroundColor(
                            ContextCompat.getColor(AddPartnerActivity.this, R.color.gray)
                    );
                } else {
                    String msg = "Failed to send request";
                    if (response.code() == 409) {
                        msg = "User already has a pending partner request";
                        addPartnerConfirmButton.setText("Request sent");
                        addPartnerConfirmButton.setEnabled(false);
                        addPartnerConfirmButton.setBackgroundColor(
                                ContextCompat.getColor(AddPartnerActivity.this, R.color.gray)
                        );
                    }

                    Toast.makeText(AddPartnerActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(AddPartnerActivity.this, "Error sending request: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
