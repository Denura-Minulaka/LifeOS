package com.example.lifeos.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lifeos.R;
import com.example.lifeos.adapters.CategoryChipAdapter;
import com.example.lifeos.adapters.PostAdapter;
import com.example.lifeos.models.Category;
import com.example.lifeos.viewmodels.ExploreViewModel;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExploreFragment extends Fragment {

    private ExploreViewModel viewModel;
    private PostAdapter postAdapter;

    private static final List<String> EXPLORE_CATEGORIES = Arrays.asList(
            "career", "coding", "learning", "dancing", "singing", "fitness", "travel", "reading");

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

        TextInputEditText etSearch = view.findViewById(R.id.etSearch);
        RecyclerView recyclerCategories = view.findViewById(R.id.recyclerCategories);
        RecyclerView recyclerResults = view.findViewById(R.id.recyclerResults);

        List<Category> exploreCats = new ArrayList<>();
        for (String name : EXPLORE_CATEGORIES) {
            Category c = new Category();
            c.setId(name);
            c.setName(name);
            c.setActive(true);
            exploreCats.add(c);
        }

        CategoryChipAdapter chipAdapter = new CategoryChipAdapter();
        chipAdapter.setCategories(exploreCats, false);
        chipAdapter.setListener(position -> {
            Category c = exploreCats.get(position);
            viewModel.filterByCategory(c.getName());
        });
        recyclerCategories.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerCategories.setAdapter(chipAdapter);

        postAdapter = new PostAdapter();
        recyclerResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerResults.setAdapter(postAdapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                viewModel.search(s.toString().trim());
            }
        });

        viewModel.getPosts().observe(getViewLifecycleOwner(), posts -> postAdapter.setPosts(posts));
        viewModel.getError().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        });
    }
}
