package com.example.lifeos.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.example.lifeos.adapters.CommentAdapter;
import com.example.lifeos.adapters.PinnedPostAdapter;
import com.example.lifeos.adapters.ProfilePostAdapter;
import com.example.lifeos.adapters.UserPostGridAdapter;
import com.example.lifeos.interfaces.FirebaseCallback;
import com.example.lifeos.interfaces.SimpleCallback;
import com.example.lifeos.models.Category;
import com.example.lifeos.models.Comment;
import com.example.lifeos.models.Post;
import com.example.lifeos.models.User;
import com.example.lifeos.repositories.AuthRepository;
import com.example.lifeos.repositories.PostRepository;
import com.example.lifeos.repositories.UserRepository;
import com.example.lifeos.utils.CategorySelector;
import com.example.lifeos.utils.SessionManager;
import com.example.lifeos.viewmodels.ProfileViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment implements ProfilePostAdapter.ProfilePostListener, PinnedPostAdapter.PinnedPostListener {

    private ProfileViewModel viewModel;
    private ProfilePostAdapter postAdapter;
    private PinnedPostAdapter pinnedAdapter;
    private PostRepository postRepository;
    private String userId;
    private User currentUser;
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
        TextView tvNoPinned = view.findViewById(R.id.tvNoPinned);

        postAdapter = new ProfilePostAdapter();
        postAdapter.setListener(this);
        pinnedAdapter = new PinnedPostAdapter();
        pinnedAdapter.setListener(this);
        recyclerPosts.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerPosts.setAdapter(postAdapter);

        recyclerPinned.setLayoutManager(new LinearLayoutManager(requireContext()));
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
        viewModel.getFilteredPosts().observe(getViewLifecycleOwner(), posts -> {
            postAdapter.setPosts(posts);
            List<Post> pinned = viewModel.getPinnedPosts();
            pinnedAdapter.setPosts(pinned);
            tvNoPinned.setVisibility(pinned.isEmpty() ? View.VISIBLE : View.GONE);
            recyclerPinned.setVisibility(pinned.isEmpty() ? View.GONE : View.VISIBLE);
        });
        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {});
        viewModel.getError().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        });

        viewModel.loadProfile(userId, userId);
    }

    private List<Category> filterAdapterCategories;

    @Override
    public void onLikeClick(Post post, int position) {
        viewModel.toggleLike(post, userId);
    }

    @Override
    public void onPinHold(Post post, View anchor) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_pin_action);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        ImageView imgIcon = dialog.findViewById(R.id.imgActionIcon);
        TextView tvTitle = dialog.findViewById(R.id.tvActionTitle);
        MaterialButton btnConfirm = dialog.findViewById(R.id.btnConfirm);
        MaterialButton btnCancel = dialog.findViewById(R.id.btnCancel);

        boolean isPinned = post.isPinned();
        tvTitle.setText(isPinned ? "Unpin The Post" : "Pin The Post");
        imgIcon.setImageResource(isPinned ? android.R.drawable.ic_menu_delete : android.R.drawable.ic_menu_save);

        btnConfirm.setOnClickListener(v -> {
            viewModel.pinPost(post, !isPinned);
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
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
