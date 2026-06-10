package com.example.lifeos.fragments;

import android.content.Intent;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lifeos.R;
import com.example.lifeos.activities.SearchActivity;
import com.example.lifeos.adapters.CategoryChipAdapter;
import com.example.lifeos.adapters.CommentAdapter;
import com.example.lifeos.adapters.PostAdapter;
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
import com.example.lifeos.viewmodels.ExploreViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ExploreFragment extends Fragment implements PostAdapter.PostListener {

    private ExploreViewModel viewModel;
    private PostAdapter postAdapter;
    private CategoryChipAdapter selectedCategoriesAdapter;
    private final List<Category> selectedCategories = new ArrayList<>();
    private String userId;
    private User currentUser;
    private PostRepository postRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_explore, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ExploreViewModel.class);
        postRepository = new PostRepository();
        
        AuthRepository auth = new AuthRepository();
        if (auth.getCurrentUser() != null) {
            userId = auth.getCurrentUser().getUid();
            new UserRepository().getUser(userId, new FirebaseCallback<User>() {
                @Override
                public void onSuccess(User result) {
                    currentUser = result;
                }
                @Override
                public void onError(String message) {}
            });
        }

        TextInputEditText etSearch = view.findViewById(R.id.etSearch);
        ImageButton btnFilter = view.findViewById(R.id.btnFilter);
        RecyclerView recyclerSelectedCategories = view.findViewById(R.id.recyclerSelectedCategories);
        RecyclerView recyclerResults = view.findViewById(R.id.recyclerResults);
        TabLayout tabLayout = view.findViewById(R.id.tabLayout);

        // Setup TabLayout
        tabLayout.addTab(tabLayout.newTab().setText("All"));
        tabLayout.addTab(tabLayout.newTab().setText("Photos"));
        tabLayout.addTab(tabLayout.newTab().setText("Videos"));
        tabLayout.addTab(tabLayout.newTab().setText("None"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String filter = "all";
                switch (tab.getPosition()) {
                    case 0: filter = "all"; break;
                    case 1: filter = "photos"; break;
                    case 2: filter = "videos"; break;
                    case 3: filter = "none"; break;
                }
                viewModel.setTabFilter(filter, userId);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Setup Selected Categories Chips
        selectedCategoriesAdapter = new CategoryChipAdapter();
        selectedCategoriesAdapter.setListener(position -> {
            selectedCategories.remove(position);
            updateCategoryFilters();
        });
        recyclerSelectedCategories.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerSelectedCategories.setAdapter(selectedCategoriesAdapter);

        // Setup Results
        postAdapter = new PostAdapter();
        postAdapter.setListener(this);
        recyclerResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerResults.setAdapter(postAdapter);

        // Search logic
        etSearch.setFocusable(false);
        etSearch.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), SearchActivity.class);
            startActivity(intent);
        });

        // Filter logic
        btnFilter.setOnClickListener(v -> {
            CategorySelector.show(requireContext(), selectedCategories, selected -> {
                selectedCategories.clear();
                selectedCategories.addAll(selected);
                for (Category c : selectedCategories) c.setSelected(true);
                updateCategoryFilters();
            });
        });

        // Observe ViewModel
        viewModel.getPosts().observe(getViewLifecycleOwner(), posts -> postAdapter.setPosts(posts));
        viewModel.getError().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (userId != null) {
            viewModel.loadExplorePosts(userId);
        }
    }

    private void updateCategoryFilters() {
        View v = getView();
        if (v == null) return;
        
        RecyclerView recycler = v.findViewById(R.id.recyclerSelectedCategories);
        if (selectedCategories.isEmpty()) {
            recycler.setVisibility(View.GONE);
        } else {
            recycler.setVisibility(View.VISIBLE);
            selectedCategoriesAdapter.setCategories(selectedCategories, true, true);
        }
        
        List<String> names = selectedCategories.stream()
                .map(Category::getName)
                .collect(Collectors.toList());
        viewModel.setSelectedCategories(names, userId);
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
