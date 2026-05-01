package com.example.datemaker.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.airbnb.lottie.animation.content.Content;

public class UserSessionManager {
    private static final String PREF_NAME = "DateMakerUserSession";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_ID_STR = "user_id_str";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_PARTNER_ID = "partner_id";
    private static final String KEY_AUTH_TOKEN = "auth_token";
    private static final String KEY_AVATAR_URL = "avatar_url";
    private static final String KEY_IS_PREMIUM = "is_premium";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public UserSessionManager(Context context) {
        prefs = context.getSharedPreferences(
                PREF_NAME, Context.MODE_PRIVATE);

        editor = prefs.edit();
    }

    public void saveUserSession(Long userId, String userEmail, String userName) {
        editor.putLong(KEY_USER_ID, userId);
        editor.remove(KEY_USER_ID_STR);
        editor.putString(KEY_USER_EMAIL, userEmail);
        editor.putString(KEY_USER_NAME, userName);
        editor.apply();
    }

    public void saveUserSession(String userIdStr, String userEmail, String userName) {
        editor.putLong(KEY_USER_ID, -1L);
        editor.putString(KEY_USER_ID_STR, userIdStr);
        editor.putString(KEY_USER_EMAIL, userEmail);
        editor.putString(KEY_USER_NAME, userName);
        editor.apply();
    }

    public void savePremium(boolean premium) {
        editor.putBoolean(KEY_IS_PREMIUM, premium);
        editor.apply();
    }

    public boolean isPremium() {
        return prefs.getBoolean(KEY_IS_PREMIUM, false);
    }

    public void saveAuthToken(String token) {
        editor.putString(KEY_AUTH_TOKEN, token);
        editor.apply();
    }

    public String getAuthToken() {
        return prefs.getString(KEY_AUTH_TOKEN, null);
    }

    public boolean isLoggedIn() {
        return prefs.contains(KEY_USER_ID) || prefs.contains(KEY_USER_ID_STR);
    }

    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, -1);
    }

    public String getUserIdString() {
        return prefs.getString(KEY_USER_ID_STR, null);
    }

    public long getPartnerId() {
        return prefs.getLong(KEY_PARTNER_ID, -1);
    }

    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, null);
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, null);
    }

    public void setPartnerId(Long partnerId) {
        editor.putLong(KEY_PARTNER_ID, partnerId);
        editor.apply();
    }

    public String getAvatarUrl() {
        return prefs.getString(KEY_AVATAR_URL, null);
    }

    public void saveUserAvatar(String url) {
        prefs.edit().putString(KEY_AVATAR_URL, url).apply();
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}
