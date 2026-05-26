package com.example.lifeos.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.lifeos.R;
import com.example.lifeos.adapters.CommentAdapter;
import com.example.lifeos.adapters.PostAdapter;
import com.example.lifeos.interfaces.FirebaseCallback;
import com.example.lifeos.interfaces.SimpleCallback;
import com.example.lifeos.models.Comment;
import com.example.lifeos.models.Post;
import com.example.lifeos.models.User;
import com.example.lifeos.repositories.AuthRepository;
import com.example.lifeos.repositories.PostRepository;
import com.example.lifeos.repositories.UserRepository;
import com.example.lifeos.viewmodels.FeedViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class HomeFragment extends Fragment implements PostAdapter.PostListener {

    private FeedViewModel viewModel;
    private PostAdapter adapter;
    private String userId;
    private User currentUser;
    private PostRepository postRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(FeedViewModel.class);
        postRepository = new PostRepository();
        AuthRepository auth = new AuthRepository();
        if (auth.getCurrentUser() == null) return;
        userId = auth.getCurrentUser().getUid();

        new UserRepository().getUser(userId, new FirebaseCallback<User>() {
            @Override
            public void onSuccess(User result) {
                currentUser = result;
            }
            @Override
            public void onError(String message) {}
        });

        SwipeRefreshLayout swipe = view.findViewById(R.id.swipeRefresh);
        RecyclerView recycler = view.findViewById(R.id.recyclerPosts);
        adapter = new PostAdapter();
        adapter.setListener(this);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        recycler.setAdapter(adapter);

        swipe.setOnRefreshListener(() -> viewModel.loadFeed(userId));
        viewModel.getPosts().observe(getViewLifecycleOwner(), posts -> {
            adapter.setPosts(posts);
            swipe.setRefreshing(false);
        });
        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            if (Boolean.TRUE.equals(loading)) swipe.setRefreshing(true);
        });
        viewModel.getError().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            swipe.setRefreshing(false);
        });

        viewModel.loadFeed(userId);
    }

    @Override
    public void onLikeClick(Post post, int position) {
        viewModel.toggleLike(post, userId);
    }

    @Override
    public void onCommentClick(Post post) {
        showCommentsDialog(post);
    }

    private void showCommentsDialog(Post post) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_comments);
        RecyclerView recycler = dialog.findViewById(R.id.recyclerComments);
        EditText etComment = dialog.findViewById(R.id.etComment);
        MaterialButton btnPost = dialog.findViewById(R.id.btnPostComment);
        CommentAdapter commentAdapter = new CommentAdapter();
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
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
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }
}
