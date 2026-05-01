package com.example.datemaker.activity;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.datemaker.R;
import com.example.datemaker.databinding.ActivityResetPasswordBinding;
import com.example.datemaker.model.PasswordResetRequest;
import com.example.datemaker.retrofit.ApiService;
import com.example.datemaker.retrofit.RetrofitClient;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordActivity extends AppCompatActivity {
    private ActivityResetPasswordBinding binding;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityResetPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = RetrofitClient.getRetrofitInstance(ResetPasswordActivity.this).create(ApiService.class);

        setupBackClick();
        setupConfirmClick();

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

    private void setupBackClick() {
        binding.backIcon.setOnClickListener(v -> {
            Toast.makeText(ResetPasswordActivity.this, "Resetting password cancelled", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void setupConfirmClick() {
        binding.resetConfirmButton.setOnClickListener(v -> {
            if (binding.textInputEditTextResetEmail.getText() == null) {
                binding.textInputLayoutResetEmail.setError("Email is required");
                return;
            }

            String email = binding.textInputEditTextResetEmail.getText().toString().trim();

            if (email.isEmpty()) {
                binding.textInputLayoutResetEmail.setError("Email is required");
                return;
            } else {
                binding.textInputLayoutResetEmail.setError(null);
            }

            binding.resetConfirmButton.setEnabled(false);
            binding.resetConfirmButton.setText("Sending...");

            PasswordResetRequest req = new PasswordResetRequest(email);

            apiService.requestPasswordReset(req).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    binding.resetConfirmButton.setEnabled(true);
                    binding.resetConfirmButton.setText(getString(R.string.send_reset_email));

                    if (response.isSuccessful()) {
                        Toast.makeText(ResetPasswordActivity.this, "Reset password email sent", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        String errorMsg = "";
                        try {
                            if (response.errorBody() != null) {
                                errorMsg = response.errorBody().string();
                            }
                        } catch (Exception ignored) { }

                        if (response.code() == 404 || errorMsg.contains("Email doesn't exist")) {
                            binding.textInputLayoutResetEmail.setError("Email doesn't exist");
                        } else {
                            Toast.makeText(ResetPasswordActivity.this, "Failed to send reset email", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    binding.resetConfirmButton.setEnabled(true);
                    binding.resetConfirmButton.setText(getString(R.string.send_reset_email));

                    Toast.makeText(ResetPasswordActivity.this, "Server not reachable", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();

            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);

                if (!outRect.contains((int) e.getRawX(), (int) e.getRawY())) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                    if (imm != null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            }
        }

        return super.dispatchTouchEvent(e);
    }
}
