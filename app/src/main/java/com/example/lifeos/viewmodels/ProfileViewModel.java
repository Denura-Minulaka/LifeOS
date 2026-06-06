package com.example.lifeos.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.lifeos.interfaces.FirebaseCallback;
import com.example.lifeos.interfaces.SimpleCallback;
import com.example.lifeos.models.Post;
import com.example.lifeos.models.User;
import com.example.lifeos.repositories.PostRepository;
import com.example.lifeos.repositories.UserRepository;

import java.util.ArrayList;
import java.util.List;

public class ProfileViewModel extends ViewModel {

    private final UserRepository userRepository = new UserRepository();
    private final PostRepository postRepository = new PostRepository();
    private final MutableLiveData<User> user = new MutableLiveData<>();
    private final MutableLiveData<List<Post>> allPosts = new MutableLiveData<>();
    private final MutableLiveData<List<Post>> filteredPosts = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private String filterCategory = null;
    private String tabFilter = "posts";

    public LiveData<User> getUser() { return user; }
    public LiveData<List<Post>> getFilteredPosts() { return filteredPosts; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }

    public void loadProfile(String profileUserId, String currentUserId) {
        loading.setValue(true);
        userRepository.getUser(profileUserId, new FirebaseCallback<User>() {
            @Override
            public void onSuccess(User result) {
                user.setValue(result);
                loadPosts(profileUserId, currentUserId);
            }
            @Override
            public void onError(String message) {
                loading.setValue(false);
                error.setValue(message);
            }
        });
    }

    private void loadPosts(String profileUserId, String currentUserId) {
        postRepository.getUserPosts(profileUserId, currentUserId, new FirebaseCallback<List<Post>>() {
            @Override
            public void onSuccess(List<Post> result) {
                loading.setValue(false);
                allPosts.setValue(result);
                applyFilters();
            }
            @Override
            public void onError(String message) {
                loading.setValue(false);
                error.setValue(message);
            }
        });
    }

    public void setTabFilter(String tab) {
        tabFilter = tab;
        applyFilters();
    }

    public void setCategoryFilter(String category) {
        filterCategory = category;
        applyFilters();
    }

    private void applyFilters() {
        List<Post> source = allPosts.getValue();
        if (source == null) {
            filteredPosts.setValue(new ArrayList<>());
            return;
        }
        List<Post> result = new ArrayList<>();
        for (Post p : source) {
            if ("videos".equals(tabFilter) && !"video".equals(p.getMediaType())) continue;
            if ("achievements".equals(tabFilter)) {
                if (p.getCategories() == null || !p.getCategories().contains("achievements")) continue;
            }
            if (filterCategory != null && (p.getCategories() == null || !p.getCategories().contains(filterCategory))) {
                continue;
            }
            result.add(p);
        }
        filteredPosts.setValue(result);
    }

    public void pinPost(Post post, boolean pin) {
        postRepository.pinPost(post.getId(), pin, new SimpleCallback() {
            @Override
            public void onSuccess() {
                post.setPinned(pin);
                applyFilters();
            }
            @Override
            public void onError(String message) {
                error.setValue(message);
            }
        });
    }

    public List<Post> getPinnedPosts() {
        List<Post> source = allPosts.getValue();
        List<Post> pinned = new ArrayList<>();
        if (source == null) return pinned;
        for (Post p : source) if (p.isPinned()) pinned.add(p);
        return pinned;
    }

    public void toggleLike(Post post, String userId) {
        boolean liked = post.isLikedByCurrentUser();
        postRepository.toggleLike(post.getId(), userId, liked, new SimpleCallback() {
            @Override
            public void onSuccess() {
                post.setLikedByCurrentUser(!liked);
                post.setLikesCount(liked ? post.getLikesCount() - 1 : post.getLikesCount() + 1);
                filteredPosts.setValue(filteredPosts.getValue());
            }
            @Override
            public void onError(String message) {
                error.setValue(message);
            }
        });
    }
}
