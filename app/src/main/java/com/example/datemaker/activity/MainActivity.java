package com.example.datemaker.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.datemaker.R;
import com.example.datemaker.databinding.ActivityMainBinding;
import com.example.datemaker.fragment.CalendarFragment;
import com.example.datemaker.fragment.HomeFragment;
import com.example.datemaker.fragment.InboxFragment;
import com.example.datemaker.fragment.ProfileFragment;
import com.example.datemaker.game.CardCategoriesActivity;
import com.example.datemaker.game.FlappyActivity;
import com.example.datemaker.model.PartnerResponse;
import com.example.datemaker.retrofit.ApiService;
import com.example.datemaker.retrofit.RetrofitClient;
import com.example.datemaker.utils.UserSessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.messaging.FirebaseMessaging;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private boolean hasPartner = false;

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {

            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
            binding.bottomBar.setSelectedItemId(R.id.navHome);
        }

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat c =
                new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        c.hide(WindowInsetsCompat.Type.systemBars());
        c.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );

        // bottom bar
        binding.bottomBar.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.navDummy) {
                return false;
            } else if (id == R.id.navHome) {
                loadFragment(new HomeFragment());
                return true;
            } else if (id == R.id.navCalendar) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    loadFragment(new CalendarFragment());
                }
                return true;
            } else if (id == R.id.navInbox) {
                loadFragment(new InboxFragment());
                return true;
            } else if (id == R.id.navProfile) {
                loadFragment(new ProfileFragment());
                return true;
            }
            return false;
        });

        // FAB
        binding.fabAdd.setOnClickListener(v -> {
            UserSessionManager session = new UserSessionManager(this);
            long userId = session.getUserId();

            if (userId != -1) {
                checkPartnerStatusAndShowSheet(userId);
            } else {
                showAddActionsSheet(false);
            }
        });

        requestNotificationPermissionIfNeeded();

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        return;
                    }

                    String token = task.getResult();
                    UserSessionManager session = new UserSessionManager(this);
                    long userId = session.getUserId();

                    if (userId != -1) {
                        ApiService apiService = RetrofitClient.getRetrofitInstance(MainActivity.this).create(ApiService.class);
                        apiService.sendFcmToken(token).enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {

                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {

                            }
                        });
                    }
                });

        UserSessionManager session = new UserSessionManager(MainActivity.this);
        long userId = session.getUserId();

        if (userId != -1) {
            loadPartnerAndSaveToSession(userId);
        }

    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            boolean granted = ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED;

            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        UserSessionManager session = new UserSessionManager(this);
        long userId = session.getUserId();

        if (userId != -1) {
            refreshPartnerStatus(userId);

            loadPartnerAndSaveToSession(userId);
        } else {
            hasPartner = false;
        }
    }

    private void loadPartnerAndSaveToSession(long userId) {
        ApiService apiService = RetrofitClient.getRetrofitInstance(MainActivity.this).create(ApiService.class);

        apiService.getPartner(userId).enqueue(new Callback<PartnerResponse>() {
            @Override
            public void onResponse(Call<PartnerResponse> call, Response<PartnerResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PartnerResponse partnerResponse = response.body();
                    UserSessionManager session = new UserSessionManager(MainActivity.this);

                    if (partnerResponse.isHasPartner() && partnerResponse.getPartner() != null) {
                        long partnerId = partnerResponse.getPartner().getId();
                        session.setPartnerId(partnerId);
                        hasPartner = true;
                    } else {
                        session.setPartnerId(-1L);
                        hasPartner = false;
                    }
                } else {
                    UserSessionManager session = new UserSessionManager(MainActivity.this);
                    session.setPartnerId(-1L);
                    hasPartner = false;
                }
            }

            @Override
            public void onFailure(Call<PartnerResponse> call, Throwable t) {
                UserSessionManager session = new UserSessionManager(MainActivity.this);
                session.setPartnerId(-1L);
                hasPartner = false;
            }
        });
    }

    private void checkPartnerStatusAndShowSheet(long userId) {
        ApiService apiService = RetrofitClient.getRetrofitInstance(MainActivity.this).create(ApiService.class);
        apiService.isPartnered(userId).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                boolean partnered = response.isSuccessful()
                        && response.body() != null
                        && response.body();
                hasPartner = partnered;
                showAddActionsSheet(partnered);
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                showAddActionsSheet(hasPartner);
            }
        });
    }

    private void refreshPartnerStatus(long userId) {
        ApiService apiService = RetrofitClient.getRetrofitInstance(MainActivity.this).create(ApiService.class);
        apiService.isPartnered(userId).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                hasPartner = response.isSuccessful()
                        && response.body() != null
                        && response.body();
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
            }
        });
    }

    private void showAddActionsSheet(boolean hasPartner) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheet = LayoutInflater.from(this)
                .inflate(R.layout.sheet_add_actions, binding.getRoot(), false);
        dialog.setContentView(sheet);

        View btnAddDate = sheet.findViewById(R.id.btnAddDate);
        View btnGameCenter = sheet.findViewById(R.id.btnGameCenter);
        View btnAddCard = sheet.findViewById(R.id.btnAddCard);
        View btnAddPartner = sheet.findViewById(R.id.btnAddPartner);

        if (hasPartner) {
            btnAddDate.setVisibility(View.VISIBLE);
            btnGameCenter.setVisibility(View.VISIBLE);
            btnAddCard.setVisibility(View.VISIBLE);
            btnAddPartner.setVisibility(View.GONE);
        } else {
            btnAddDate.setVisibility(View.GONE);
            btnGameCenter.setVisibility(View.GONE);
            btnAddCard.setVisibility(View.GONE);
            btnAddPartner.setVisibility(View.VISIBLE);
        }

        btnAddDate.setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(this, PresetDateActivity.class));
        });

        btnGameCenter.setOnClickListener(v -> {
            dialog.dismiss();

            startActivity(new Intent(this, GameCenterActivity.class));
        });

        btnAddCard.setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(this, CardCategoriesActivity.class));
        });

        btnAddPartner.setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(this, AddPartnerActivity.class));
        });

        dialog.show();
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragmentContainer, fragment);
        ft.commit();
    }
}
