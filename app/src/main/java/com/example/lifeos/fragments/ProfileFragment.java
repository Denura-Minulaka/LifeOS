package com.example.lifeos.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lifeos.R;
import com.example.lifeos.activities.EditProfileActivity;
import com.example.lifeos.activities.WelcomeActivity;
import com.example.lifeos.adapters.CategoryChipAdapter;
import com.example.lifeos.adapters.UserPostGridAdapter;
import com.example.lifeos.models.Category;
import com.example.lifeos.models.User;
import com.example.lifeos.repositories.AuthRepository;
import com.example.lifeos.utils.CategorySelector;
import com.example.lifeos.utils.SessionManager;
import com.example.lifeos.viewmodels.ProfileViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private ProfileViewModel viewModel;
    private UserPostGridAdapter gridAdapter;
    private UserPostGridAdapter pinnedAdapter;
    private final List<Category> selectedCategoriesForFilter = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        AuthRepository auth = new AuthRepository();
        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();

        ImageView imgCover = view.findViewById(R.id.imgCover);
        ImageView imgProfile = view.findViewById(R.id.imgProfile);
        TextView tvName = view.findViewById(R.id.tvName);
        TextView tvUsername = view.findViewById(R.id.tvUsername);
        TextView tvBio = view.findViewById(R.id.tvBio);
        TextView tvXp = view.findViewById(R.id.tvXp);
        TextView tvLevel = view.findViewById(R.id.tvLevel);
        TextView tvFollowers = view.findViewById(R.id.tvFollowers);
        TextView tvFollowing = view.findViewById(R.id.tvFollowing);
        MaterialButton btnEdit = view.findViewById(R.id.btnEditProfile);
        MaterialButton btnSelectCategories = view.findViewById(R.id.btnSelectCategories);
        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        TextView tvPinnedLabel = view.findViewById(R.id.tvPinnedLabel);
        RecyclerView recyclerFilter = view.findViewById(R.id.recyclerCategoryFilter);
        RecyclerView recyclerPinned = view.findViewById(R.id.recyclerPinned);
        RecyclerView recyclerPosts = view.findViewById(R.id.recyclerPosts);

        gridAdapter = new UserPostGridAdapter();
        pinnedAdapter = new UserPostGridAdapter();
        recyclerPosts.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        recyclerPosts.setAdapter(gridAdapter);
        recyclerPinned.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerPinned.setAdapter(pinnedAdapter);

        tabLayout.addTab(tabLayout.newTab().setText("All"));
        tabLayout.addTab(tabLayout.newTab().setText("Photos"));
        tabLayout.addTab(tabLayout.newTab().setText("Videos"));
        tabLayout.addTab(tabLayout.newTab().setText("None"));
        tabLayout.addTab(tabLayout.newTab().setText("Achievements"));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String filter = "all";
                boolean showExtras = true;
                switch (tab.getPosition()) {
                    case 0: filter = "all"; break;
                    case 1: filter = "photos"; break;
                    case 2: filter = "videos"; break;
                    case 3: filter = "none"; break;
                    case 4: 
                        filter = "achievements"; 
                        showExtras = false;
                        break;
                }
                viewModel.setTabFilter(filter);
                int visibility = showExtras ? View.VISIBLE : View.GONE;
                recyclerFilter.setVisibility(visibility);
                tvPinnedLabel.setVisibility(visibility);
                recyclerPinned.setVisibility(visibility);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        CategoryChipAdapter filterAdapter = new CategoryChipAdapter();
        filterAdapter.setListener(position -> {
            if (selectedCategoriesForFilter != null && position < selectedCategoriesForFilter.size()) {
                selectedCategoriesForFilter.remove(position);
                filterAdapter.setCategories(selectedCategoriesForFilter, true, true);
                if (!selectedCategoriesForFilter.isEmpty()) {
                    viewModel.setCategoryFilter(selectedCategoriesForFilter.get(0).getName());
                } else {
                    viewModel.setCategoryFilter(null);
                }
            }
        });
        recyclerFilter.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerFilter.setAdapter(filterAdapter);

        new com.example.lifeos.repositories.CategoryRepository().getActiveCategories(new com.example.lifeos.interfaces.FirebaseCallback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> result) {
                filterAdapterCategories = result;
                // Use false for isHorizontalPreview initially if showing all, 
                // but since we aren't showing all, this is just for reference.
            }
            @Override
            public void onError(String message) {}
        });

        btnEdit.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), EditProfileActivity.class)));
        
        btnSelectCategories.setOnClickListener(v -> {
            CategorySelector.show(requireContext(), selectedCategoriesForFilter, selected -> {
                selectedCategoriesForFilter.clear();
                selectedCategoriesForFilter.addAll(selected);
                // Mark all as selected for gradient display
                for(Category c : selected) c.setSelected(true);
                filterAdapter.setCategories(selected, true, true);
                // Here you would typically trigger a viewmodel filter update based on the list
                if (!selected.isEmpty()) {
                    viewModel.setCategoryFilter(selected.get(0).getName()); // Example: filter by first one
                } else {
                    viewModel.setCategoryFilter(null);
                }
            });
        });

        viewModel.getUser().observe(getViewLifecycleOwner(), user -> bindUser(user, imgCover, imgProfile,
                tvName, tvUsername, tvBio, tvXp, tvLevel, tvFollowers, tvFollowing));
        viewModel.getFilteredPosts().observe(getViewLifecycleOwner(), posts -> gridAdapter.setPosts(posts));
        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {});
        viewModel.getError().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        });

        viewModel.loadProfile(userId, userId);
    }

    private List<Category> filterAdapterCategories;

    private void bindUser(User user, ImageView imgCover, ImageView imgProfile, TextView tvName,
                          TextView tvUsername, TextView tvBio, TextView tvXp, TextView tvLevel,
                          TextView tvFollowers, TextView tvFollowing) {
        if (user == null) return;
        tvName.setText(user.getName());
        tvUsername.setText("@" + user.getUsername());
        tvBio.setText(user.getBio());
        tvXp.setText(getString(R.string.xp_label, (int) user.getXp()));
        tvLevel.setText(getString(R.string.level, user.getLevel()));
        tvFollowers.setText(user.getFollowersCount() + " " + getString(R.string.followers));
        tvFollowing.setText(user.getFollowingCount() + " " + getString(R.string.following));
        Glide.with(this).load(user.getProfilePhoto()).circleCrop()
                .placeholder(R.drawable.ic_logo).into(imgProfile);
        Glide.with(this).load(user.getCoverPhoto()).placeholder(R.drawable.bg_card_glass).into(imgCover);
        pinnedAdapter.setPosts(viewModel.getPinnedPosts());
    }

    @Override
    public void onResume() {
        super.onResume();
        AuthRepository auth = new AuthRepository();
        if (auth.getCurrentUser() != null) {
            String uid = auth.getCurrentUser().getUid();
            viewModel.loadProfile(uid, uid);
        }
    }
}
