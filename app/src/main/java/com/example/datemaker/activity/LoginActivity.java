package com.example.datemaker.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.example.datemaker.R;
import com.example.datemaker.model.AuthResponse;
import com.example.datemaker.model.GoogleLoginRequest;
import com.example.datemaker.model.Login;
import com.example.datemaker.model.UserDto;
import com.example.datemaker.retrofit.ApiService;
import com.example.datemaker.retrofit.RetrofitClient;
import com.example.datemaker.utils.UserSessionManager;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String GOOGLE_CLIENT_ID = "301039942357-glghj754l5dval84g1d3kd5psv4tm4s8.apps.googleusercontent.com";
    private static final String TAG_GOOGLE = "GOOGLE_LOGIN";

    private MaterialButton loginButton;
    private TextInputEditText email;
    private TextInputEditText password;
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private CallbackManager callbackManager;
    private TextView signUpText;
    private TextView forgotPasswordText;

    // credential manager
    private CredentialManager credentialManager;
    private GetCredentialRequest googleCredentialRequest;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        initViews();
        setupEmailPasswordLogin();
        setupSignUpText();
        setupFacebookLogin();
        setupGoogleSignInWithCredentialManager();
        setupGoogleCardClick();
        setupForgotPasswordClick();
    }

    private void initViews() {
        loginButton = findViewById(R.id.loginButton);
        email = findViewById(R.id.textInputEditTextEmail);
        password = findViewById(R.id.textInputEditTextPassword);
        emailLayout = findViewById(R.id.textInputLayoutEmail);
        passwordLayout = findViewById(R.id.textInputLayoutPassword);
        signUpText = findViewById(R.id.signUpText);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);
    }

    private void setupEmailPasswordLogin() {
        loginButton.setOnClickListener(v -> {
            if (email.getText() != null && password.getText() != null) {

                Login loginUser = new Login(
                        email.getText().toString().trim(),
                        password.getText().toString().trim()
                );

                loginButton.setEnabled(false);
                loginButton.setText("Logging in...");

                apiService.loginUser(loginUser).enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        loginButton.setEnabled(true);
                        loginButton.setText("Login");

                        if (response.isSuccessful() && response.body() != null) {
                            AuthResponse auth = response.body();
                            UserDto user = auth.getUser();

                            UserSessionManager session = new UserSessionManager(LoginActivity.this);
                            session.saveUserSession(user.getId(), user.getEmail(), user.getFirstName());
                            session.saveAuthToken(auth.getToken());

                            Toast.makeText(LoginActivity.this, "Welcome: " + user.getFirstName(), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            try {
                                String errorMsg = response.errorBody() != null ? response.errorBody().string() : "";
                                if (errorMsg.contains("Email is not verified")) {
                                    Toast.makeText(LoginActivity.this, "Email is not verified", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(LoginActivity.this, "Invalid email or password.", Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                Toast.makeText(LoginActivity.this, "Login failed.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        loginButton.setEnabled(true);
                        loginButton.setText("Login");
                        Toast.makeText(LoginActivity.this, "Server not reachable.", Toast.LENGTH_SHORT).show();
                        Log.e("LOGIN_ERROR", "Error: ", t);
                    }
                });
            }
        });
    }

    private void setupForgotPasswordClick() {
        if (forgotPasswordText != null) {
            forgotPasswordText.setOnClickListener(v -> {
                startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
            });
        }
    }

    private void setupSignUpText() {
        String text = "Dont have an account? Sign up!";
        SpannableString spannable = new SpannableString(text);
        int start = text.indexOf("Sign up!");
        int end = start + "Sign up!".length();

        ClickableSpan clickable = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(getResources().getColor(R.color.purple_200));
                ds.setUnderlineText(true);
                ds.setFakeBoldText(true);
            }
        };

        spannable.setSpan(clickable, start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        signUpText.setText(spannable);
        signUpText.setMovementMethod(LinkMovementMethod.getInstance());
        signUpText.setHighlightColor(Color.TRANSPARENT);
    }

    private void setupFacebookLogin() {
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this.getApplication());
        callbackManager = CallbackManager.Factory.create();

        MaterialCardView facebookSignInCard = findViewById(R.id.facebookSignInCard);
        facebookSignInCard.setOnClickListener(v -> {
            LoginManager.getInstance().logInWithReadPermissions(
                    this,
                    Arrays.asList("email", "public_profile")
            );

            LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    Log.d("FB_LOGIN", "Login successful");
                    handleFacebookAccessToken(loginResult.getAccessToken());
                }

                @Override
                public void onCancel() {
                    Log.d("FB_LOGIN", "Login denied");
                    Toast.makeText(LoginActivity.this, "Login cancelled.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(@NonNull FacebookException e) {
                    Log.d("FB_LOGIN", "Something happened G");
                }
            });
        });
    }

    private void setupGoogleSignInWithCredentialManager() {
        credentialManager = CredentialManager.create(this);

        GetSignInWithGoogleOption googleOption =
                new GetSignInWithGoogleOption.Builder(GOOGLE_CLIENT_ID).build();

        googleCredentialRequest = new GetCredentialRequest.Builder()
                .addCredentialOption(googleOption)
                .build();
    }

    private void setupGoogleCardClick() {
        MaterialCardView googleSignInCard = findViewById(R.id.googleSignInCard);
        googleSignInCard.setOnClickListener(v -> signInWithGoogleCM());
    }

    private void signInWithGoogleCM() {
        if (credentialManager == null || googleCredentialRequest == null) return;

        credentialManager.getCredentialAsync(
                this,
                googleCredentialRequest,
                null,
                ContextCompat.getMainExecutor(this),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse getCredentialResponse) {
                        handleGoogleCredentialResult(getCredentialResponse);
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        Log.e(TAG_GOOGLE, "Google CM error: " + e.getMessage(), e);
                        Toast.makeText(LoginActivity.this, "Google sign-in failed", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void handleGoogleCredentialResult(GetCredentialResponse response) {
        if (response.getCredential() == null) return;

        if (GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                .equals(response.getCredential().getType())) {

            GoogleIdTokenCredential googleCred =
                    GoogleIdTokenCredential.createFrom(response.getCredential().getData());

            String idToken = googleCred.getIdToken();
            if (idToken == null) {
                Toast.makeText(this, "Google token missing", Toast.LENGTH_SHORT).show();
                return;
            }

            // 🔥 For login screen we call /api/users/google-auth (the lenient one)
            sendGoogleTokenToBackend(idToken);
        }
    }

    private void sendGoogleTokenToBackend(String idToken) {
        GoogleLoginRequest req = new GoogleLoginRequest(idToken);

        apiService.googleAuth(req).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse auth = response.body();
                    UserDto user = auth.getUser();

                    UserSessionManager session = new UserSessionManager(LoginActivity.this);
                    session.saveUserSession(user.getId(), user.getEmail(), user.getFirstName());
                    session.saveAuthToken(auth.getToken());

                    Toast.makeText(LoginActivity.this, "Welcome: " + user.getFirstName(), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    String msg = "Google login failed";
                    try {
                        if (response.errorBody() != null) {
                            msg = response.errorBody().string();
                        }
                    } catch (Exception ignored) {}
                    Log.e(TAG_GOOGLE, "code=" + response.code() + " msg=" + msg);
                    Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Log.e(TAG_GOOGLE, "error", t);
                Toast.makeText(LoginActivity.this, "Server not reachable.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d("FB_TOKEN", "Access Token: " + token.getToken());

        GraphRequest request = GraphRequest.newMeRequest(
                token,
                (object, response) -> {
                    try {
                        String name = object.optString("name");
                        String email = object.optString("email");

                        Log.d("FB_USER", "Name: " + name + ", Email: " + email);
                        Toast.makeText(this, "Welcome, " + name, Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();

                    } catch (Exception e) {
                        Log.e("FB_PARSE", "Error parsing user data", e);
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email");
        request.setParameters(parameters);
        request.executeAsync();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (callbackManager != null) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();

            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);

                if (!outRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                    if (imm != null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}