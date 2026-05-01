package com.example.datemaker.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class AppLaunchManager {
    private static final String PREF_NAME = "AppLaunchPrefs";
    private static final String KEY_LAUNCHED_BEFORE = "launched_before";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public AppLaunchManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public boolean isFirstLaunch() {
        return !prefs.getBoolean(KEY_LAUNCHED_BEFORE, false);
    }

    public void setLaunched() {
        editor.putBoolean(KEY_LAUNCHED_BEFORE, true);
        editor.apply();
    }

    public void reset() {
        editor.clear();
        editor.apply();
    }
}
