package com.example.datemaker.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.Toast;

import java.util.Random;

public class AdPopupManager {
    private static int gamesPlayed = 0;
    private static int targetGames = -1;

    public static void onGameEnd(Context context, Runnable onDoubleMoney) {
        if (targetGames == -1) {
            resetTarget();
        }

        gamesPlayed++;

        if (gamesPlayed >= targetGames) {
            showAdPopup(context, onDoubleMoney);
            gamesPlayed = 0;
            resetTarget();
        }
    }

    private static void resetTarget() {
        // Random between 2 and 6 (inclusive)
        // nextInt(5) gives 0-4. +2 gives 2-6.
        targetGames = new Random().nextInt(5) + 2;
    }

    private static void showAdPopup(Context context, Runnable onDoubleMoney) {
        new AlertDialog.Builder(context)
                .setTitle("Double Money!")
                .setMessage("Watch an ad to double your earnings?")
                .setPositiveButton("Watch Ad", (dialog, which) -> {
                    // Simulate Ad watching
                    Toast.makeText(context, "Ad Watched! Money Doubled!", Toast.LENGTH_SHORT).show();
                    if (onDoubleMoney != null) {
                        onDoubleMoney.run();
                    }
                })
                .setNegativeButton("No Thanks", null)
                .setCancelable(false)
                .show();
    }
}
