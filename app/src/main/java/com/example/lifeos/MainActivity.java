package com.example.lifeos;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.lifeos.fragments.CreatePostFragment;
import com.example.lifeos.fragments.ExploreFragment;
import com.example.lifeos.fragments.HomeFragment;
import com.example.lifeos.fragments.ProfileFragment;
import com.example.lifeos.fragments.QuestsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment;
            int id = item.getItemId();
            if (id == R.id.nav_home) fragment = new HomeFragment();
            else if (id == R.id.nav_explore) fragment = new ExploreFragment();
            else if (id == R.id.nav_create) fragment = new CreatePostFragment();
            else if (id == R.id.nav_quests) fragment = new QuestsFragment();
            else if (id == R.id.nav_profile) fragment = new ProfileFragment();
            else return false;
            showFragment(fragment);
            return true;
        });

        if (savedInstanceState == null) {
            int targetTab = getIntent().getIntExtra("SELECT_TAB", R.id.nav_home);
            bottomNav.setSelectedItemId(targetTab);
        }
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        int targetTab = intent.getIntExtra("SELECT_TAB", -1);
        if (targetTab != -1) {
            BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
            bottomNav.setSelectedItemId(targetTab);
        }
    }
}
