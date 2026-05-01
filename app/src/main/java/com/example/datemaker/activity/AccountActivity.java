package com.example.datemaker.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.datemaker.R;
import com.example.datemaker.databinding.ActivityAccountBinding;
import com.example.datemaker.model.ContactInfoDto;
import com.example.datemaker.model.PhoneNumberRequest;
import com.example.datemaker.model.ProfileDto;
import com.example.datemaker.retrofit.ApiService;
import com.example.datemaker.retrofit.RetrofitClient;
import com.google.android.material.snackbar.Snackbar;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountActivity extends AppCompatActivity {

    private ActivityAccountBinding binding;
    private ApiService apiService;

    private ContactInfoDto currentContact;
    private ProfileDto currentProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        setupListeners();

        // Initial load
        loadProfile();
        loadContact();

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

    @Override
    protected void onResume() {
        super.onResume();
        loadContact();
    }

    // region Setup

    private void setupListeners() {
        // FIRST NAME
        View.OnClickListener openFirstNameEditor = v -> {
            if (currentProfile == null) return;
            showFirstNameEditor();
        };
        binding.cardFirstName.setOnClickListener(openFirstNameEditor);
        binding.iconFirstNameEdit.setOnClickListener(openFirstNameEditor);

        binding.firstNameOkButton.setOnClickListener(v -> saveFirstName());
        binding.firstNameCancelButton.setOnClickListener(v -> hideFirstNameEditor());

        binding.inputFirstNameEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                saveFirstName();
                return true;
            }
            return false;
        });

        // LAST NAME
        View.OnClickListener openLastNameEditor = v -> {
            if (currentProfile == null) return;
            showLastNameEditor();
        };
        binding.cardLastName.setOnClickListener(openLastNameEditor);
        binding.iconLastNameEdit.setOnClickListener(openLastNameEditor);

        binding.lastNameOkButton.setOnClickListener(v -> saveLastName());
        binding.lastNameCancelButton.setOnClickListener(v -> hideLastNameEditor());

        binding.inputLastNameEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                saveLastName();
                return true;
            }
            return false;
        });

        // EMAIL
        View.OnClickListener openEmailEditor = v -> {
            if (currentContact == null) return;
            showEmailEditor();
        };
        binding.cardEmail.setOnClickListener(openEmailEditor);
        binding.iconEmailEdit.setOnClickListener(openEmailEditor);

        binding.emailOkButton.setOnClickListener(v -> saveEmail());
        binding.emailCancelButton.setOnClickListener(v -> hideEmailEditor());

        binding.inputEmailEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                saveEmail();
                return true;
            }
            return false;
        });

        // PHONE
        View.OnClickListener openPhoneEditor = v -> {
            if (currentContact == null) return;
            showPhoneEditor();
        };
        binding.cardPhone.setOnClickListener(openPhoneEditor);
        binding.iconPhoneEdit.setOnClickListener(openPhoneEditor);

        binding.phoneOkButton.setOnClickListener(v -> sendVerificationCode());
        binding.phoneCancelButton.setOnClickListener(v -> hidePhoneEditor());

        binding.inputPhoneEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                sendVerificationCode();
                return true;
            }
            return false;
        });
    }

    // endregion

    // region Load data

    private void loadProfile() {
        showLoading(true);
        apiService.getMe().enqueue(new Callback<ProfileDto>() {
            @Override
            public void onResponse(@NonNull Call<ProfileDto> call,
                                   @NonNull Response<ProfileDto> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    currentProfile = response.body();
                    renderProfile(currentProfile);
                } else {
                    showSnack(getString(R.string.error_loading_contact));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProfileDto> call,
                                  @NonNull Throwable t) {
                showLoading(false);
                showSnack(getString(R.string.error_network));
            }
        });
    }

    private void loadContact() {
        showLoading(true);
        apiService.getMyContact().enqueue(new Callback<ContactInfoDto>() {
            @Override
            public void onResponse(@NonNull Call<ContactInfoDto> call,
                                   @NonNull Response<ContactInfoDto> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    currentContact = response.body();
                } else {
                    currentContact = new ContactInfoDto(null, null);
                    showSnack(getString(R.string.error_loading_contact));
                }
                renderContact(currentContact);
            }

            @Override
            public void onFailure(@NonNull Call<ContactInfoDto> call,
                                  @NonNull Throwable t) {
                showLoading(false);
                currentContact = new ContactInfoDto(null, null);
                renderContact(currentContact);
                showSnack(getString(R.string.error_network));
            }
        });
    }

    private void renderProfile(ProfileDto profile) {
        String first = TextUtils.isEmpty(profile.getFirstName())
                ? getString(R.string.value_not_set)
                : profile.getFirstName();
        String last = TextUtils.isEmpty(profile.getLastName())
                ? getString(R.string.value_not_set)
                : profile.getLastName();

        binding.textFirstNameValue.setText(first);
        binding.textLastNameValue.setText(last);
    }

    private void renderContact(ContactInfoDto contact) {
        String email = TextUtils.isEmpty(contact.getEmail())
                ? getString(R.string.email_not_set)
                : contact.getEmail();

        String phone = TextUtils.isEmpty(contact.getPhoneNumber())
                ? getString(R.string.phone_not_set)
                : contact.getPhoneNumber();

        binding.textEmailValue.setText(email);
        binding.textPhoneValue.setText(phone);
    }

    // endregion

    // region First name

    private void showFirstNameEditor() {
        String current = currentProfile != null ? currentProfile.getFirstName() : "";
        binding.inputFirstNameEditText.setText(current);
        binding.inputFirstNameLayout.setError(null);

        binding.firstNameDisplayGroup.setVisibility(View.GONE);
        binding.firstNameEditGroup.setVisibility(View.VISIBLE);

        binding.inputFirstNameEditText.requestFocus();
        showKeyboard(binding.inputFirstNameEditText);
    }

    private void hideFirstNameEditor() {
        hideKeyboard(binding.inputFirstNameEditText);
        binding.firstNameEditGroup.setVisibility(View.GONE);
        binding.firstNameDisplayGroup.setVisibility(View.VISIBLE);
    }

    private void saveFirstName() {
        if (binding.inputFirstNameEditText.getText() == null) return;

        String newFirst = binding.inputFirstNameEditText.getText().toString().trim();
        if (TextUtils.isEmpty(newFirst)) {
            binding.inputFirstNameLayout.setError(getString(R.string.first_name_required));
            return;
        }

        binding.inputFirstNameLayout.setError(null);

        Map<String, String> body = new HashMap<>();
        body.put("firstName", newFirst);

        showLoading(true);
        hideKeyboard(binding.inputFirstNameEditText);

        apiService.updateMe(body).enqueue(new Callback<ProfileDto>() {
            @Override
            public void onResponse(@NonNull Call<ProfileDto> call,
                                   @NonNull Response<ProfileDto> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    currentProfile = response.body();
                    renderProfile(currentProfile);
                    hideFirstNameEditor();
                    showSnack(getString(R.string.first_name_updated));
                } else {
                    showSnack(getString(R.string.error_updating_name));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProfileDto> call,
                                  @NonNull Throwable t) {
                showLoading(false);
                showSnack(getString(R.string.error_network));
            }
        });
    }

    // endregion

    // region Last name

    private void showLastNameEditor() {
        String current = currentProfile != null ? currentProfile.getLastName() : "";
        binding.inputLastNameEditText.setText(current);
        binding.inputLastNameLayout.setError(null);

        binding.lastNameDisplayGroup.setVisibility(View.GONE);
        binding.lastNameEditGroup.setVisibility(View.VISIBLE);

        binding.inputLastNameEditText.requestFocus();
        showKeyboard(binding.inputLastNameEditText);
    }

    private void hideLastNameEditor() {
        hideKeyboard(binding.inputLastNameEditText);
        binding.lastNameEditGroup.setVisibility(View.GONE);
        binding.lastNameDisplayGroup.setVisibility(View.VISIBLE);
    }

    private void saveLastName() {
        if (binding.inputLastNameEditText.getText() == null) return;

        String newLast = binding.inputLastNameEditText.getText().toString().trim();
        if (TextUtils.isEmpty(newLast)) {
            binding.inputLastNameLayout.setError(getString(R.string.last_name_required));
            return;
        }

        binding.inputLastNameLayout.setError(null);

        Map<String, String> body = new HashMap<>();
        body.put("lastName", newLast);

        showLoading(true);
        hideKeyboard(binding.inputLastNameEditText);

        apiService.updateMe(body).enqueue(new Callback<ProfileDto>() {
            @Override
            public void onResponse(@NonNull Call<ProfileDto> call,
                                   @NonNull Response<ProfileDto> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    currentProfile = response.body();
                    renderProfile(currentProfile);
                    hideLastNameEditor();
                    showSnack(getString(R.string.last_name_updated));
                } else {
                    showSnack(getString(R.string.error_updating_name));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProfileDto> call,
                                  @NonNull Throwable t) {
                showLoading(false);
                showSnack(getString(R.string.error_network));
            }
        });
    }

    // endregion

    // region Email

    private void showEmailEditor() {
        String currentEmail = currentContact != null ? currentContact.getEmail() : "";
        binding.inputEmailEditText.setText(currentEmail);
        binding.inputEmailLayout.setError(null);

        binding.emailDisplayGroup.setVisibility(View.GONE);
        binding.emailEditGroup.setVisibility(View.VISIBLE);

        binding.inputEmailEditText.requestFocus();
        showKeyboard(binding.inputEmailEditText);
    }

    private void hideEmailEditor() {
        hideKeyboard(binding.inputEmailEditText);
        binding.emailEditGroup.setVisibility(View.GONE);
        binding.emailDisplayGroup.setVisibility(View.VISIBLE);
    }

    private void saveEmail() {
        if (binding.inputEmailEditText.getText() == null) return;

        String newEmail = binding.inputEmailEditText.getText().toString().trim();

        if (TextUtils.isEmpty(newEmail)) {
            binding.inputEmailLayout.setError(getString(R.string.email_required));
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            binding.inputEmailLayout.setError(getString(R.string.email_invalid));
            return;
        }

        binding.inputEmailLayout.setError(null);

        String phone = currentContact != null ? currentContact.getPhoneNumber() : null;
        ContactInfoDto toUpdate = new ContactInfoDto(newEmail, phone);

        showLoading(true);
        hideKeyboard(binding.inputEmailEditText);

        apiService.updateMyContact(toUpdate).enqueue(new Callback<ContactInfoDto>() {
            @Override
            public void onResponse(@NonNull Call<ContactInfoDto> call,
                                   @NonNull Response<ContactInfoDto> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    currentContact = response.body();
                    renderContact(currentContact);
                    hideEmailEditor();
                    showSnack(getString(R.string.email_updated));
                } else {
                    showSnack(getString(R.string.error_updating_email));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ContactInfoDto> call,
                                  @NonNull Throwable t) {
                showLoading(false);
                showSnack(getString(R.string.error_network));
            }
        });
    }

    // endregion

    // region Phone

    private void showPhoneEditor() {
        String phone = currentContact != null ? currentContact.getPhoneNumber() : "";
        binding.inputPhoneEditText.setText(phone);
        binding.inputPhoneLayout.setError(null);

        binding.phoneDisplayGroup.setVisibility(View.GONE);
        binding.phoneEditGroup.setVisibility(View.VISIBLE);

        binding.inputPhoneEditText.requestFocus();
        showKeyboard(binding.inputPhoneEditText);
    }

    private void hidePhoneEditor() {
        hideKeyboard(binding.inputPhoneEditText);
        binding.phoneEditGroup.setVisibility(View.GONE);
        binding.phoneDisplayGroup.setVisibility(View.VISIBLE);
    }

    private void sendVerificationCode() {
        if (binding.inputPhoneEditText.getText() == null) return;

        String newPhone = binding.inputPhoneEditText.getText().toString().trim();

        if (TextUtils.isEmpty(newPhone)) {
            binding.inputPhoneLayout.setError(getString(R.string.phone_required));
            return;
        }
        if (!Patterns.PHONE.matcher(newPhone).matches()) {
            binding.inputPhoneLayout.setError(getString(R.string.phone_invalid));
            return;
        }

        binding.inputPhoneLayout.setError(null);

        showLoading(true);
        hideKeyboard(binding.inputPhoneEditText);

        PhoneNumberRequest request = new PhoneNumberRequest(newPhone);

        apiService.sendPhoneVerificationCode(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call,
                                   @NonNull Response<Void> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    hidePhoneEditor();
                    showSnack(getString(R.string.code_sent));
                    openVerifyActivity(newPhone);
                } else {
                    showSnack(getString(R.string.error_sending_code));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call,
                                  @NonNull Throwable t) {
                showLoading(false);
                showSnack(getString(R.string.error_network));
            }
        });
    }

    private void openVerifyActivity(String phone) {
        Intent intent = new Intent(this, VerifyPhoneActivity.class);
        intent.putExtra(VerifyPhoneActivity.EXTRA_PHONE_NUMBER, phone);
        startActivity(intent);
    }

    // endregion

    // region Helpers

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showSnack(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
    }

    private void showKeyboard(View view) {
        Context context = this;
        InputMethodManager imm =
                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void hideKeyboard(View view) {
        Context context = this;
        InputMethodManager imm =
                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && view.getWindowToken() != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // endregion
}