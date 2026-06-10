package com.example.lifeos.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lifeos.R;
import com.example.lifeos.adapters.CommentAdapter;
import com.example.lifeos.interfaces.FirebaseCallback;
import com.example.lifeos.interfaces.SimpleCallback;
import com.example.lifeos.models.Comment;
import com.example.lifeos.models.Post;
import com.example.lifeos.models.User;
import com.example.lifeos.repositories.AuthRepository;
import com.example.lifeos.repositories.PostRepository;
import com.example.lifeos.repositories.UserRepository;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

public class CommentBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_POST = "arg_post";
    private Post post;
    private PostRepository postRepository;
    private CommentAdapter adapter;
    private String userId;
    private User currentUser;

    public static CommentBottomSheet newInstance(Post post) {
        CommentBottomSheet fragment = new CommentBottomSheet();
        Bundle args = new Bundle();
        // Post is not Serializable/Parcelable in the current project yet, but we can pass ID if needed.
        // For now, let's assume we pass the whole object or ID. 
        // I'll check if Post implements Serializable.
        fragment.setPost(post);
        return fragment;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_comments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        postRepository = new PostRepository();
        AuthRepository auth = new AuthRepository();
        
        if (auth.getCurrentUser() != null) {
            userId = auth.getCurrentUser().getUid();
            new UserRepository().getUser(userId, new FirebaseCallback<User>() {
                @Override
                public void onSuccess(User result) {
                    currentUser = result;
                    ImageView ivCurrentUser = view.findViewById(R.id.ivCurrentUser);
                    Glide.with(requireContext()).load(currentUser.getProfilePhoto())
                            .placeholder(R.drawable.ic_logo).circleCrop().into(ivCurrentUser);
                }
                @Override
                public void onError(String message) {}
            });
        }

        TextView tvCount = view.findViewById(R.id.tvCommentsCount);
        RecyclerView recycler = view.findViewById(R.id.recyclerComments);
        EditText etComment = view.findViewById(R.id.etComment);
        ImageButton btnPost = view.findViewById(R.id.btnPostComment);

        tvCount.setText(getString(R.string.comments) + " (" + post.getCommentsCount() + ")");

        adapter = new CommentAdapter();
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        recycler.setAdapter(adapter);

        loadComments();

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
                    post.setCommentsCount(post.getCommentsCount() + 1);
                    tvCount.setText(getString(R.string.comments) + " (" + post.getCommentsCount() + ")");
                    loadComments();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void loadComments() {
        postRepository.getComments(post.getId(), new FirebaseCallback<List<Comment>>() {
            @Override
            public void onSuccess(List<Comment> result) {
                adapter.setComments(result);
            }

            @Override
            public void onError(String message) {}
        });
    }

    @Override
    public int getTheme() {
        // Use a theme that makes the background transparent so the rounded corners show
        return R.style.CustomBottomSheetDialog;
    }
}
