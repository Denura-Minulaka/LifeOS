package com.example.lifeos.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lifeos.MainActivity;
import com.example.lifeos.R;
import com.example.lifeos.repositories.AuthRepository;
import com.example.lifeos.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY = 2000L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(this::checkAuth, SPLASH_DELAY);
    }

    private void checkAuth() {
        AuthRepository authRepository = new AuthRepository();
        SessionManager session = new SessionManager(this);

        if (authRepository.isLoggedIn() && session.isRememberLogin()) {
            startActivity(new Intent(this, MainActivity.class));
        } else {
            startActivity(new Intent(this, WelcomeActivity.class));
        }
        finish();
    }
}
