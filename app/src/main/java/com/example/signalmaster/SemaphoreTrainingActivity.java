package com.example.signalmaster;

import android.os.Bundle;
import android.view.View;
import androidx.gridlayout.widget.GridLayout;

import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import android.view.Gravity;



public class SemaphoreTrainingActivity extends AppCompatActivity {

    private final int[] semaphoreImages = {
            R.drawable.a, R.drawable.b, R.drawable.c, R.drawable.d, R.drawable.e,
            R.drawable.f, R.drawable.g, R.drawable.h, R.drawable.i, R.drawable.j,
            R.drawable.k, R.drawable.l, R.drawable.m, R.drawable.n, R.drawable.o,
            R.drawable.p, R.drawable.q, R.drawable.r, R.drawable.s, R.drawable.t,
            R.drawable.u, R.drawable.v, R.drawable.w, R.drawable.x, R.drawable.y,
            R.drawable.z, R.drawable.space
    };

    private final String[] semaphoreLetters = {
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
            "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
            "U", "V", "W", "X", "Y", "Z", "space"
    };

    private int currentImageIndex = 0;
    private ImageView selectedImage;
    private TextView selectedLetter;
    private CardView popUpContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_semaphore_training);

        // Hide navigation bar
        hideSystemUI();

        GridLayout semaphoreGrid = findViewById(R.id.semaphoreGrid);
        popUpContainer = findViewById(R.id.popUpContainer);
        selectedImage = findViewById(R.id.selectedSemaphoreImage);
        selectedLetter = findViewById(R.id.selectedSemaphoreLetter);
        ImageButton closeButton = findViewById(R.id.closeButton);
        ImageButton prevButton = findViewById(R.id.prevButton);
        ImageButton nextButton = findViewById(R.id.nextButton);

        int columns = 3;
        semaphoreGrid.setColumnCount(columns);

        // Calculate image size based on screen width
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int imageSize = screenWidth / columns - 40; // margin offset

        // Dynamically add images and letters to the grid
        for (int i = 0; i < semaphoreImages.length; i++) {
            final int index = i;

            // Create a container for each image-text pair
            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.VERTICAL);
            itemLayout.setGravity(Gravity.CENTER);
            itemLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            itemLayout.setPadding(10, 10, 10, 10);

            // Image setup
            ImageView imageView = new ImageView(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = imageSize;
            params.height = imageSize;
            params.setMargins(10, 10, 10, 10);
            imageView.setLayoutParams(params);
            imageView.setImageResource(semaphoreImages[i]);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setAdjustViewBounds(true);
            imageView.setOnClickListener(v -> showPopup(index));

            // Text setup
            TextView textView = new TextView(this);
            textView.setText(semaphoreLetters[i]);
            textView.setTextSize(16);
            textView.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

            // Add image and text to the container
            itemLayout.addView(imageView);
            itemLayout.addView(textView);

            // Add container to the GridLayout
            semaphoreGrid.addView(itemLayout);
        }

        // Close button
        closeButton.setOnClickListener(v -> popUpContainer.setVisibility(View.GONE));

        // Previous Button
        prevButton.setOnClickListener(v -> navigateImage(-1));

        // Next Button
        nextButton.setOnClickListener(v -> navigateImage(1));
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
    private void showPopup(int index) {
        currentImageIndex = index;
        selectedImage.setImageResource(semaphoreImages[index]);
        selectedLetter.setText(semaphoreLetters[index]);
        popUpContainer.setVisibility(View.VISIBLE);
    }
    private void navigateImage(int direction) {
        currentImageIndex += direction;
        if (currentImageIndex < 0) currentImageIndex = semaphoreImages.length - 1;
        if (currentImageIndex >= semaphoreImages.length) currentImageIndex = 0;

        selectedImage.setImageResource(semaphoreImages[currentImageIndex]);
        selectedLetter.setText(semaphoreLetters[currentImageIndex]);
    }
}
