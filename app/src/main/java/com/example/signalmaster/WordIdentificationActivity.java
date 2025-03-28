package com.example.signalmaster;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.graphics.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class WordIdentificationActivity extends AppCompatActivity {

    private ImageView letterImage, replayButton;
    private Button startButton;
    private TextView currentScoreText, highScoreText;
    private EditText wordInput; // Input field for user to type word
    private StringBuilder userInput = new StringBuilder(); // Stores user input dynamically
    private boolean isGameRunning = false; // Flag to track game state
    private String currentWord = "";
    private int score = 0; // Initialize score
    final private Handler handler = new Handler(); // Used for delay
    MediaPlayer startSound;
    MediaPlayer correctSound;
    private ArrayList<String> wordsList = new ArrayList<>();
    private int currentLetterIndex = 0;
    private int wordHighScore = 0; // Stores the high score
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_identification);

        // Hide navigation bar
        hideSystemUI();

        letterImage = findViewById(R.id.semaphoreImage);
        startButton = findViewById(R.id.playPauseButton);
        currentScoreText = findViewById(R.id.currentScoreText);
        wordInput = findViewById(R.id.wordInput); // EditText field
        replayButton = findViewById(R.id.replayButton);
        highScoreText = findViewById(R.id.highScoreText);

        // Load and display the high score
        wordHighScore = getHighScore("word_guessing");
        highScoreText.setText("High Score: " + wordHighScore);

        // Set initial image as "space"
        letterImage.setImageResource(R.drawable.space); // Ensure 'space.png' is in res/drawable
        currentScoreText.setText("Current Score: 0");

        // Load words from the text file
        loadWordsFromFile();
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
                    stopGame();
                } else {
                    // Start the game
                    startButton.setText("Pause");
                    startNewWord();
                    isGameRunning = true;
                }
            }
        });
        replayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentLetterIndex = 0;
                displayWordImages(); // Call function to restart line display
            }
        });
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
        if (correctSound != null) {
            correctSound.release();
            correctSound = null;
        }
    }

    private void stopGame() {
        isGameRunning = false; // Stop game state
        handler.removeCallbacksAndMessages(null); // Stop image display
        startButton.setText("Play"); // Reset play button text
        letterImage.setImageResource(R.drawable.space); // Reset image
        userInput.setLength(0); // Clear input
        wordInput.setText(generateUnderscores(currentWord.length())); // Reset word display
        currentScoreText.setText("Current Score: 0");
    }

    private void startNewWord() {
        getRandomWord();
        resetKeyboardColors(); // ✅ Reset keyboard key colors before new word
        userInput.setLength(0);
        wordInput.setText(generateUnderscores(currentWord.length()));
        currentLetterIndex = 0;
        displayWordImages();
    }

    private void resetKeyboardColors() {
        int[] letterIds = {
                R.id.key_a, R.id.key_b, R.id.key_c, R.id.key_d, R.id.key_e, R.id.key_f,
                R.id.key_g, R.id.key_h, R.id.key_i, R.id.key_j, R.id.key_k, R.id.key_l,
                R.id.key_m, R.id.key_n, R.id.key_o, R.id.key_p, R.id.key_q, R.id.key_r,
                R.id.key_s, R.id.key_t, R.id.key_u, R.id.key_v, R.id.key_w, R.id.key_x,
                R.id.key_y, R.id.key_z
        };

        for (int id : letterIds) {
            TextView letterView = findViewById(id);
            if (letterView != null) {
                letterView.setTextColor(Color.BLACK); // ✅ Reset text color to white
            }
        }
    }

    private void checkAnswer() {
        String guessedWord = userInput.toString().trim().toLowerCase();
        if (guessedWord.equals(currentWord)) {
            score++; // Increase score on correct answer
            saveTotalScore("word_guessing_total", score);

            if (score > wordHighScore) {
                wordHighScore = score;
                saveHighScore("word_guessing", wordHighScore);
                highScoreText.setText("High Score: " + wordHighScore);
            }
            playCorrectSound();
            currentScoreText.setText("Current Score: "+ score);
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
            // Delay for 2 seconds, then reset color and generate new letter
            handler.postDelayed(this::startNewWord, 3000);
        }
        else {
            wordInput.setTextColor(Color.RED); // ❌ Wrong answer turns red
            handler.postDelayed(() -> wordInput.setTextColor(Color.BLACK), 1000);
        }
    }

    private void saveTotalScore(String gameMode, int newScore) {
        SharedPreferences preferences = getSharedPreferences("TotalScores", MODE_PRIVATE);
        int currentTotal = preferences.getInt(gameMode, 0);
        int updatedTotal = currentTotal + newScore; // Add new score to total

            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(gameMode, updatedTotal);
            editor.apply();
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

    private String generateUnderscores(int length) {
        return "_ ".repeat(length).trim();
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
                R.id.key_y, R.id.key_z, R.id.key_backspace // Backspace key
        };

        for (int id : letterIds) {
            TextView letterView = findViewById(id);
            if (letterView != null) {
                letterView.setOnClickListener(view -> {
                    if (!isGameRunning) {
                        Toast.makeText(this, "Press Play to start!", Toast.LENGTH_SHORT).show();
                        return; // Stop processing input if game is not running
                    }
                    String selectedLetter = ((TextView) view).getText().toString().toLowerCase();;
                    if (selectedLetter.equals("←")) { // If Backspace is clicked
                        if (userInput.length() > 0) {
                            userInput.deleteCharAt(userInput.length() - 1);
                        }
                    }else {
                        if (userInput.length() < currentWord.length()) { // ✅ Limit input to word length
                            userInput.append(selectedLetter.toLowerCase());
                        }
                    }

                    updateDisplayedWord();

                    if (userInput.length() == currentWord.length()) {
                        checkAnswer(); // Call checkAnswer when word is complete
                    }
                    updateKeyboardColors(selectedLetter);
                });
            }
        }
    }

    private void updateDisplayedWord() {
        StringBuilder displayText = new StringBuilder();
        for (int i = 0; i < currentWord.length(); i++) {
            if (i < userInput.length()) {
                displayText.append(userInput.charAt(i)).append(" "); // Show guessed letter
            } else {
                displayText.append("_ "); // Keep underscores for unguessed letters
            }
        }
        wordInput.setText(displayText.toString().trim().toUpperCase()); // Update EditText view
    }


    private void updateKeyboardColors(String letter) {
        int keyId = getResources().getIdentifier("key_" + letter, "id", getPackageName());
        TextView keyView = findViewById(keyId);
        if (keyView != null) {
            if (currentWord.contains(letter)) {
                keyView.setTextColor(Color.GREEN);
                handler.postDelayed(() -> keyView.setTextColor(Color.BLACK), 1000);
            } else {
                keyView.setTextColor(Color.RED);
                handler.postDelayed(() -> keyView.setTextColor(Color.BLACK), 1000);
            }
        }
    }

    private void loadWordsFromFile() {
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.words);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                wordsList.add(line.trim().toLowerCase()); // Store words in lowercase
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getRandomWord() {
        Random random = new Random();
        int index = random.nextInt(wordsList.size());
        currentWord = wordsList.get(index); // Select a random word
    }

    private void displayWordImages() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (currentLetterIndex < currentWord.length()) {
                    char letter = currentWord.charAt(currentLetterIndex);
                    int imageId = getResources().getIdentifier(String.valueOf(letter), "drawable", getPackageName());
                    letterImage.setImageResource(imageId);
                    currentLetterIndex++;
                    handler.postDelayed(this, 3000); // 3-second delay for each letter
                }else {
                    // After displaying all letters, show space image
                    letterImage.setImageResource(R.drawable.space);
                }
            }
        }, 3000);
    }
}
