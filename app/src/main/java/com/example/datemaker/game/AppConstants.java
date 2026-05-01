package com.example.datemaker.game;

import android.content.Context;

public class AppConstants {

    public static int surfaceViewWidth;
    public static int surfaceViewHeight;

    private static Context appContext;
    private static GameEngine gameEngine;

    public static void initialize(Context context) {
        appContext = context.getApplicationContext();
        gameEngine = new GameEngine();
    }

    public static Context getContext() {
        return appContext;
    }

    public static GameEngine getGameEngine() {
        return gameEngine;
    }
}
