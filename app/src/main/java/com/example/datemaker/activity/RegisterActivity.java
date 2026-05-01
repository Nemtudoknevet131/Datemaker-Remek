package com.example.datemaker.activity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.*;

import com.example.datemaker.R;
import com.example.datemaker.model.RegisterRequest;
import com.example.datemaker.utils.UserSessionManager;
import com.example.datemaker.viewmodel.RegisterViewModel;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.*;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;

import androidx.core.content.ContextCompat;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {

    // region constants
    private static final String TAG_CAPTCHA = "CAPTCHA";
    private static final String TAG_CAPTCHA_JS = "CAPTCHA_JS";
    private static final String TAG_FB_LOGIN = "FB_LOGIN";
    private static final String TAG_FB_TOKEN = "FB_TOKEN";
    private static final String TAG_FB_USER = "FB_USER";
    private static final String TAG_FB_PARSE = "FB_PARSE";
    private static final String TAG_GOOGLE_CREDENTIAL = "GOOGLE_CREDENTIAL";

    private static final String CAPTCHA_URL = "https://muscular-clinking-traction.ngrok-free.dev/recaptcha.html?ts=";
    private static final String GOOGLE_CLIENT_ID = "301039942357-glghj754l5dval84g1d3kd5psv4tm4s8.apps.googleusercontent.com";
    private static final long CAPTCHA_FALLBACK_DELAY = 5000L;

    private static final String TEXT_ALREADY_HAVE_ACCOUNT = "Already have an account? Log in!";
    private static final String TEXT_LOGIN_PART = "Log in!";
    private static final String DATE_FORMAT = "%04d-%02d-%02d";

    private static final int MIN_AGE = 16;
    private static final int MAX_AGE = 120;
    // endregion

    private TextInputEditText username;
    private TextInputEditText password;
    private TextInputEditText firstName;
    private TextInputEditText lastName;
    private TextInputEditText email;
    private TextInputEditText confirmPassword;
    private TextInputLayout usernameLayout;
    private TextInputLayout passwordLayout;
    private TextInputLayout firstNameLayout;
    private TextInputLayout lastNameLayout;
    private TextInputLayout emailLayout;
    private TextInputLayout confirmPasswordLayout;
    private CallbackManager callbackManager;
    private TextInputEditText dobEditText;
    private TextView loginText;

    private FrameLayout captchaContainer;
    private String recaptchaToken = null;
    private WebView captchaWebView;

    private MaterialCheckBox checkBoxAge;
    private MaterialCheckBox checkBoxToS;
    private CredentialManager credentialManager;
    private GetCredentialRequest googleCredentialRequest;

    private RegisterViewModel viewModel;
    private ScrollView scrollView;
    private LinearLayout passwordReqLayout;
    private MaterialButton registerButton;

    private ImageView iconLengthOutline;
    private ImageView iconLengthFilled;
    private ImageView iconCapitalOutline;
    private ImageView iconCapitalFilled;
    private ImageView iconSpecialOutline;
    private ImageView iconSpecialFilled;
    private ImageView iconNumberOutline;
    private ImageView iconNumberFilled;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (shouldSkipToMain()) return;

        setContentView(R.layout.activity_register);

        initViews();
        initViewModel();
        setupCaptchaWebView();
        setupCaptchaFallback();
        setupGoogleSignInWithCredentialManager();
        setupSocialCards();
        setupDOBPicker();
        setupPasswordWatcher();
        setupPasswordFocusAnimation();
        setupScrollFocusHelpers();
        setupRegisterButtonClick();
        setupLoginText();
        observeViewModel();
    }

    // region onCreate sub-methods

    private boolean shouldSkipToMain() {
        UserSessionManager session = new UserSessionManager(this);
        if (session.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return true;
        }
        return false;
    }


    @SuppressLint("SetJavaScriptEnabled")
    private void setupCaptchaWebView() {
        WebSettings ws = captchaWebView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        captchaWebView.setWebViewClient(new android.webkit.WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                Log.d(TAG_CAPTCHA, "Page loaded: " + url);
            }
        });

        captchaWebView.setWebChromeClient(new android.webkit.WebChromeClient() {
            @Override
            public boolean onConsoleMessage(android.webkit.ConsoleMessage consoleMessage) {
                Log.d(TAG_CAPTCHA_JS,
                        consoleMessage.message()
                                + " -- line " + consoleMessage.lineNumber()
                                + " of " + consoleMessage.sourceId());
                return true;
            }
        });

        captchaWebView.addJavascriptInterface(new Object() {
            @android.webkit.JavascriptInterface
            public void onToken(String token) {
                recaptchaToken = token;
                runOnUiThread(() ->
                        Toast.makeText(RegisterActivity.this, "Captcha OK", Toast.LENGTH_SHORT).show()
                );
            }

            @android.webkit.JavascriptInterface
            public void onError(String msg) {
                runOnUiThread(() ->
                        Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_SHORT).show()
                );
            }
        }, "Android");

        java.util.Map<String, String> extraHeaders = new java.util.HashMap<>();
        extraHeaders.put("ngrok-skip-browser-warning", "true");
        captchaWebView.loadUrl(CAPTCHA_URL + System.currentTimeMillis(), extraHeaders);
    }

    private void setupCaptchaFallback() {
        captchaWebView.postDelayed(() -> {
            if (recaptchaToken == null) {
                openCaptchaInBrowser();
            }
        }, CAPTCHA_FALLBACK_DELAY);
    }

    private void initViews() {
        captchaContainer = findViewById(R.id.captchaContainer);
        captchaWebView = new WebView(this);
        captchaContainer.addView(captchaWebView);

        username = findViewById(R.id.textInputEditTextUsername);
        password = findViewById(R.id.textInputEditTextPassword);
        firstName = findViewById(R.id.textInputEditTextFirstName);
        lastName = findViewById(R.id.textInputEditTextLastName);
        email = findViewById(R.id.textInputEditTextEmail);
        confirmPassword = findViewById(R.id.textInputEditTextConfirmPassword);
        usernameLayout = findViewById(R.id.textInputLayoutUsername);
        passwordLayout = findViewById(R.id.textInputLayoutPassword);
        firstNameLayout = findViewById(R.id.textInputLayoutFirstName);
        lastNameLayout = findViewById(R.id.textInputLayoutLastName);
        emailLayout = findViewById(R.id.textInputLayoutEmail);
        confirmPasswordLayout = findViewById(R.id.textInputLayoutConfirmPassword);
        registerButton = findViewById(R.id.registerButton);

        iconLengthOutline = findViewById(R.id.icon_length_outline);
        iconLengthFilled = findViewById(R.id.icon_length_filled);
        iconCapitalOutline = findViewById(R.id.icon_capital_outline);
        iconCapitalFilled = findViewById(R.id.icon_capital_filled);
        iconSpecialOutline = findViewById(R.id.icon_special_outline);
        iconSpecialFilled = findViewById(R.id.icon_special_filled);
        iconNumberOutline = findViewById(R.id.icon_number_outline);
        iconNumberFilled = findViewById(R.id.icon_number_filled);

        dobEditText = findViewById(R.id.textInputEditTextDOB);
        scrollView = findViewById(R.id.scrollViewRoot);
        passwordReqLayout = findViewById(R.id.passwordRequirementsLayout);
        checkBoxAge = findViewById(R.id.checkBoxAge);
        checkBoxToS = findViewById(R.id.checkBoxToS);
        loginText = findViewById(R.id.loginText);
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);
    }

    private void setupSocialCards() {
        MaterialCardView googleSignInCard = findViewById(R.id.googleSignInCard);
        MaterialCardView facebookSignInCard = findViewById(R.id.facebookSignInCard);

        googleSignInCard.setOnClickListener(v -> signInWithGoogleCM());

        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, createFacebookCallback());

        facebookSignInCard.setOnClickListener(v -> {
            LoginManager.getInstance().logInWithReadPermissions(
                    this,
                    Arrays.asList("email", "public_profile", "user_birthday")
            );
        });
    }

    private FacebookCallback<LoginResult> createFacebookCallback() {
        return new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG_FB_LOGIN, "Login successful");
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG_FB_LOGIN, "Login denied");
                Toast.makeText(RegisterActivity.this, "Login cancelled.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(@NonNull FacebookException e) {
                Log.d(TAG_FB_LOGIN, "Facebook error", e);
                Toast.makeText(RegisterActivity.this, "Facebook error.", Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void setupDOBPicker() {
        dobEditText.setClickable(true);
        dobEditText.setFocusable(false);
        dobEditText.setOnClickListener(v -> showDobPicker());
    }

    private void setupPasswordWatcher() {
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {}
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                updatePasswordRequirementIcons();
            }
        });
    }

    private void setupPasswordFocusAnimation() {
        passwordReqLayout.setVisibility(View.GONE);
        passwordReqLayout.setAlpha(0f);
        passwordReqLayout.setTranslationY(30f);

        password.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                scrollView.post(() -> scrollView.smoothScrollTo(0, scrollView.getChildAt(0).getBottom()));
                passwordReqLayout.setVisibility(View.VISIBLE);
                passwordReqLayout.animate().alpha(1f).translationY(0f).setDuration(500).start();
            } else {
                passwordReqLayout.animate()
                        .alpha(0f)
                        .translationY(30f)
                        .setDuration(500)
                        .withEndAction(() -> passwordReqLayout.setVisibility(View.GONE))
                        .start();
            }
        });
    }

    private void setupScrollFocusHelpers() {
        View.OnFocusChangeListener focusListener = (v, hasFocus) -> {
            if (hasFocus) {
                scrollView.post(() -> scrollView.smoothScrollTo(0, v.getBottom()));
            }
        };
        username.setOnFocusChangeListener(focusListener);
        firstName.setOnFocusChangeListener(focusListener);
        lastName.setOnFocusChangeListener(focusListener);
        email.setOnFocusChangeListener(focusListener);
        dobEditText.setOnFocusChangeListener(focusListener);
        confirmPassword.setOnFocusChangeListener((v, hasfocus) -> {
            if (hasfocus) {
                scrollView.smoothScrollTo(0, scrollView.getChildAt(0).getBottom());
            }
        });
    }

    private void setupRegisterButtonClick() {
        registerButton.setOnClickListener(v -> {
            if (recaptchaToken == null) {
                Toast.makeText(RegisterActivity.this, "Please solve the captcha", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isAllFieldsNonNull()) {
                String f = firstName.getText().toString().trim();
                String l = lastName.getText().toString().trim();
                String u = username.getText().toString().trim();
                String e = email.getText().toString().trim();
                String p = password.getText().toString().trim();
                String c = confirmPassword.getText().toString().trim();
                String d = dobEditText.getText().toString().trim();
                boolean isAgeConfirmed = checkBoxAge.isChecked();
                boolean isTosAccepted = checkBoxToS.isChecked();

                if (viewModel.validateInputs(f, l, u, e, p, c, d, isAgeConfirmed, isTosAccepted)) {
                    RegisterRequest req = new RegisterRequest();
                    req.setFirstName(f);
                    req.setLastName(l);
                    req.setUsername(u);
                    req.setEmail(e);
                    req.setPassword(p);
                    req.setAdult(true);
                    req.setDateOfBirth(d);
                    req.setRecaptchaToken(recaptchaToken);

                    viewModel.registerUser(req);
                }
            }
        });
    }

    private void setupLoginText() {
        String text = TEXT_ALREADY_HAVE_ACCOUNT;
        SpannableString spannable = new SpannableString(text);
        int start = text.indexOf(TEXT_LOGIN_PART);
        int end = start + TEXT_LOGIN_PART.length();
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
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
        spannable.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        loginText.setText(spannable);
        loginText.setMovementMethod(LinkMovementMethod.getInstance());
        loginText.setHighlightColor(Color.TRANSPARENT);
    }

    private void observeViewModel() {
        viewModel.loading.observe(this, isLoading -> {
            registerButton.setEnabled(!isLoading);
            registerButton.setText(isLoading ? "Registering..." : "Register");
        });

        viewModel.errorMessage.observe(this, this::handleErrorMessage);

        viewModel.success.observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                Intent intent = new Intent(RegisterActivity.this, EmailValidationActivity.class);
                intent.putExtra("email", email.getText().toString().trim());
                intent.putExtra("email_sent", true);
                startActivity(intent);
                finish();
            }
        });

        viewModel._googleLoginSuccess.observe(this, authResponse -> {
            if (authResponse == null) return;

            UserSessionManager session = new UserSessionManager(this);
            session.saveUserSession(
                    authResponse.getUser().getId(),
                    authResponse.getUser().getEmail(),
                    authResponse.getUser().getFirstName()
            );

            session.saveAuthToken(authResponse.getToken());
            session.saveUserAvatar(authResponse.getUser().getAvatarUrl());

            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        viewModel.facebookLoginSuccess.observe(this, authResponse -> {
            if (authResponse == null) return;

            UserSessionManager session = new UserSessionManager(this);
            session.saveUserSession(
                    authResponse.getUser().getId(),
                    authResponse.getUser().getEmail(),
                    authResponse.getUser().getFirstName()
            );

            session.saveAuthToken(authResponse.getToken());
            session.saveUserAvatar(authResponse.getUser().getAvatarUrl());

            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    // endregion

    // region helpers

    private void handleErrorMessage(String msg) {
        firstNameLayout.setError(null);
        lastNameLayout.setError(null);
        usernameLayout.setError(null);
        emailLayout.setError(null);
        passwordLayout.setError(null);
        confirmPasswordLayout.setError(null);

        if (msg == null) return;

        if (msg.contains("First name")) {
            firstNameLayout.setError(msg);
        } else if (msg.contains("Last name")) {
            lastNameLayout.setError(msg);
        } else if (msg.contains("Username")) {
            usernameLayout.setError(msg);
        } else if (msg.contains("Email")) {
            emailLayout.setError(msg);
        } else if (msg.contains("Password")) {
            passwordLayout.setError(msg);
        } else if (msg.contains("Confirm password")) {
            confirmPasswordLayout.setError(msg);
        } else if (msg.contains("You must confirm")) {
            checkBoxAge.setError(msg);
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        } else if (msg.contains("You must accept")) {
            checkBoxToS.setError(msg);
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isAllFieldsNonNull() {
        return firstName.getText() != null && lastName.getText() != null && username.getText() != null &&
                email.getText() != null && password.getText() != null && confirmPassword.getText() != null &&
                dobEditText.getText() != null;
    }

    private void showDobPicker() {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(RegisterActivity.this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String dateString = String.format(Locale.US, DATE_FORMAT,
                            year1, monthOfYear + 1, dayOfMonth);
                    dobEditText.setText(dateString);
                }, year, month, day);

        Calendar maxDob = (Calendar) c.clone();
        maxDob.add(Calendar.YEAR, -MIN_AGE);

        Calendar minDob = (Calendar) c.clone();
        minDob.add(Calendar.YEAR, -MAX_AGE);

        datePickerDialog.getDatePicker().setMaxDate(maxDob.getTimeInMillis());
        datePickerDialog.getDatePicker().setMinDate(minDob.getTimeInMillis());

        datePickerDialog.show();

        datePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.WHITE);
        datePickerDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.WHITE);
    }

    private void updatePasswordRequirementIcons() {
        if (password != null && password.getText() != null) {
            String pass = password.getText().toString().trim();

            boolean hasLength = pass.length() >= 6;
            boolean isCapital = pass.matches("^[A-Z].*");
            boolean containsSpecial = pass.matches(".*[!@#$%^&*(),.?\":{}|<>].*");
            boolean containsNumber = pass.matches(".*[0-9].*");

            animateIconPair(iconLengthOutline, iconLengthFilled, hasLength);
            animateIconPair(iconCapitalOutline, iconCapitalFilled, isCapital);
            animateIconPair(iconSpecialOutline, iconSpecialFilled, containsSpecial);
            animateIconPair(iconNumberOutline, iconNumberFilled, containsNumber);
        }
    }

    private void animateIconPair(ImageView outline, ImageView filled, boolean condition) {
        if (condition) {
            outline.animate().alpha(0f).setDuration(300).start();
            filled.animate().alpha(1f).setDuration(300).start();
        } else {
            outline.animate().alpha(1f).setDuration(300).start();
            filled.animate().alpha(0f).setDuration(300).start();
        }
    }

    private void setupGoogleSignInWithCredentialManager() {
        credentialManager = CredentialManager.create(this);

        GetSignInWithGoogleOption googleOption =
                new GetSignInWithGoogleOption.Builder(GOOGLE_CLIENT_ID)
                        .build();

        googleCredentialRequest = new GetCredentialRequest.Builder()
                .addCredentialOption(googleOption)
                .build();
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
                        Log.e(TAG_GOOGLE_CREDENTIAL, "getCredentialAsync failed", e);

                        String simpleName = e.getClass().getSimpleName();
                        Log.e(TAG_GOOGLE_CREDENTIAL, "Exception type: " + simpleName);

                        if ("GetCredentialCancellationException".equals(simpleName)) {
                            return;
                        }

                        Toast.makeText(RegisterActivity.this, "Google sign-in failed", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void openCaptchaInBrowser() {
        String url = CAPTCHA_URL + System.currentTimeMillis();
        Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url));
        startActivity(intent);
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG_FB_TOKEN, "Access Token: " + token.getToken());
        viewModel.loginWithFacebook(token.getToken());
    }

    private void handleGoogleCredentialResult(GetCredentialResponse response) {
        if (response.getCredential() == null) return;

        if (GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(response.getCredential().getType())) {

            GoogleIdTokenCredential googleCred = GoogleIdTokenCredential.createFrom(response.getCredential().getData());

            String idToken = googleCred.getIdToken();

            viewModel.loginWithGoogle(idToken);
        }
    }

    // endregion

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
                    v.clearFocus();
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