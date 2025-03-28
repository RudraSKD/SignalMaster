package com.example.signalmaster;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private ImageView semaphoreImage, lockIcon;
    final private Handler handler = new Handler();
    final private int[] images = {
            R.drawable.space, R.drawable.a, R.drawable.b, R.drawable.c,
            R.drawable.d, R.drawable.e, R.drawable.f, R.drawable.g,
            R.drawable.h, R.drawable.i, R.drawable.j, R.drawable.k,
            R.drawable.l, R.drawable.m, R.drawable.n, R.drawable.o,
            R.drawable.p, R.drawable.q, R.drawable.r, R.drawable.s,
            R.drawable.t, R.drawable.u, R.drawable.v, R.drawable.w,
            R.drawable.x, R.drawable.y, R.drawable.z, R.drawable.space};
    private int imageIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean isFirstTime = prefs.getBoolean("isFirstTime", true);  // Default = true

        if (isFirstTime) {
            // Redirect to OnboardingActivity for first-time users
            startActivity(new Intent(this, onBoardActivity.class));
            finish();  // Close MainActivity so it doesn't stay in the back stack

            // Update SharedPreferences so this doesn't happen again
            prefs.edit().putBoolean("isFirstTime", false).apply();
            return;  // Exit onCreate() early
        }

        setContentView(R.layout.activity_main); // Load UI only for returning users

        // Hide navigation bar
        hideSystemUI();

        // Initialize UI elements
        semaphoreImage = findViewById(R.id.semaphoreImage);
        lockIcon = findViewById(R.id.lockIcon);
        Button btnLearn = findViewById(R.id.btnLearn);
        Button btnLetterGuess = findViewById(R.id.btnLetterGuess);
        Button btnWordGuess = findViewById(R.id.btnWordGuess);
        Button btnLineGuess = findViewById(R.id.btnLineGuess);

        // Start alternating images
        startImageAnimation();

        // Button click actions (Example: Show Toast for now)
        btnLearn.setOnClickListener(v -> openSemaphoreTrainingActivity());
        btnLetterGuess.setOnClickListener(v -> openLetterIdentificationActivity());
        btnWordGuess.setOnClickListener(v -> openWordIdentificationActivity());
        btnLineGuess.setOnClickListener(v -> openLineIdentificationActivity());
    }

    // Function to hide navigation bar
    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
    }
    private void openLineIdentificationActivity() {
        if (checkAndUnlockDecodeLine()) { // Ensure the mode is unlocked before opening
            Intent intent = new Intent(MainActivity.this, LineIdentificationActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Score 50+ in Word Guessing to unlock!", Toast.LENGTH_SHORT).show();
        }
    }
    private void openWordIdentificationActivity() {
        Intent intent = new Intent(MainActivity.this, WordIdentificationActivity.class);
        startActivity(intent);
    }
    private void openSemaphoreTrainingActivity() {
        Intent intent = new Intent(MainActivity.this, SemaphoreTrainingActivity.class);
        startActivity(intent);
    }
    public void openLetterIdentificationActivity() {
        Intent intent = new Intent(MainActivity.this, LetterIdentificationActivity.class);
        startActivity(intent);
    }

    // Function to alternate semaphore images every 2 seconds
    private void startImageAnimation() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                imageIndex = (imageIndex + 1) % images.length;
                semaphoreImage.setImageResource(images[imageIndex]);
                if (!isFinishing()) {
                    handler.postDelayed(this, 1500);
                }
            }
        }, 2000);
    }
    private boolean checkAndUnlockDecodeLine() {
        int wordScore = getWordScore();
        Button decodeLineButton = findViewById(R.id.btnLineGuess);

        if (wordScore >= 5) {
            decodeLineButton.setEnabled(true);  // Unlock the mode
            decodeLineButton.setAlpha(1.0f);    // Make it fully visible
            lockIcon.setVisibility(View.GONE); // Remove lock icon
            return true; // Unlock the mode
        } else {
            decodeLineButton.setEnabled(false); // Keep it locked
            decodeLineButton.setAlpha(0.5f);    // Make button look disabled
            lockIcon.setVisibility(View.VISIBLE); // Show lock icon
            return false; // Keep it locked
        }
    }

    private int getWordScore() {
        SharedPreferences prefs = getSharedPreferences("GamePrefs", MODE_PRIVATE);
        return prefs.getInt("word_guessing_total", 0); // Default is 0
    }

}
