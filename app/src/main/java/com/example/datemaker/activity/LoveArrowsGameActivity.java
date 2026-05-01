package com.example.datemaker.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.datemaker.databinding.ActivityLoveArrowsGameBinding;
import com.example.datemaker.game.LoveArrowsGameView;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

public class LoveArrowsGameActivity extends AppCompatActivity {

    private ActivityLoveArrowsGameBinding binding;
    private int arrows = 10;
    private int score = 0;
    private int timeLeft = 30;
    private CountDownTimer timer;

    private static final int REQ_CAMERA = 101;

    // Sensors
    private SensorManager sensorManager;
    private Sensor rotationSensor;
    private SensorEventListener rotationListener;
    private float baseAzimuth = Float.NaN;
    private float basePitch = Float.NaN;

    // Filtered orientation
    private float filteredAzimuth = 0f;
    private float filteredPitch = 0f;
    private boolean first = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoveArrowsGameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        LoveArrowsGameView gameView = binding.gameView;

        // Listen for score & arrows from the game view
        gameView.setGameListener(new LoveArrowsGameView.GameListener() {
            @Override
            public void onScoreChanged(int newScore) {
                // Ensure UI update on main thread
                runOnUiThread(() -> {
                    score = newScore;
                    binding.tvScore.setText("Score: " + score);
                });
            }

            @Override
            public void onArrowUsed(int remaining) {
                // Ensure UI update on main thread
                runOnUiThread(() -> {
                    arrows = remaining;
                    binding.tvArrows.setText("Arrows: " + arrows);

                    if (remaining <= 0) {
                        endGame();
                    }
                });
            }
        });

        // Sensor setup
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        rotationListener = new SensorEventListener() {
            final float[] rotationMatrix = new float[9];
            final float[] orientation = new float[3];

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {}

            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                if (sensorEvent.sensor.getType() != Sensor.TYPE_ROTATION_VECTOR) return;

                SensorManager.getRotationMatrixFromVector(rotationMatrix, sensorEvent.values);
                SensorManager.getOrientation(rotationMatrix, orientation);

                float azimuth = orientation[0]; // yaw
                float pitch   = orientation[1]; // pitch

                // Initialize filter with first values
                if (first) {
                    filteredAzimuth = azimuth;
                    filteredPitch   = pitch;
                    first = false;
                }

                float alpha = 0.25f;
                filteredAzimuth = filteredAzimuth * (1 - alpha) + azimuth * alpha;
                filteredPitch   = filteredPitch   * (1 - alpha) + pitch   * alpha;

                // Initialize base orientation once (our "zero" direction)
                if (Float.isNaN(baseAzimuth)) {
                    baseAzimuth = filteredAzimuth;
                    basePitch   = filteredPitch;
                }

                float relativeAzimuth = filteredAzimuth - baseAzimuth;
                float relativePitch   = filteredPitch   - basePitch;

                gameView.setDeviceOrientation(relativeAzimuth, relativePitch);
            }
        };

        // Camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQ_CAMERA);
        } else {
            startCamera();
        }

        startTimer();
    }

    private void startTimer() {
        timer = new CountDownTimer(30000, 1000) {
            @Override
            public void onFinish() {
                endGame();
            }

            @Override
            public void onTick(long l) {
                timeLeft = (int) (l / 1000);
                binding.tvTimer.setText(timeLeft + "s");
            }
        };

        timer.start();
    }

    private void endGame() {
        if (timer != null) timer.cancel();

        int coins = score / 10;

        Intent intent = new Intent(this, LoveArrowsResultActivity.class);
        intent.putExtra("score", score);
        intent.putExtra("coins", coins);
        intent.putExtra("watchedDouble", false);
        startActivity(intent);
        finish();
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

        preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());

        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (rotationSensor != null) {
            sensorManager.registerListener(
                    rotationListener,
                    rotationSensor,
                    SensorManager.SENSOR_DELAY_GAME
            );
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(rotationListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CAMERA &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            Toast.makeText(this, "Camera permission is required to play Love Arrows.", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}