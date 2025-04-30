package com.example.signalmaster;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.constraintlayout.widget.ConstraintLayout;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private ImageView semaphoreImage, lockIcon, menuIcon, backbtn, setting;
    ConstraintLayout sidebar, settingbar;
    boolean isSidebarOpen = false;
    boolean isSettingbarOpen = false;
    TextView loginBtn, logoutBtn, profile, leaderBoard;
    int wordScore;
    TextView ratetheapp;
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

        // Hide navigation bar
        hideSystemUI();

        // Initialize UI elements
        semaphoreImage = findViewById(R.id.semaphoreImage);
        Button btnLearn = findViewById(R.id.btnLearn);
        Button btnLetterGuess = findViewById(R.id.btnLetterGuess);
        Button btnWordGuess = findViewById(R.id.btnWordGuess);
        Button btnLineGuess = findViewById(R.id.btnLineGuess);
        lockIcon = findViewById(R.id.lockIcon); // ✅ Add this line

        // Start alternating images
        startImageAnimation();

        // Button click actions (Example: Show Toast for now)
        btnLearn.setOnClickListener(v -> openSemaphoreTrainingActivity());
        btnLetterGuess.setOnClickListener(v -> openLetterIdentificationActivity());
        btnWordGuess.setOnClickListener(v -> openWordIdentificationActivity());
        btnLineGuess.setOnClickListener(v -> openLineIdentificationActivity());
        updateLockState();


        menuIcon = findViewById(R.id.menu);
        sidebar = findViewById(R.id.sidebar);
        backbtn = findViewById(R.id.backbtn);
        settingbar = findViewById(R.id.settingbar);
        setting = findViewById(R.id.setting);
        ratetheapp = findViewById(R.id.ratetheapp);
        loginBtn = findViewById(R.id.loginBtn);
        logoutBtn = findViewById(R.id.logoutBtn);

        profile = findViewById(R.id.profile);
        leaderBoard = findViewById(R.id.leaderBoard);

        SharedPreferences prefs = getSharedPreferences("SignalMasterPrefs", MODE_PRIVATE);

        leaderBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isSignedup = prefs.getBoolean("isSignedup", false);

                if (isSignedup) {
                    // ✅ Username exists — go to ProfileActivity (or wherever)
                    Intent intent = new Intent(MainActivity.this, LeaderBoard.class);
                    startActivity(intent);
                } else {
                    // ❌ No username — show toast
                    Toast.makeText(MainActivity.this, "Please log in first.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isSignedup = prefs.getBoolean("isSignedup", false);

                if (isSignedup) {
                    // ✅ Username exists — go to ProfileActivity (or wherever)
                    Intent intent = new Intent(MainActivity.this, Profile.class);
                    startActivity(intent);
                } else {
                    // ❌ No username — show toast
                    Toast.makeText(MainActivity.this, "Please log in first.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Login.class);
                startActivity(intent);
            }
        });
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prefs.edit().putString("username", "").apply();

                loginBtn.setVisibility(View.VISIBLE);
                logoutBtn.setVisibility(View.GONE);
            }
        });
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isSettingbarOpen) {
                    settingbar.setVisibility(View.VISIBLE);
                    settingbar.animate().translationX(0).setDuration(300);
                    isSettingbarOpen = true;
                } else if (isSettingbarOpen) {
                    settingbar.animate().translationX(-settingbar.getWidth()).setDuration(300)
                            .withEndAction(() -> {
                                settingbar.setVisibility(View.GONE);
                            });
                    isSettingbarOpen = false;
                } else {
                    settingbar.animate().translationX(-settingbar.getWidth()).setDuration(300)
                            .withEndAction(() -> {
                                settingbar.setVisibility(View.GONE);
                            });
                    isSettingbarOpen = false;
                }
            }
        });
        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isSidebarOpen) {
                    sidebar.setVisibility(View.VISIBLE);
                    boolean isSignedup = prefs.getBoolean("isSignedup", false);
                    if (isSignedup) {
                        loginBtn.setVisibility(View.GONE);
                        logoutBtn.setVisibility(View.VISIBLE);
                    }else {
                        loginBtn.setVisibility(View.VISIBLE);
                        logoutBtn.setVisibility(View.GONE);
                    }
                    sidebar.animate().translationX(0).setDuration(300);
                    isSidebarOpen = true;
                    btnLearn.setClickable(false);
                    btnLetterGuess.setClickable(false);
                    btnWordGuess.setClickable(false);
                    btnLineGuess.setClickable(false);
                } else {
                    sidebar.animate().translationX(-sidebar.getWidth()).setDuration(300)
                            .withEndAction(() -> {
                                sidebar.setVisibility(View.GONE);
                            });
                    isSidebarOpen = false;
                    btnLearn.setClickable(true);
                    btnLetterGuess.setClickable(true);
                    btnWordGuess.setClickable(true);
                    btnLineGuess.setClickable(true);
                }
            }
        });
        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSidebarOpen) {
                    sidebar.animate().translationX(-sidebar.getWidth()).setDuration(300)
                            .withEndAction(() -> {
                                sidebar.setVisibility(View.GONE);
                            });
                    isSidebarOpen = false;
                    btnLearn.setClickable(true);
                    btnLetterGuess.setClickable(true);
                    btnWordGuess.setClickable(true);
                    btnLineGuess.setClickable(true);
                } else {
                    sidebar.setVisibility(View.VISIBLE);
                    sidebar.animate().translationX(0).setDuration(300);
                    isSidebarOpen = true;
                    btnLearn.setClickable(false);
                    btnLetterGuess.setClickable(false);
                    btnWordGuess.setClickable(false);
                    btnLineGuess.setClickable(false);
                }
            }
        });

//        ratetheapp.setOnClickListener(v -> {
//            try {
//                // Open directly in Play Store app
//                startActivity(new Intent(Intent.ACTION_VIEW,
//                        Uri.parse("market://details?id=" + getPackageName())));
//            } catch (android.content.ActivityNotFoundException e) {
//                // If Play Store is not available, open in browser
//                startActivity(new Intent(Intent.ACTION_VIEW,
//                        Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
//            }
//        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
        if (checkAndUnlockDecodeLine()) {
            updateLockState();  // Call this only if unlocked
        }
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
            Toast.makeText(this, "Score 50+ in Word Guessing to unlock!\nYour current score is: "+wordScore, Toast.LENGTH_SHORT).show();
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
        wordScore = getWordScore();

        if (wordScore >= 5) {
            return true;
        } else {
            return false;
        }
    }
    private int getWordScore() {
        SharedPreferences prefs = getSharedPreferences("TotalScores", MODE_PRIVATE);
        return prefs.getInt("word_guessing_total", 0); // Default is 0
    }
    private void updateLockState() {
        wordScore = getWordScore();
        if (wordScore >= 3) {
            lockIcon.setVisibility(View.GONE);
        } else {
            lockIcon.setVisibility(View.VISIBLE);
        }
    }

}
