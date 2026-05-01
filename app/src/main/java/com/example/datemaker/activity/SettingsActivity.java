package com.example.datemaker.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.datemaker.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setupButtons();
    }

    private void setupButtons() {
        Button btnEditProfile = findViewById(R.id.btnEditProfile);
        Button btnPartnerInfo = findViewById(R.id.btnPartnerInfo);
        Button btnLogout = findViewById(R.id.btnLogout);
        SwitchMaterial switchNotifications = findViewById(R.id.switchNotifications);
        TextView txtVersion = findViewById(R.id.txtVersion);

        btnEditProfile.setOnClickListener(v -> 
            Toast.makeText(this, "Navigate to Edit Profile", Toast.LENGTH_SHORT).show()
        );

        btnPartnerInfo.setOnClickListener(v -> 
            Toast.makeText(this, "Navigate to Partner Settings", Toast.LENGTH_SHORT).show()
        );

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String status = isChecked ? "Enabled" : "Disabled";
            Toast.makeText(this, "Notifications " + status, Toast.LENGTH_SHORT).show();
        });

        btnLogout.setOnClickListener(v -> {
            // Clear session and go to Login
            Toast.makeText(this, "Logging Out...", Toast.LENGTH_SHORT).show();
            finishAffinity(); 
        });

        txtVersion.setText("App Version 1.0.0");
    }
}
