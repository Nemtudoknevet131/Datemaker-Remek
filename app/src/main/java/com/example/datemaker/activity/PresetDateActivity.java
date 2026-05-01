package com.example.datemaker.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.datemaker.R;
import com.example.datemaker.model.DateEventDto;
import com.example.datemaker.model.DateEventRequest;
import com.example.datemaker.retrofit.ApiService;
import com.example.datemaker.retrofit.RetrofitClient;
import com.example.datemaker.utils.UserSessionManager;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PresetDateActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etDescription, etLocation;
    private TextView tvTimeLabel;
    private TextView tvDateLabel;
    private ImageView ivAskAiCrown;
    private String selectedTime = "";
    private String selectedDateIso = "";
    private UserSessionManager session;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preset_date);

        session = new UserSessionManager(PresetDateActivity.this);

        apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etLocation = findViewById(R.id.etLocation);
        tvTimeLabel = findViewById(R.id.tvTimeLabel);
        tvDateLabel = findViewById(R.id.tvDateLabel);
        Button btnPickTime = findViewById(R.id.btnPickTime);
        Button btnPickDate = findViewById(R.id.btnPickDate);
        Button btnSaveDate = findViewById(R.id.btnSaveDate);
        Button btnAskAi = findViewById(R.id.btnAskAi);
        ivAskAiCrown = findViewById(R.id.ivAskAiCrown);

        btnPickDate.setOnClickListener(v -> showDatePicker());
        btnPickTime.setOnClickListener(v -> showTimePicker());

        btnSaveDate.setOnClickListener(v -> saveDate());

        btnAskAi.setOnClickListener(v -> {
            if (!session.isPremium()) {
                startActivity(new Intent(this, SubscriptionActivity.class));
            } else {
                startActivity(new Intent(this, AiDateChatActivity.class));
            }
        });

        updatePremiumUi();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.presetDateRoot), (v, insets) -> {
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

    @Override
    protected void onResume() {
        super.onResume();

        updatePremiumUi();
    }

    private void updatePremiumUi() {
        if (session.isPremium()) {
            ivAskAiCrown.setVisibility(View.GONE);
        } else {
            ivAskAiCrown.setVisibility(View.VISIBLE);
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, y, m, d) -> {
                    selectedDateIso = String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m + 1, d);
                    String displayDate = String.format(java.util.Locale.getDefault(), "%04d.%02d.%02d", y, m + 1, d);
                    tvDateLabel.setText("Date: " + displayDate);
                },
                year,
                month,
                day
        );

        dialog.show();

        dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(this, R.color.white));

        dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(this, R.color.white));
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minuteOfHour) -> {
                    selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour);
                    tvTimeLabel.setText("Time: " + selectedTime);
                }, hour, minute, true);
        timePickerDialog.show();
    }

    private void saveDate() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String location = etLocation.getText().toString().trim();

        if (title.isEmpty()) {
            etTitle.setError("Title is required");
            return;
        }

        if (selectedDateIso.isEmpty()) {
            Toast.makeText(this, "Yeah dude just pick a date nga", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedTime.isEmpty()) {
            Toast.makeText(this, "Just pick a fucking time moron", Toast.LENGTH_SHORT).show();
            return;
        }

        DateEventRequest request = new DateEventRequest(
                selectedDateIso,
                title,
                description,
                "#9C27B0",
                selectedTime
        );

        apiService.createDateEvent(request).enqueue(new Callback<DateEventDto>() {
            @Override
            public void onResponse(Call<DateEventDto> call, Response<DateEventDto> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(PresetDateActivity.this, "Date saved to calendar", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(PresetDateActivity.this, "Failed to save date", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DateEventDto> call, Throwable t) {
                Toast.makeText(PresetDateActivity.this, "Something gone wrong: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
