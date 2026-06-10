package com.example.lifeos.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lifeos.R;
import com.example.lifeos.adapters.CommentAdapter;
import com.example.lifeos.adapters.PostAdapter;
import com.example.lifeos.adapters.UserAdapter;
import com.example.lifeos.fragments.CommentBottomSheet;
import com.example.lifeos.interfaces.FirebaseCallback;
import com.example.lifeos.interfaces.SimpleCallback;
import com.example.lifeos.models.Comment;
import com.example.lifeos.models.Post;
import com.example.lifeos.models.User;
import com.example.lifeos.repositories.AuthRepository;
import com.example.lifeos.repositories.PostRepository;
import com.example.lifeos.repositories.UserRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SearchResultsActivity extends AppCompatActivity implements PostAdapter.PostListener {

    private String query;
    private UserRepository userRepository;
    private PostRepository postRepository;
    
    private UserAdapter usersSmallAdapter, usersOnlyAdapter;
    private PostAdapter postsSmallAdapter, postsOnlyAdapter;
    
    private List<User> allMatchedUsers = new ArrayList<>();
    private List<Post> allMatchedPosts = new ArrayList<>();
    
    private String userId;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        query = getIntent().getStringExtra("QUERY");
        userRepository = new UserRepository();
        postRepository = new PostRepository();
        
        AuthRepository auth = new AuthRepository();
        if (auth.getCurrentUser() != null) {
            userId = auth.getCurrentUser().getUid();
            userRepository.getUser(userId, new FirebaseCallback<User>() {
                @Override
                public void onSuccess(User result) {
                    currentUser = result;
                }
                @Override
                public void onError(String message) {}
            });
        }

        initViews();
        performSearch();
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        TextView tvQuery = findViewById(R.id.tvSearchQuery);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        
        tvQuery.setText(query);
        btnBack.setOnClickListener(v -> finish());

        // Adapters for "All" tab
        usersSmallAdapter = new UserAdapter();
        RecyclerView recyclerUsersSmall = findViewById(R.id.recyclerUsersSmall);
        recyclerUsersSmall.setLayoutManager(new LinearLayoutManager(this));
        recyclerUsersSmall.setAdapter(usersSmallAdapter);

        postsSmallAdapter = new PostAdapter();
        postsSmallAdapter.setListener(this);
        RecyclerView recyclerPostsSmall = findViewById(R.id.recyclerPostsSmall);
        recyclerPostsSmall.setLayoutManager(new LinearLayoutManager(this));
        recyclerPostsSmall.setAdapter(postsSmallAdapter);

        // Adapters for specific tabs
        usersOnlyAdapter = new UserAdapter();
        RecyclerView recyclerAccountsOnly = findViewById(R.id.recyclerAccountsOnly);
        recyclerAccountsOnly.setLayoutManager(new LinearLayoutManager(this));
        recyclerAccountsOnly.setAdapter(usersOnlyAdapter);

        postsOnlyAdapter = new PostAdapter();
        postsOnlyAdapter.setListener(this);
        RecyclerView recyclerPostsOnly = findViewById(R.id.recyclerPostsOnly);
        recyclerPostsOnly.setLayoutManager(new LinearLayoutManager(this));
        recyclerPostsOnly.setAdapter(postsOnlyAdapter);

        Button btnSeeMoreUsers = findViewById(R.id.btnSeeMoreUsers);
        btnSeeMoreUsers.setOnClickListener(v -> {
            TabLayout.Tab tab = tabLayout.getTabAt(1);
            if (tab != null) tab.select();
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                findViewById(R.id.scrollAll).setVisibility(View.GONE);
                findViewById(R.id.recyclerAccountsOnly).setVisibility(View.GONE);
                findViewById(R.id.recyclerPostsOnly).setVisibility(View.GONE);

                switch (tab.getPosition()) {
                    case 0: findViewById(R.id.scrollAll).setVisibility(View.VISIBLE); break;
                    case 1: findViewById(R.id.recyclerAccountsOnly).setVisibility(View.VISIBLE); break;
                    case 2: findViewById(R.id.recyclerPostsOnly).setVisibility(View.VISIBLE); break;
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void performSearch() {
        // Search Users
        userRepository.searchUsers(query, new FirebaseCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> result) {
                allMatchedUsers = result;
                updateUserUI();
            }
            @Override
            public void onError(String message) {
                Toast.makeText(SearchResultsActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });

        // Search Posts
        postRepository.searchPosts(query, userId, new FirebaseCallback<List<Post>>() {
            @Override
            public void onSuccess(List<Post> result) {
                allMatchedPosts = result;
                updatePostUI();
            }
            @Override
            public void onError(String message) {
                Toast.makeText(SearchResultsActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserUI() {
        usersOnlyAdapter.setUsers(allMatchedUsers);
        
        List<User> smallList = allMatchedUsers.stream().limit(3).collect(Collectors.toList());
        usersSmallAdapter.setUsers(smallList);
        
        findViewById(R.id.btnSeeMoreUsers).setVisibility(allMatchedUsers.size() > 3 ? View.VISIBLE : View.GONE);
    }

    private void updatePostUI() {
        postsOnlyAdapter.setPosts(allMatchedPosts);
        postsSmallAdapter.setPosts(allMatchedPosts);
        
        findViewById(R.id.tvPostsLabel).setVisibility(allMatchedPosts.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onLikeClick(Post post, int position) {
        if (userId == null) return;
        boolean liked = post.isLikedByCurrentUser();
        postRepository.toggleLike(post.getId(), userId, liked, new SimpleCallback() {
            @Override
            public void onSuccess() {
                post.setLikedByCurrentUser(!liked);
                post.setLikesCount(liked ? post.getLikesCount() - 1 : post.getLikesCount() + 1);
                postsOnlyAdapter.notifyDataSetChanged();
                postsSmallAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(SearchResultsActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCommentClick(Post post) {
        CommentBottomSheet.newInstance(post).show(getSupportFragmentManager(), "comments");
    }
}
