package com.example.signalmaster;

import android.media.MediaPlayer;
import android.graphics.Color;
import java.util.Random;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;

public class LetterIdentificationActivity extends AppCompatActivity {

    private ImageView letterImage;
    private Button startButton;
    private TextView currentScoreText, highScoreText;
    private boolean isGameRunning = false; // Flag to track game state
    private char currentLetter;
    private int score = 0; // Initialize score
    final private Handler handler = new Handler(); // Used for delay
    private MediaPlayer mediaPlayer; // Sound player
    MediaPlayer startSound;
    MediaPlayer correctSound;
    private int letterHighScore = 0; // Stores the high score

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_letter_identification);

        // Hide navigation bar
        hideSystemUI();

        letterImage = findViewById(R.id.semaphoreImage);
        startButton = findViewById(R.id.playPauseButton);
        currentScoreText = findViewById(R.id.currentScoreText);
        highScoreText = findViewById(R.id.highScoreText);

        // Load and display the high score
        letterHighScore = getHighScore("letter_guessing");
        highScoreText.setText("High Score: " + letterHighScore);

        // Set initial image as "space"
        letterImage.setImageResource(R.drawable.space); // Ensure 'space.png' is in res/drawable
        currentScoreText.setText("Current Score: 0");
        setupKeyboardListeners();

        // Initialize MediaPlayer with sound effect
        startSound = MediaPlayer.create(this, R.raw.start);
        correctSound = MediaPlayer.create(this, R.raw.correct_answer);

        // Start Game Button Click
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playStartSound(); // Play click sound
                if (isGameRunning) {
                    // Pause the game
                    startButton.setText("Play");
                    letterImage.setImageResource(R.drawable.space); // Show space image again
                } else {
                    // Start the game
                    startButton.setText("Pause");
                    char randomLetter = getRandomLetter(); // Generate a random letter
                    int imageId = getResources().getIdentifier(String.valueOf(randomLetter), "drawable", getPackageName());
                    letterImage.setImageResource(imageId); // Set new image
                }
                isGameRunning = !isGameRunning; // Toggle game state
            }
        });
    }
    protected void onResume() {
        super.onResume();
        hideSystemUI();
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
    private void playStartSound() {
        if (startSound != null) {
            startSound.start(); // Play sound
        }
    }
    private void playCorrectSound() {
        if (correctSound != null) {
            correctSound.start(); // Play sound
        }
    }
    protected void onDestroy() {
        super.onDestroy();
        if (startSound != null) {
            startSound.release(); // Release resources
            startSound = null;
        }
    }

    public char getRandomLetter() {
        Random random = new Random();
        char randomLetter = (char) ('a' + random.nextInt(26)); // Generates a random lowercase letter
        currentLetter = randomLetter; // ✅ Store it for validation
        return randomLetter;
    }

    private void checkAnswer(final TextView selectedTextView, String selectedLetter) {
        if (selectedLetter.equalsIgnoreCase(String.valueOf(currentLetter))) {
            score++; // Increase score on correct answer

            if (score > letterHighScore) {
                letterHighScore = score;
                saveHighScore("letter_guessing", letterHighScore);
                highScoreText.setText("High Score: " + letterHighScore);
            }
            playCorrectSound();
            currentScoreText.setText("Current Score: "+ score);
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
            selectedTextView.setTextColor(Color.GREEN); // ✅ Turn text green if correct
            // Delay for 2 seconds, then reset color and generate new letter
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    selectedTextView.setTextColor(Color.BLACK); // Reset color
                    getRandomLetter(); // Generate new letter
                    updateLetterImage(); // ✅ Call updateLetterImage() to change the displayed letter
                }
            }, 2000);
        }
        else {
            selectedTextView.setTextColor(Color.RED); // ❌ Wrong answer turns red
            // Reset color after a short delay
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    selectedTextView.setTextColor(Color.BLACK);
                }
            }, 1000);
        }
    }

    private void saveHighScore(String gameMode, int newScore) {
        SharedPreferences preferences = getSharedPreferences("HighScores", MODE_PRIVATE);
        int highScore = preferences.getInt(gameMode, 0);

        if (newScore > highScore) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(gameMode, newScore);
            editor.apply();
        }
    }

    private int getHighScore(String gameMode) {
        SharedPreferences preferences = getSharedPreferences("HighScores", MODE_PRIVATE);
        return preferences.getInt(gameMode, 0);
    }

    private void updateLetterImage() {
        int imageId = getResources().getIdentifier(String.valueOf(currentLetter), "drawable", getPackageName());
        letterImage.setImageResource(imageId); // ✅ Update ImageView with the new letter
    }

    private void setupKeyboardListeners() {
        int[] letterIds = {
                R.id.key_a, R.id.key_b, R.id.key_c,
                R.id.key_d, R.id.key_e, R.id.key_f,
                R.id.key_g, R.id.key_h, R.id.key_i,
                R.id.key_j, R.id.key_k, R.id.key_l,
                R.id.key_m, R.id.key_n, R.id.key_o,
                R.id.key_p, R.id.key_q, R.id.key_r,
                R.id.key_s, R.id.key_t, R.id.key_u,
                R.id.key_v, R.id.key_w, R.id.key_x,
                R.id.key_y, R.id.key_z
        };

        for (int id : letterIds) {
            TextView letterView = findViewById(id);
            if (letterView != null) {
                letterView.setOnClickListener(view -> {
                    if (!isGameRunning) {
                        Toast.makeText(this, "Press Play to start!", Toast.LENGTH_SHORT).show();
                        return; // Stop processing input if game is not running
                    }
                    String selectedLetter = ((TextView) view).getText().toString();
                    checkAnswer((TextView) view, selectedLetter); // Validate the answer
                });
            }
        }
    }
}
