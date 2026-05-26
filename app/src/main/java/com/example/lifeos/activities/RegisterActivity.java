package com.example.lifeos.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.lifeos.MainActivity;
import com.example.lifeos.R;
import com.example.lifeos.utils.SessionManager;
import com.example.lifeos.utils.ValidationUtils;
import com.example.lifeos.viewmodels.AuthViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        AuthViewModel viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        SessionManager sessionManager = new SessionManager(this);

        TextInputEditText etName = findViewById(R.id.etName);
        TextInputEditText etUsername = findViewById(R.id.etUsername);
        TextInputEditText etEmail = findViewById(R.id.etEmail);
        TextInputEditText etPassword = findViewById(R.id.etPassword);
        TextInputEditText etConfirm = findViewById(R.id.etConfirm);
        MaterialButton btnRegister = findViewById(R.id.btnRegister);
        TextView tvLogin = findViewById(R.id.tvLogin);
        ProgressBar progressBar = findViewById(R.id.progressBar);

        btnRegister.setOnClickListener(v -> {
            String name = text(etName);
            String username = text(etUsername);
            String email = text(etEmail);
            String password = text(etPassword);
            String confirm = text(etConfirm);

            if (name.isEmpty()) {
                Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!ValidationUtils.isValidUsername(username)) {
                Toast.makeText(this, "Username: 3-20 chars, letters, numbers, underscore",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (!ValidationUtils.isValidEmail(email)) {
                Toast.makeText(this, "Invalid email", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!ValidationUtils.isValidPassword(password)) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!ValidationUtils.passwordsMatch(password, confirm)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.register(name, username, email, password);
        });

        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        viewModel.getLoading().observe(this, loading ->
                progressBar.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE));
        viewModel.getError().observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });
        viewModel.getUser().observe(this, user -> onAuthSuccess(user, sessionManager));
    }

    private void onAuthSuccess(FirebaseUser user, SessionManager sessionManager) {
        if (user == null) return;
        sessionManager.setRememberLogin(true);
        sessionManager.setUserId(user.getUid());
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private static String text(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}
