// activities/SplashActivity.java

package com.balilihan.mdrrmo.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.balilihan.mdrrmo.R;
import com.balilihan.mdrrmo.utils.SessionManager;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 1500; // 1.5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SessionManager session = SessionManager.getInstance(this);

            if (session.isLoggedIn()) {
                // Token exists — go straight to main app
                startActivity(new Intent(this, MainActivity.class));
            } else {
                // No token — user must log in
                startActivity(new Intent(this, LoginActivity.class));
            }

            // Close splash so user can't navigate back to it
            finish();

        }, SPLASH_DELAY);
    }
}