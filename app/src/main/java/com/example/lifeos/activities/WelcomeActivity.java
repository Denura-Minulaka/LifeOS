package com.example.lifeos.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lifeos.R;
import com.google.android.material.button.MaterialButton;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        MaterialButton btnGoogle = findViewById(R.id.btnGoogle);
        MaterialButton btnEmail = findViewById(R.id.btnEmail);
        MaterialButton btnLogin = findViewById(R.id.btnLogin);

        btnGoogle.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)
                        .putExtra(LoginActivity.EXTRA_GOOGLE, true)));
        btnEmail.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
        btnLogin.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));
    }
}
