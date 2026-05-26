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
        if (savedInstanceState == null) {
            showFragment(new HomeFragment());
            bottomNav.setSelectedItemId(R.id.nav_home);
        }

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
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
