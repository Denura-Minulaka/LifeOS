package com.example.lifeos.viewmodels;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.lifeos.firebase.FirestoreConstants;
import com.example.lifeos.interfaces.FirebaseCallback;
import com.example.lifeos.interfaces.SimpleCallback;
import com.example.lifeos.models.Category;
import com.example.lifeos.models.Post;
import com.example.lifeos.repositories.CategoryRepository;
import com.example.lifeos.repositories.PostRepository;
import com.example.lifeos.utils.XpCalculator;

import java.util.ArrayList;
import java.util.List;

public class CreatePostViewModel extends ViewModel {

    private final PostRepository postRepository = new PostRepository();
    private final CategoryRepository categoryRepository = new CategoryRepository();
    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    private final List<Category> selectedCategories = new ArrayList<>();
    private final MutableLiveData<Integer> xpPreview = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> success = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public LiveData<List<Category>> getCategories() { return categories; }
    public List<Category> getSelectedCategories() { return selectedCategories; }
    
    public void setSelectedCategories(List<Category> list) {
        selectedCategories.clear();
        selectedCategories.addAll(list);
    }

    public LiveData<Integer> getXpPreview() { return xpPreview; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<Boolean> getSuccess() { return success; }
    public LiveData<String> getError() { return error; }

    public void loadCategories() {
        categoryRepository.getActiveCategories(new FirebaseCallback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> result) {
                categories.setValue(result);
            }
            @Override
            public void onError(String message) {
                error.setValue(message);
            }
        });
    }

    public void updateXpPreview(String mediaType, String visibility, String shouldGoFeed) {
        xpPreview.setValue(XpCalculator.calculatePostXp(mediaType, visibility, shouldGoFeed, selectedCategories));
    }

    public void publishPost(String userId, String username, String userPhoto,
                            String title, String description, String mediaType,
                            String visibility, String shouldGoFeed, Uri mediaUri) {
        List<String> categoryNames = new ArrayList<>();
        for (Category c : selectedCategories) {
            categoryNames.add(c.getName());
        }
        int xp = XpCalculator.calculatePostXp(mediaType, visibility, shouldGoFeed, selectedCategories);

        Post post = new Post();
        post.setUserId(userId);
        post.setUsername(username);
        post.setUserPhoto(userPhoto);
        post.setTitle(title);
        post.setDescription(description);
        post.setMediaType(mediaType);
        post.setCategories(categoryNames);
        post.setVisibility(visibility);
        post.setShouldGoFeed(shouldGoFeed);
        post.setXpEarned(xp);

        loading.setValue(true);
        postRepository.createPost(post, mediaUri, new SimpleCallback() {
            @Override
            public void onSuccess() {
                loading.setValue(false);
                success.setValue(true);
            }
            @Override
            public void onError(String message) {
                loading.setValue(false);
                error.setValue(message);
            }
        });
    }

    public static String mediaTypeFromUri(Uri uri, String mime) {
        if (uri == null) return FirestoreConstants.MEDIA_NONE;
        if (mime != null && mime.startsWith("video")) return FirestoreConstants.MEDIA_VIDEO;
        return FirestoreConstants.MEDIA_IMAGE;
    }
}
