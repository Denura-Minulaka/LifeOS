package com.example.lifeos.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.example.lifeos.fragments.CommentBottomSheet;
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
        ImageView btnCreatePost = view.findViewById(R.id.btnCreatePost);

        new UserRepository().getUser(userId, new FirebaseCallback<User>() {
            @Override
            public void onSuccess(User result) {
                currentUser = result;
            }
            @Override
            public void onError(String message) {}
        });

        btnCreatePost.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new CreatePostFragment())
                    .addToBackStack(null)
                    .commit();
        });

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
        CommentBottomSheet.newInstance(post).show(getChildFragmentManager(), "comments");
    }
}
