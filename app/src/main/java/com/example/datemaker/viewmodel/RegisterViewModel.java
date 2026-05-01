package com.example.datemaker.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.datemaker.model.AuthResponse;
import com.example.datemaker.model.FacebookLoginRequest;
import com.example.datemaker.model.GoogleLoginRequest;
import com.example.datemaker.model.RegisterRequest;
import com.example.datemaker.model.UserDto;
import com.example.datemaker.retrofit.ApiService;
import com.example.datemaker.retrofit.RetrofitClient;
import com.google.android.gms.auth.api.Auth;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterViewModel extends AndroidViewModel {

    private final ApiService apiService;
    private final MutableLiveData<Boolean> _loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> _success = new MutableLiveData<>(false);
    public final MutableLiveData<AuthResponse> _googleLoginSuccess = new MutableLiveData<>();
    public final MutableLiveData<AuthResponse> _facebookLoginSuccess = new MutableLiveData<>();

    public LiveData<Boolean> loading = _loading;
    public LiveData<String> errorMessage = _errorMessage;
    public LiveData<Boolean> success = _success;
    public LiveData<AuthResponse> facebookLoginSuccess = _facebookLoginSuccess;

    public RegisterViewModel(@NonNull Application application) {
        super(application);
        apiService = RetrofitClient
                .getRetrofitInstance(application.getApplicationContext())
                .create(ApiService.class);
    }

    public void loginWithGoogle(String idToken) {
        _loading.setValue(true);

        GoogleLoginRequest req = new GoogleLoginRequest(idToken);

        apiService.googleLogin(req).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                _loading.postValue(false);

                Log.e("GOOGLE_LOGIN", "onResponse: code=" + response.code()
                        + " url=" + call.request().url());

                if (!response.isSuccessful()) {
                    // try to read backend message
                    String msg = "Error: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            msg = response.errorBody().string();
                            Log.e("GOOGLE_LOGIN", "errorBody=" + msg);
                        }
                    } catch (Exception e) {
                        Log.e("GOOGLE_LOGIN", "errorBody read failed", e);
                    }

                    _errorMessage.postValue(msg);
                    return;
                }

                if (response.body() != null) {
                    Log.d("GOOGLE_LOGIN", "success, token=" + response.body().getToken());
                    _googleLoginSuccess.postValue(response.body());
                } else {
                    Log.e("GOOGLE_LOGIN", "successful HTTP but body is null");
                    _errorMessage.postValue("Empty response from server");
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                _loading.postValue(false);
                Log.e("GOOGLE_LOGIN", "onFailure: " + t.getMessage(), t);
                _errorMessage.postValue("Google login failed");
            }
        });
    }

    public void loginWithFacebook(String fbAccessToken) {
        _loading.postValue(true);
        apiService.facebookAuth(new FacebookLoginRequest(fbAccessToken)).enqueue(new Callback<AuthResponse>() {
            @Override public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                _loading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    _facebookLoginSuccess.postValue(response.body());
                } else {
                    String msg = "Facebook login failed (" + response.code() + ")";
                    try {
                        if (response.errorBody() != null) msg = response.errorBody().string();
                    } catch (Exception ignored) {}
                    Log.e("FB_LOGIN", "HTTP " + response.code() + " url=" + call.request().url() + " msg=" + msg);
                    _errorMessage.postValue(msg);
                }
            }
            @Override public void onFailure(Call<AuthResponse> call, Throwable t) {
                _loading.postValue(false);
                Log.e("FB_LOGIN", "Network error", t);
                _errorMessage.postValue("Network error: " + t.getMessage());
            }
        });
    }

    public boolean validateInputs(String firstName, String lastName, String username,
                                  String email, String password, String confirmPassword,
                                  String dob, boolean isAgeConfirmed, boolean isTosAccepted) {

        if (firstName == null || firstName.isEmpty() || !firstName.matches("^[A-Z].*")) {
            _errorMessage.setValue("First name must start with a capital letter.");
            return false;
        }

        if (lastName == null || lastName.isEmpty() || !lastName.matches("^[A-Z].*")) {
            _errorMessage.setValue("Last name must start with a capital value.");
            return false;
        }

        if (username == null || username.isEmpty()) {
            _errorMessage.setValue("Username field must be filled out.");
            return false;
        } else if (username.length() < 4 || username.length() > 16) {
            _errorMessage.setValue("Username must be between 4 and 16 characters.");
            return false;
        }

        if (email == null || email.isEmpty()) {
            _errorMessage.setValue("Email is required.");
            return false;
        } else if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            _errorMessage.setValue("Invalid email address.");
            return false;
        }

        if (password == null || password.isEmpty() || !password.matches("^.*[A-Z].*")) {
            _errorMessage.setValue("Password must contain at least one capital letter");
            return false;
        } else if (!password.matches(".*[a-z].*")) {
            _errorMessage.setValue("Password must contain at least one lowercase letter");
            return false;
        } else if (password.length() < 6) {
            _errorMessage.setValue("Password must be at least 6 characters.");
            return false;
        } else if (!password.matches(".*[!@#$%^&*(),.?\":{}|<>_].*")) {
            _errorMessage.setValue("Password must contain at least one special character!");
            return false;
        } else if (!password.matches(".*[0-9].*")) {
            _errorMessage.setValue("Password must contain at least one number!");
            return false;
        }

        if (confirmPassword == null || confirmPassword.isEmpty()) {
            _errorMessage.setValue("Confirm password is required.");
            return false;
        } else if (!confirmPassword.equals(password)) {
            _errorMessage.setValue("Passwords do not match");
            return false;
        }

        if (dob == null || dob.isEmpty()) {
            _errorMessage.setValue("Date of birth is required.");
            return false;
        }

        if (!isAgeConfirmed) {
            _errorMessage.setValue("You must confirm you are at least 16 years old.");
            return false;
        }

        if (!isTosAccepted) {
            _errorMessage.setValue("You must accept the Terms of Service.");
            return false;
        }

        return true;
    }

    public void registerUser(RegisterRequest request) {
        _loading.setValue(true);

        Call<UserDto> call = apiService.createUser(request);

        call.enqueue(new Callback<UserDto>() {
            @Override
            public void onResponse(@NonNull Call<UserDto> call,
                                   @NonNull Response<UserDto> response) {
                _loading.setValue(false);

                if (response.isSuccessful()) {
                    _success.setValue(true);
                    _errorMessage.setValue(null);
                } else {
                    String errorBody;
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        } else {
                            errorBody = "Unknown error (empty response body)";
                        }
                    } catch (IOException e) {
                        errorBody = "Error reading errorBody(): " + e.getMessage();
                    }

                    Log.e("REGISTER", "Registration failed: code=" + response.code()
                            + " body=" + errorBody);

                    _success.setValue(false);
                    _errorMessage.setValue("Registration failed: " + errorBody);
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserDto> call, @NonNull Throwable t) {
                _loading.setValue(false);
                _success.setValue(false);
                _errorMessage.setValue("Error: " + t.getMessage());
            }
        });
    }
}
