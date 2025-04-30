package com.example.signalmaster;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Signup extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseFirestore firestore;
    EditText UserName, Password, Email, ConfirmPassword;
    Button Submit;
    ImageView backbutton;
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Hide navigation bar
        hideSystemUI();

        Email = findViewById(R.id.email);
        UserName = findViewById(R.id.userName);
        Password = findViewById(R.id.password);
        ConfirmPassword = findViewById(R.id.confirmpassword);
        Submit = findViewById(R.id.Submit);

        Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = Email.getText().toString();
                String username = UserName.getText().toString();
                String password = Password.getText().toString();
                String confirmpassword = ConfirmPassword.getText().toString();

                if (email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmpassword.isEmpty()) {
                    Toast.makeText(Signup.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 📧 Email Validation
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(Signup.this, "Enter a valid email address", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 🔒 Password Checks
                if (password.length() < 8) {
                    Toast.makeText(Signup.this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!password.equals(confirmpassword)) {
                    Toast.makeText(Signup.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 🌐 Internet Check
                if (!isConnected()) {
                    Toast.makeText(Signup.this, "No internet connection", Toast.LENGTH_SHORT).show();
                    return;
                }

                // ⏳ Show Progress
                ProgressDialog progressDialog = new ProgressDialog(Signup.this);
                progressDialog.setMessage("Registering...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            progressDialog.dismiss();
                            if (task.isSuccessful()) {
                                FirebaseUser user = auth.getCurrentUser();
                                if (user != null) {
                                    user.sendEmailVerification().addOnCompleteListener(verifyTask -> {
                                        if (verifyTask.isSuccessful()) {
                                            Toast.makeText(Signup.this, "Verification email sent! Please verify and wait...", Toast.LENGTH_LONG).show();
                                            // Start checking for verification
                                            waitForEmailVerification(user, username, email, password);
                                        } else {
                                            Toast.makeText(Signup.this, "Verification email failed to send!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            } else {
                                Toast.makeText(Signup.this, "Signup Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        backbutton = findViewById(R.id.backbutton);
        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Signup.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
    private void waitForEmailVerification(FirebaseUser user, String username, String email, String password) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                user.reload().addOnCompleteListener(task -> {
                    if (user.isEmailVerified()) {

                        String uid = user.getUid();
                        String encryptedPassword = hashPassword(password);
                        // Save to Firestore only if verified
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("UserName", username);
                        userMap.put("Email", email);
                        userMap.put("Password", encryptedPassword);

                        firestore.collection("users")
                                .document(uid)
                                .set(userMap)
                                .addOnSuccessListener(documentReference -> {
                                    DatabaseHelper dbHelper = new DatabaseHelper(Signup.this);
                                    boolean success = dbHelper.addUser(username);
                                    if (success) {
                                        Toast.makeText(Signup.this, "Registration complete!", Toast.LENGTH_LONG).show();
                                        SharedPreferences prefs = getSharedPreferences("SignalMasterPrefs", MODE_PRIVATE);
                                        prefs.edit().putBoolean("isSignedup", true).apply();
                                        prefs.edit().putString("username", username).apply();
                                        // Go to MainActivity or next screen
                                        Intent intent = new Intent(Signup.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(Signup.this, "Failed to store user data!", Toast.LENGTH_SHORT).show();
                                });


                    } else {
                        // Still not verified, check again after 5 seconds
                        handler.postDelayed(this, 5000);
                    }
                });
            }
        }, 5000);
    }
    public String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
    // 📶 Check Internet Connectivity
    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo net = cm.getActiveNetworkInfo();
        return net != null && net.isConnected();
    }

    @Override
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
}