package com.example.lifeos.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lifeos.R;
import com.example.lifeos.adapters.CategoryChipAdapter;
import com.example.lifeos.adapters.PinnedPostAdapter;
import com.example.lifeos.adapters.ProfilePostAdapter;
import com.example.lifeos.fragments.CommentBottomSheet;
import com.example.lifeos.models.Category;
import com.example.lifeos.models.Post;
import com.example.lifeos.models.User;
import com.example.lifeos.repositories.AuthRepository;
import com.example.lifeos.utils.CategorySelector;
import com.example.lifeos.viewmodels.ProfileViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class UserProfileActivity extends AppCompatActivity implements ProfilePostAdapter.ProfilePostListener, PinnedPostAdapter.PinnedPostListener {

    private ProfileViewModel viewModel;
    private String targetUserId;
    private String currentUserId;
    private ProfilePostAdapter postAdapter;
    private PinnedPostAdapter pinnedAdapter;
    private CategoryChipAdapter filterAdapter;
    private MaterialButton btnFollow;
    private final List<Category> selectedCategoriesForFilter = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        targetUserId = getIntent().getStringExtra("USER_ID");
        AuthRepository auth = new AuthRepository();
        if (auth.getCurrentUser() != null) {
            currentUserId = auth.getCurrentUser().getUid();
        }

        if (targetUserId == null) {
            finish();
            return;
        }

        // If it's my own profile, this activity might not be the best, 
        // but let's handle it by hiding the follow button if needed, 
        // though normally SearchResults would lead here.
        
        initViews();
        setupViewModel();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        btnFollow = findViewById(R.id.btnFollow);
        if (targetUserId.equals(currentUserId)) {
            btnFollow.setVisibility(View.GONE);
        }

        RecyclerView recyclerPosts = findViewById(R.id.recyclerPosts);
        postAdapter = new ProfilePostAdapter();
        postAdapter.setListener(this);
        postAdapter.setCanPin(false); // Disable glowing/pinning for guest view
        recyclerPosts.setLayoutManager(new LinearLayoutManager(this));
        recyclerPosts.setAdapter(postAdapter);

        RecyclerView recyclerPinned = findViewById(R.id.recyclerPinned);
        pinnedAdapter = new PinnedPostAdapter();
        pinnedAdapter.setListener(this);
        pinnedAdapter.setCanPin(false);
        recyclerPinned.setLayoutManager(new LinearLayoutManager(this));
        recyclerPinned.setAdapter(pinnedAdapter);

        RecyclerView recyclerFilter = findViewById(R.id.recyclerCategoryFilter);
        filterAdapter = new CategoryChipAdapter();
        filterAdapter.setListener(position -> {
            if (position < selectedCategoriesForFilter.size()) {
                if (postAdapter != null) postAdapter.clearExpandedState();
                selectedCategoriesForFilter.remove(position);
                filterAdapter.setCategories(selectedCategoriesForFilter, true, true);
                if (!selectedCategoriesForFilter.isEmpty()) {
                    viewModel.setCategoryFilter(selectedCategoriesForFilter.get(0).getName());
                } else {
                    viewModel.setCategoryFilter(null);
                }
            }
        });
        recyclerFilter.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerFilter.setAdapter(filterAdapter);

        findViewById(R.id.btnSelectCategories).setOnClickListener(v -> {
            CategorySelector.show(this, selectedCategoriesForFilter, selected -> {
                if (postAdapter != null) postAdapter.clearExpandedState();
                selectedCategoriesForFilter.clear();
                selectedCategoriesForFilter.addAll(selected);
                for (Category c : selected) c.setSelected(true);
                filterAdapter.setCategories(selected, true, true);
                if (!selected.isEmpty()) {
                    viewModel.setCategoryFilter(selected.get(0).getName());
                } else {
                    viewModel.setCategoryFilter(null);
                }
            });
        });

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText("All"));
        tabLayout.addTab(tabLayout.newTab().setText("Photos"));
        tabLayout.addTab(tabLayout.newTab().setText("Videos"));
        tabLayout.addTab(tabLayout.newTab().setText("None"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (postAdapter != null) postAdapter.clearExpandedState();
                String filter;
                switch (tab.getPosition()) {
                    case 0: filter = "all"; break;
                    case 1: filter = "photos"; break;
                    case 2: filter = "videos"; break;
                    case 3: filter = "none"; break;
                    default: filter = "all"; break;
                }
                viewModel.setTabFilter(filter);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        btnFollow.setOnClickListener(v -> {
            Boolean following = viewModel.getIsFollowing().getValue();
            if (following != null && following) {
                viewModel.unfollowUser(currentUserId, targetUserId);
            } else {
                viewModel.followUser(currentUserId, targetUserId);
            }
        });
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        
        viewModel.getUser().observe(this, this::bindUser);
        viewModel.getFilteredPosts().observe(this, posts -> {
            postAdapter.setPosts(posts);
            List<Post> pinned = viewModel.getPinnedPosts();
            pinnedAdapter.setPosts(pinned);
            findViewById(R.id.tvPinnedLabel).setVisibility(pinned.isEmpty() ? View.GONE : View.VISIBLE);
            findViewById(R.id.recyclerPinned).setVisibility(pinned.isEmpty() ? View.GONE : View.VISIBLE);
        });

        viewModel.getIsFollowing().observe(this, following -> {
            if (following) {
                btnFollow.setText("Unfollow");
                btnFollow.setIconResource(0); // Optional: remove icon or change it
            } else {
                btnFollow.setText("Follow");
            }
        });

        viewModel.getError().observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        viewModel.loadProfile(targetUserId, currentUserId);
    }

    private void bindUser(User user) {
        if (user == null) return;
        
        String displayName = user.getName();
        if (user.getId().equals(currentUserId)) {
            displayName += " (Me)";
        }
        ((TextView) findViewById(R.id.tvName)).setText(displayName);
        ((TextView) findViewById(R.id.tvUsername)).setText("@" + user.getUsername());
        ((TextView) findViewById(R.id.tvBio)).setText(user.getBio());
        ((TextView) findViewById(R.id.tvFollowers)).setText(String.valueOf(user.getFollowersCount()));
        ((TextView) findViewById(R.id.tvFollowing)).setText(String.valueOf(user.getFollowingCount()));
        ((TextView) findViewById(R.id.tvLevel)).setText("Lvl " + user.getLevel());
        ((TextView) findViewById(R.id.tvXp)).setText(user.getXp() + " XP");

        Glide.with(this).load(user.getProfilePhoto())
                .placeholder(R.drawable.ic_logo).circleCrop().into((ImageView) findViewById(R.id.imgProfile));
        Glide.with(this).load(user.getCoverPhoto())
                .placeholder(R.drawable.bg_card_glass).into((ImageView) findViewById(R.id.imgCover));
    }

    @Override
    public void onLikeClick(Post post, int position) {
        viewModel.toggleLike(post, currentUserId);
    }

    @Override
    public void onCommentClick(Post post) {
        CommentBottomSheet.newInstance(post).show(getSupportFragmentManager(), "comments");
    }

    @Override
    public void onPinHold(Post post, View anchor) {
        // Users cannot pin other users' posts usually, 
        // but we can keep it empty or hide the action in the adapter if not owner.
    }
}
