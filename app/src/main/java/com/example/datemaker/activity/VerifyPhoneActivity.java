package com.example.datemaker.activity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.datemaker.R;
import com.example.datemaker.databinding.ActivityVerifyPhoneBinding;
import com.example.datemaker.model.ContactInfoDto;
import com.example.datemaker.model.PhoneNumberRequest;
import com.example.datemaker.model.PhoneVerificationRequest;
import com.example.datemaker.retrofit.ApiService;
import com.example.datemaker.retrofit.RetrofitClient;
import com.google.android.material.snackbar.Snackbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyPhoneActivity extends AppCompatActivity{
    public static final String EXTRA_PHONE_NUMBER = "extra_phone_number";

    private ActivityVerifyPhoneBinding binding;
    private ApiService apiService;
    private String phoneNumber;
    private CountDownTimer countDownTimer;
    private boolean canResend = false;
    private static final long RESEND_INTERVAL_MS = 60_000L;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityVerifyPhoneBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        phoneNumber = getIntent().getStringExtra(EXTRA_PHONE_NUMBER);

        setupUi();
        startTimer();
    }

    private void setupUi() {
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        String phoneText = (phoneNumber != null) ? phoneNumber : getString(R.string.unknown_phone);
        binding.phoneText.setText(phoneText);

        binding.verifyButton.setOnClickListener(v -> verifyCode());

        binding.textInputCodeEditText.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                verifyCode();
                return true;
            }

            return false;
        });

        binding.resendText.setOnClickListener(v -> {
            if (canResend) {
                resendCode();
            }
        });

        setResendEnabled(false);
    }

    private void verifyCode() {
        if (binding.textInputCodeEditText.getText() == null) return;

        String code = binding.textInputCodeEditText.getText().toString().trim();

        if (TextUtils.isEmpty(code) || code.length() != 6) {
            binding.textInputCodeLayout.setError(getString(R.string.code_invalid));
            return;
        }

        binding.textInputCodeLayout.setError(null);

        showLoading(true);

        PhoneVerificationRequest request = new PhoneVerificationRequest(code);

        apiService.verifyPhone(request).enqueue(new Callback<ContactInfoDto>() {
            @Override
            public void onResponse(Call<ContactInfoDto> call, Response<ContactInfoDto> response) {
                showLoading(false);

                if (response.isSuccessful()) {
                    Snackbar.make(binding.getRoot(), R.string.phone_verified, Snackbar.LENGTH_LONG).show();
                    finish();
                } else {
                    binding.textInputCodeLayout.setError(getString(R.string.code_invalid_or_expired));
                }
            }

            @Override
            public void onFailure(Call<ContactInfoDto> call, Throwable t) {
                showLoading(false);
                Snackbar.make(binding.getRoot(), R.string.error_network, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void resendCode() {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            Snackbar.make(binding.getRoot(), R.string.unknown_phone, Snackbar.LENGTH_LONG).show();
            return;
        }

        showLoading(true);

        PhoneNumberRequest request = new PhoneNumberRequest(phoneNumber);

        apiService.sendPhoneVerificationCode(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                showLoading(false);

                if (response.isSuccessful()) {
                    Snackbar.make(binding.getRoot(), R.string.code_resent, Snackbar.LENGTH_LONG).show();
                    startTimer();
                } else {
                    Snackbar.make(binding.getRoot(), R.string.error_sending_code, Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showLoading(false);
                Snackbar.make(binding.getRoot(), R.string.error_network, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void startTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        setResendEnabled(false);

        countDownTimer = new CountDownTimer(RESEND_INTERVAL_MS, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000L;
                String text = String.format("00:%02d", seconds);
                binding.timerText.setText(text);
            }

            @Override
            public void onFinish() {
                binding.timerText.setText("");
                setResendEnabled(true);
            }
        };
        countDownTimer.start();
    }

    private void setResendEnabled(boolean enabled) {
        canResend = enabled;
        binding.resendText.setEnabled(enabled);
        binding.resendText.setAlpha(enabled ? 1f : 0.5f);
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.verifyButton.setEnabled(!show);
        binding.textInputCodeEditText.setEnabled(!show);
        binding.resendText.setEnabled(canResend && !show);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}