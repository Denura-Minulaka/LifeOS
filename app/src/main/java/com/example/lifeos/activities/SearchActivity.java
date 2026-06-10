package com.example.lifeos.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lifeos.R;
import com.example.lifeos.adapters.CommentAdapter;
import com.example.lifeos.adapters.PostAdapter;
import com.example.lifeos.adapters.RecentSearchAdapter;
import com.example.lifeos.interfaces.FirebaseCallback;
import com.example.lifeos.interfaces.SimpleCallback;
import com.example.lifeos.models.Comment;
import com.example.lifeos.models.Post;
import com.example.lifeos.models.User;
import com.example.lifeos.repositories.AuthRepository;
import com.example.lifeos.repositories.PostRepository;
import com.example.lifeos.repositories.UserRepository;
import com.example.lifeos.viewmodels.ExploreViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements PostAdapter.PostListener {

    private ExploreViewModel viewModel;
    private PostAdapter postAdapter;
    private RecentSearchAdapter recentSearchAdapter;
    private final List<String> recentSearches = new ArrayList<>();
    private String userId;
    private User currentUser;
    private PostRepository postRepository;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        viewModel = new ViewModelProvider(this).get(ExploreViewModel.class);
        postRepository = new PostRepository();
        userRepository = new UserRepository();
        
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
            loadRecentSearches();
        }

        ImageButton btnBack = findViewById(R.id.btnBack);
        TextInputEditText etSearch = findViewById(R.id.etSearch);
        RecyclerView recyclerResults = findViewById(R.id.recyclerResults);
        RecyclerView recyclerRecent = findViewById(R.id.recyclerRecentSearches);
        TextView tvClearAll = findViewById(R.id.tvClearAll);

        btnBack.setOnClickListener(v -> finish());

        // Setup Recent Searches
        recentSearchAdapter = new RecentSearchAdapter();
        recentSearchAdapter.setListener(new RecentSearchAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String query) {
                performSearch(query);
            }

            @Override
            public void onDeleteClick(int position) {
                String query = recentSearches.get(position);
                userRepository.deleteRecentSearch(userId, query);
                recentSearches.remove(position);
                recentSearchAdapter.setItems(recentSearches);
                if (recentSearches.isEmpty()) {
                    findViewById(R.id.layoutRecentSearches).setVisibility(View.GONE);
                }
            }
        });
        recyclerRecent.setLayoutManager(new LinearLayoutManager(this));
        recyclerRecent.setAdapter(recentSearchAdapter);

        // No dummy data anymore
        recentSearchAdapter.setItems(recentSearches);

        tvClearAll.setOnClickListener(v -> {
            showClearHistoryDialog();
        });

        postAdapter = new PostAdapter();
        postAdapter.setListener(this);
        recyclerResults.setLayoutManager(new LinearLayoutManager(this));
        recyclerResults.setAdapter(postAdapter);

        etSearch.setOnEditorActionListener((v1, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                String query = etSearch.getText().toString().trim();
                if (!query.isEmpty()) {
                    performSearch(query);
                }
                return true;
            }
            return false;
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                // Check the actual text in the Editable
                boolean isEmpty = s == null || s.toString().trim().isEmpty();
                
                // Show/Hide recent searches based on whether text is empty AND history exists
                if (isEmpty && !recentSearches.isEmpty()) {
                    findViewById(R.id.layoutRecentSearches).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.layoutRecentSearches).setVisibility(View.GONE);
                }
            }
        });

        viewModel.getPosts().observe(this, posts -> postAdapter.setPosts(posts));
        viewModel.getError().observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });
        
        // Focus the search bar and show keyboard
        etSearch.requestFocus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecentSearches();
    }

    @Override
    public void onLikeClick(Post post, int position) {
        viewModel.toggleLike(post, userId);
    }

    @Override
    public void onCommentClick(Post post) {
        showCommentsDialog(post);
    }

    private void showClearHistoryDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.clear_history_title))
                .setMessage(getString(R.string.clear_history_msg))
                .setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss())
                .setPositiveButton(getString(R.string.confirm), (dialog, which) -> {
                    if (userId != null) {
                        userRepository.clearRecentSearches(userId, new SimpleCallback() {
                            @Override
                            public void onSuccess() {
                                recentSearches.clear();
                                recentSearchAdapter.setItems(recentSearches);
                                findViewById(R.id.layoutRecentSearches).setVisibility(View.GONE);
                            }

                            @Override
                            public void onError(String message) {
                                Toast.makeText(SearchActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .show();
    }

    private void performSearch(String query) {
        // Save to Firebase
        if (userId != null) {
            userRepository.addRecentSearch(userId, query);
        }

        // Update local list for immediate UI feedback if we return
        if (recentSearches.contains(query)) {
            recentSearches.remove(query);
        }
        recentSearches.add(0, query);
        recentSearchAdapter.setItems(recentSearches);
        
        Intent intent = new Intent(this, SearchResultsActivity.class);
        intent.putExtra("QUERY", query);
        startActivity(intent);
    }

    private void loadRecentSearches() {
        if (userId == null) return;
        userRepository.getRecentSearches(userId, new FirebaseCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> result) {
                recentSearches.clear();
                recentSearches.addAll(result);
                recentSearchAdapter.setItems(recentSearches);
                
                // Show if search bar is empty
                EditText etSearch = findViewById(R.id.etSearch);
                if (etSearch.getText().toString().trim().isEmpty() && !recentSearches.isEmpty()) {
                    findViewById(R.id.layoutRecentSearches).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String message) {}
        });
    }

    private void showCommentsDialog(Post post) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_comments);
        RecyclerView recycler = dialog.findViewById(R.id.recyclerComments);
        EditText etComment = dialog.findViewById(R.id.etComment);
        MaterialButton btnPost = dialog.findViewById(R.id.btnPostComment);
        CommentAdapter commentAdapter = new CommentAdapter();
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(commentAdapter);

        postRepository.getComments(post.getId(), new FirebaseCallback<List<Comment>>() {
            @Override
            public void onSuccess(List<Comment> result) {
                commentAdapter.setComments(result);
            }
            @Override
            public void onError(String message) {}
        });

        btnPost.setOnClickListener(v -> {
            String text = etComment.getText().toString().trim();
            if (text.isEmpty() || currentUser == null) return;
            Comment comment = new Comment();
            comment.setUserId(userId);
            comment.setUsername(currentUser.getUsername());
            comment.setUserPhoto(currentUser.getProfilePhoto());
            comment.setText(text);
            postRepository.addComment(post.getId(), comment, new SimpleCallback() {
                @Override
                public void onSuccess() {
                    etComment.setText("");
                    postRepository.getComments(post.getId(), new FirebaseCallback<List<Comment>>() {
                        @Override
                        public void onSuccess(List<Comment> result) {
                            commentAdapter.setComments(result);
                            post.setCommentsCount(post.getCommentsCount() + 1);
                        }
                        @Override
                        public void onError(String message) {}
                    });
                }
                @Override
                public void onError(String message) {
                    Toast.makeText(SearchActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }
}
