package com.example.lifeos.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lifeos.R;
import com.example.lifeos.adapters.CategoryChipAdapter;
import com.example.lifeos.firebase.FirestoreConstants;
import com.example.lifeos.interfaces.FirebaseCallback;
import com.example.lifeos.models.Category;
import com.example.lifeos.models.User;
import com.example.lifeos.repositories.AuthRepository;
import com.example.lifeos.repositories.UserRepository;
import com.example.lifeos.utils.CategorySelector;
import com.example.lifeos.viewmodels.CreatePostViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

public class CreatePostFragment extends Fragment {

    private CreatePostViewModel viewModel;
    private Uri mediaUri;
    private String mediaMime;
    private User currentUser;

    private final ActivityResultLauncher<String> pickMedia =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null) return;
                mediaUri = uri;
                mediaMime = requireContext().getContentResolver().getType(uri);
                Glide.with(this).load(uri).into((ImageView) requireView().findViewById(R.id.imgPreview));
                updateXp();
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CreatePostViewModel.class);

        AuthRepository auth = new AuthRepository();
        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();

        ImageView imgPreview = view.findViewById(R.id.imgPreview);
        MaterialButton btnSelect = view.findViewById(R.id.btnSelectMedia);
        TextInputEditText etTitle = view.findViewById(R.id.etTitle);
        TextInputEditText etDescription = view.findViewById(R.id.etDescription);
        SwitchMaterial switchPublic = view.findViewById(R.id.switchPublic);
        SwitchMaterial switchFeed = view.findViewById(R.id.switchFeed);
        TextView tvXp = view.findViewById(R.id.tvXpPreview);
        MaterialButton btnPublish = view.findViewById(R.id.btnPublish);
        MaterialButton btnSelectCategories = view.findViewById(R.id.btnSelectCategories);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        RecyclerView recyclerCategories = view.findViewById(R.id.recyclerCategories);

        new UserRepository().getUser(userId, new FirebaseCallback<User>() {
            @Override
            public void onSuccess(User result) {
                currentUser = result;
            }
            @Override
            public void onError(String message) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Profile error: " + message, Toast.LENGTH_LONG).show();
                }
            }
        });

        CategoryChipAdapter chipAdapter = new CategoryChipAdapter();
        chipAdapter.setListener(position -> {
            // No action needed on click here if it's just a preview list
        });
        recyclerCategories.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerCategories.setAdapter(chipAdapter);

        btnSelectCategories.setOnClickListener(v -> {
            CategorySelector.show(requireContext(), viewModel.getSelectedCategories(), selected -> {
                viewModel.setSelectedCategories(selected);
                // Mark all as selected for gradient display
                for (Category c : selected) c.setSelected(true);
                chipAdapter.setCategories(selected, true);
                updateXp();
            });
        });

        viewModel.loadCategories();
        viewModel.getCategories().observe(getViewLifecycleOwner(), list -> {
            // Initially, we don't show any selected categories if none are selected
            updateXp();
        });
        viewModel.getXpPreview().observe(getViewLifecycleOwner(), xp ->
                tvXp.setText(getString(R.string.xp_preview, xp != null ? xp : 0)));

        btnSelect.setOnClickListener(v -> pickMedia.launch("*/*"));
        switchPublic.setOnCheckedChangeListener((b, checked) -> updateXp());
        switchFeed.setOnCheckedChangeListener((b, checked) -> updateXp());

        btnPublish.setOnClickListener(v -> {
            if (currentUser == null) {
                Toast.makeText(requireContext(), "Still loading profile, please wait...", Toast.LENGTH_SHORT).show();
                return;
            }
            String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
            if (title.isEmpty()) {
                etTitle.setError("Title is required");
                return;
            }
            
            progressBar.setVisibility(View.VISIBLE);
            String desc = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
            String visibility = switchPublic.isChecked()
                    ? FirestoreConstants.VISIBILITY_PUBLIC : FirestoreConstants.VISIBILITY_PRIVATE;
            String feed = switchFeed.isChecked() ? FirestoreConstants.FEED_YES : FirestoreConstants.FEED_NO;
            String mediaType = CreatePostViewModel.mediaTypeFromUri(mediaUri, mediaMime);

            viewModel.publishPost(userId, currentUser.getUsername(),
                    currentUser.getProfilePhoto(), title, desc, mediaType, visibility, feed, mediaUri);
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), loading ->
                progressBar.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE));
        viewModel.getSuccess().observe(getViewLifecycleOwner(), ok -> {
            if (Boolean.TRUE.equals(ok)) {
                Toast.makeText(requireContext(), "Post published!", Toast.LENGTH_SHORT).show();
                etTitle.setText("");
                etDescription.setText("");
                imgPreview.setImageDrawable(null);
                mediaUri = null;
            }
        });
        viewModel.getError().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        });
    }

    private void updateXp() {
        View view = getView();
        if (view == null) return;
        SwitchMaterial switchPublic = view.findViewById(R.id.switchPublic);
        SwitchMaterial switchFeed = view.findViewById(R.id.switchFeed);
        String visibility = switchPublic.isChecked()
                ? FirestoreConstants.VISIBILITY_PUBLIC : FirestoreConstants.VISIBILITY_PRIVATE;
        String feed = switchFeed.isChecked() ? FirestoreConstants.FEED_YES : FirestoreConstants.FEED_NO;
        String mediaType = CreatePostViewModel.mediaTypeFromUri(mediaUri, mediaMime);
        viewModel.updateXpPreview(mediaType, visibility, feed);
    }
}
