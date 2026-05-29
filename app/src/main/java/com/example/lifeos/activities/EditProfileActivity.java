package com.example.lifeos.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.lifeos.R;
import com.example.lifeos.interfaces.FirebaseCallback;
import com.example.lifeos.interfaces.SimpleCallback;
import com.example.lifeos.models.User;
import com.example.lifeos.repositories.AuthRepository;
import com.example.lifeos.repositories.UserRepository;
import com.example.lifeos.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class EditProfileActivity extends AppCompatActivity {

    private UserRepository userRepository;
    private String userId;
    private Uri profileUri;
    private Uri coverUri;

    private final ActivityResultLauncher<String> pickProfile =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    profileUri = uri;
                    Glide.with(this).load(uri).circleCrop().into((ImageView) findViewById(R.id.imgProfile));
                }
            });

    private final ActivityResultLauncher<String> pickCover =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    coverUri = uri;
                    Glide.with(this).load(uri).into((ImageView) findViewById(R.id.imgCover));
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        userRepository = new UserRepository();
        AuthRepository auth = new AuthRepository();
        if (auth.getCurrentUser() == null) {
            finish();
            return;
        }
        userId = auth.getCurrentUser().getUid();

        ImageView imgProfile = findViewById(R.id.imgProfile);
        ImageView imgCover = findViewById(R.id.imgCover);
        TextInputEditText etName = findViewById(R.id.etName);
        TextInputEditText etUsername = findViewById(R.id.etUsername);
        TextInputEditText etBio = findViewById(R.id.etBio);
        MaterialButton btnSave = findViewById(R.id.btnSave);
        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        ProgressBar progressBar = findViewById(R.id.progressBar);

        imgProfile.setOnClickListener(v -> pickProfile.launch("image/*"));
        imgCover.setOnClickListener(v -> pickCover.launch("image/*"));

        btnLogout.setOnClickListener(v -> {
            new AuthRepository().logout(this);
            new SessionManager(this).clear();
            Intent intent = new Intent(this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        userRepository.getUser(userId, new FirebaseCallback<User>() {
            @Override
            public void onSuccess(User user) {
                etName.setText(user.getName());
                etUsername.setText(user.getUsername());
                etBio.setText(user.getBio());
                Glide.with(EditProfileActivity.this).load(user.getProfilePhoto())
                        .circleCrop().placeholder(R.drawable.ic_logo).into(imgProfile);
                Glide.with(EditProfileActivity.this).load(user.getCoverPhoto())
                        .placeholder(R.drawable.bg_card_glass).into(imgCover);
            }
            @Override
            public void onError(String message) {
                Toast.makeText(EditProfileActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });

        btnSave.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            String name = etName.getText() != null ? etName.getText().toString().trim() : "";
            String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
            String bio = etBio.getText() != null ? etBio.getText().toString().trim() : "";

            Runnable saveProfile = () -> userRepository.updateProfile(userId, name, username, bio,
                    new SimpleCallback() {
                        @Override
                        public void onSuccess() {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(EditProfileActivity.this, "Profile saved", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                        @Override
                        public void onError(String message) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(EditProfileActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    });

            if (profileUri != null) {
                userRepository.uploadProfilePhoto(userId, profileUri, new FirebaseCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        if (coverUri != null) uploadCoverThenSave(saveProfile);
                        else saveProfile.run();
                    }
                    @Override
                    public void onError(String message) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(EditProfileActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
            } else if (coverUri != null) {
                uploadCoverThenSave(saveProfile);
            } else {
                saveProfile.run();
            }
        });
    }

    private void uploadCoverThenSave(Runnable thenSave) {
        userRepository.uploadCoverPhoto(userId, coverUri, new FirebaseCallback<String>() {
            @Override
            public void onSuccess(String result) {
                thenSave.run();
            }
            @Override
            public void onError(String message) {
                Toast.makeText(EditProfileActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
