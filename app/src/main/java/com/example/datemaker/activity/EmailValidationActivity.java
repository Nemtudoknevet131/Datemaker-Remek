package com.example.datemaker.activity;

import static android.content.ContentValues.TAG;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.datemaker.R;
import com.example.datemaker.model.ResendCodeRequest;
import com.example.datemaker.model.VerifyEmailRequest;
import com.example.datemaker.retrofit.ApiService;
import com.example.datemaker.retrofit.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import com.airbnb.lottie.LottieAnimationView;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmailValidationActivity extends AppCompatActivity {

    private TextInputEditText codeInput;
    private MaterialButton verifyButton;
    private MaterialButton resendButton;
    private int fakeCode;
    private TextView infoText;
    private ApiService apiService;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_email_validation);

        codeInput = findViewById(R.id.textInputEditTextCode);
        verifyButton = findViewById(R.id.verifyButton);
        resendButton = findViewById(R.id.resendButton);
        infoText = findViewById(R.id.infoText);

        apiService = RetrofitClient.getRetrofitInstance(EmailValidationActivity.this).create(ApiService.class);

        email = getIntent().getStringExtra("email");
        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "No email provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        boolean emailSent = getIntent().getBooleanExtra("email_sent", false);
        if (emailSent) {
            Toast.makeText(this, "Email verification sent", Toast.LENGTH_SHORT).show();
        }

        verifyButton.setOnClickListener(v -> verifyCode());
        resendButton.setOnClickListener(v -> resendCode());
    }

    private void verifyCode() {

        String code = codeInput.getText() != null ? codeInput.getText().toString().trim() : "";

        if (code.isEmpty()) {
            Toast.makeText(this, "Please enter a 6 digit number", Toast.LENGTH_SHORT).show();
            return;
        }

        VerifyEmailRequest request = new VerifyEmailRequest(email, code);

        apiService.verifyEmail(request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    showSuccess();
                } else {
                    Toast.makeText(EmailValidationActivity.this, "Invalid or expired code.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(EmailValidationActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "verifyCode error", t);
            }
        });
    }

    private void resendCode() {
        resendButton.setEnabled(false);
        resendButton.setText("Sending...");

        ResendCodeRequest request = new ResendCodeRequest(email);

        apiService.resendCode(request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                resendButton.setEnabled(true);
                resendButton.setText("Resend Code");

                if (response.isSuccessful()) {
                    Toast.makeText(EmailValidationActivity.this, "New verification code sent.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(EmailValidationActivity.this, "Please wait before requesting a new code", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                resendButton.setEnabled(true);
                resendButton.setText("Resend Code");
                Toast.makeText(EmailValidationActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "resendCode error", t);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void showSuccess() {

        codeInput.setVisibility(View.GONE);
        verifyButton.setEnabled(false);
        verifyButton.setVisibility(View.GONE);
        resendButton.setVisibility(View.GONE);
        resendButton.setEnabled(false);
        infoText.setVisibility(View.GONE);

        try {
            FrameLayout fl = new FrameLayout(this);
            fl.setBackgroundColor(Color.parseColor("#AA000000"));

            TextView text = new TextView(this);
            text.setText("Registration Successful!");
            text.setTextSize(26f);
            text.setTextColor(Color.WHITE);
            text.setGravity(Gravity.CENTER);

            fl.addView(text);

            addContentView(
                    fl,
                    new FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                    )
            );

            LottieAnimationView heartLottie = findViewById(R.id.heartLottie);
            heartLottie.setVisibility(View.VISIBLE);
            heartLottie.playAnimation();

            heartLottie.addAnimatorListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    heartLottie.setVisibility(View.GONE);
                }
            });

            new Handler().postDelayed(() -> {
                Intent intent = new Intent(EmailValidationActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }, 2500);
        } catch (Exception e) {
            Log.e(TAG, "verifyCode: unexpected error", e);
            Toast.makeText(this, "Unexpected error, check logcat.", Toast.LENGTH_SHORT).show();
        }
    }
}
