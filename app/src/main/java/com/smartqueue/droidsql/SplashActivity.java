package com.smartqueue.droidsql;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Splash Screen Activity.
 * Displays logo for 2 seconds then launches MainActivity.
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Hide ActionBar if present
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Delay for 2 seconds then start main activity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Close splash activity so back button doesn't return to it
        }, 2000);
    }
}
