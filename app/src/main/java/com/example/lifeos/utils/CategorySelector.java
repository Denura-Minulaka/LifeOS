package com.example.lifeos.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lifeos.R;
import com.example.lifeos.adapters.CategoryChipAdapter;
import com.example.lifeos.models.Category;
import com.example.lifeos.repositories.CategoryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CategorySelector {

    public interface OnCategoriesSelectedListener {
        void onSelected(List<Category> selectedCategories);
    }

    public static void show(Context context, List<Category> currentlySelected, OnCategoriesSelectedListener listener) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_category_selector);
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        EditText etSearch = dialog.findViewById(R.id.etSearch);
        TextView tvSelectedTitle = dialog.findViewById(R.id.tvSelectedTitle);
        RecyclerView recyclerSelected = dialog.findViewById(R.id.recyclerSelected);
        RecyclerView recyclerAll = dialog.findViewById(R.id.recyclerAll);
        View btnCancel = dialog.findViewById(R.id.btnCancel);
        View btnOk = dialog.findViewById(R.id.btnOk);

        List<Category> allCategories = new ArrayList<>();
        List<Category> filteredCategories = new ArrayList<>();
        List<Category> selectedList = new ArrayList<>(currentlySelected);

        CategoryChipAdapter selectedAdapter = new CategoryChipAdapter();
        CategoryChipAdapter allAdapter = new CategoryChipAdapter();

        recyclerSelected.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        recyclerSelected.setAdapter(selectedAdapter);

        recyclerAll.setLayoutManager(new GridLayoutManager(context, 2));
        recyclerAll.setAdapter(allAdapter);

        Runnable updateUI = () -> {
            selectedAdapter.setCategories(selectedList, true);
            tvSelectedTitle.setVisibility(selectedList.isEmpty() ? View.GONE : View.VISIBLE);
            recyclerSelected.setVisibility(selectedList.isEmpty() ? View.GONE : View.VISIBLE);
            
            // Mark selected in all list
            for (Category c : allCategories) {
                c.setSelected(selectedList.stream().anyMatch(s -> s.getId().equals(c.getId())));
            }
            allAdapter.notifyDataSetChanged();
        };

        new CategoryRepository().getActiveCategories(new com.example.lifeos.interfaces.FirebaseCallback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> result) {
                allCategories.clear();
                allCategories.addAll(result);
                filteredCategories.clear();
                filteredCategories.addAll(result);
                allAdapter.setCategories(filteredCategories, true);
                updateUI.run();
            }
            @Override
            public void onError(String message) {}
        });

        allAdapter.setListener(position -> {
            Category cat = filteredCategories.get(position);
            boolean alreadySelected = selectedList.stream().anyMatch(s -> s.getId().equals(cat.getId()));
            if (alreadySelected) {
                selectedList.removeIf(s -> s.getId().equals(cat.getId()));
            } else {
                cat.setSelected(true);
                selectedList.add(cat);
            }
            updateUI.run();
        });

        selectedAdapter.setListener(position -> {
            selectedList.remove(position);
            updateUI.run();
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                String query = s.toString().toLowerCase().trim();
                filteredCategories.clear();
                if (query.isEmpty()) {
                    filteredCategories.addAll(allCategories);
                } else {
                    filteredCategories.addAll(allCategories.stream()
                            .filter(c -> c.getName().toLowerCase().startsWith(query))
                            .collect(Collectors.toList()));
                }
                allAdapter.setCategories(filteredCategories, true);
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnOk.setOnClickListener(v -> {
            listener.onSelected(selectedList);
            dialog.dismiss();
        });

        dialog.show();
    }
}
