package com.example.lifeos.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.lifeos.firebase.FirestoreConstants;
import com.example.lifeos.interfaces.FirebaseCallback;
import com.example.lifeos.interfaces.SimpleCallback;
import com.example.lifeos.models.Post;
import com.example.lifeos.models.User;
import com.example.lifeos.models.UserStats;
import com.example.lifeos.repositories.PostRepository;
import com.example.lifeos.repositories.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProfileViewModel extends ViewModel {

    private final UserRepository userRepository = new UserRepository();
    private final PostRepository postRepository = new PostRepository();
    private final com.example.lifeos.firebase.FirestoreService firestoreService = new com.example.lifeos.firebase.FirestoreService();
    private final MutableLiveData<User> user = new MutableLiveData<>();
    private final MutableLiveData<UserStats> stats = new MutableLiveData<>();
    private final MutableLiveData<List<Map<String, Object>>> completedQuests = new MutableLiveData<>();
    private final MutableLiveData<List<Post>> allPosts = new MutableLiveData<>();
    private final MutableLiveData<List<Post>> filteredPosts = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isFollowing = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private String filterCategory = null;
    private String tabFilter = "all";

    public LiveData<User> getUser() { return user; }
    public LiveData<UserStats> getStats() { return stats; }
    public LiveData<List<Map<String, Object>>> getCompletedQuests() { return completedQuests; }
    public LiveData<List<Post>> getFilteredPosts() { return filteredPosts; }
    public LiveData<Boolean> getIsFollowing() { return isFollowing; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }

    public void loadProfile(String profileUserId, String currentUserId) {
        loading.setValue(true);
        if (currentUserId != null && !currentUserId.equals(profileUserId)) {
            userRepository.checkFollowing(currentUserId, profileUserId, new FirebaseCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    isFollowing.setValue(result);
                }
                @Override public void onError(String message) {}
            });
        }
        userRepository.getUser(profileUserId, new FirebaseCallback<User>() {
            @Override
            public void onSuccess(User result) {
                user.setValue(result);
                loadPosts(profileUserId, currentUserId);
                loadStats(profileUserId);
            }
            @Override
            public void onError(String message) {
                loading.setValue(false);
                error.setValue(message);
            }
        });
    }

    private void loadStats(String userId) {
        firestoreService.getUserStats(userId, new FirebaseCallback<UserStats>() {
            @Override
            public void onSuccess(UserStats result) {
                stats.setValue(result);
            }
            @Override
            public void onError(String message) {}
        });
        firestoreService.getCompletedQuests(userId, new FirebaseCallback<List<Map<String, Object>>>() {
            @Override
            public void onSuccess(List<Map<String, Object>> result) {
                completedQuests.setValue(result);
            }
            @Override
            public void onError(String message) {}
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
            String mediaType = p.getMediaType();
            if (mediaType == null || mediaType.trim().isEmpty()) {
                mediaType = FirestoreConstants.MEDIA_NONE;
            }

            boolean matchesTab = true;
            if ("photos".equals(tabFilter)) {
                matchesTab = FirestoreConstants.MEDIA_IMAGE.equalsIgnoreCase(mediaType);
            } else if ("videos".equals(tabFilter)) {
                matchesTab = FirestoreConstants.MEDIA_VIDEO.equalsIgnoreCase(mediaType);
            } else if ("none".equals(tabFilter)) {
                matchesTab = FirestoreConstants.MEDIA_NONE.equalsIgnoreCase(mediaType);
            } else if ("achievements".equals(tabFilter)) {
                matchesTab = false;
            }

            if (!matchesTab) continue;

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
        for (Post p : source) {
            if (!p.isPinned()) continue;

            String mediaType = p.getMediaType();
            if (mediaType == null || mediaType.trim().isEmpty()) {
                mediaType = FirestoreConstants.MEDIA_NONE;
            }

            boolean matchesTab = true;
            if ("photos".equals(tabFilter)) {
                matchesTab = FirestoreConstants.MEDIA_IMAGE.equalsIgnoreCase(mediaType);
            } else if ("videos".equals(tabFilter)) {
                matchesTab = FirestoreConstants.MEDIA_VIDEO.equalsIgnoreCase(mediaType);
            } else if ("none".equals(tabFilter)) {
                matchesTab = FirestoreConstants.MEDIA_NONE.equalsIgnoreCase(mediaType);
            } else if ("achievements".equals(tabFilter)) {
                matchesTab = false;
            }

            if (matchesTab) pinned.add(p);
        }
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

    public void followUser(String currentUserId, String targetUserId) {
        userRepository.followUser(currentUserId, targetUserId, new SimpleCallback() {
            @Override
            public void onSuccess() {
                isFollowing.setValue(true);
                // Optionally refresh user to update follower count
                loadProfile(targetUserId, currentUserId);
            }
            @Override
            public void onError(String message) {
                error.setValue(message);
            }
        });
    }

    public void unfollowUser(String currentUserId, String targetUserId) {
        userRepository.unfollowUser(currentUserId, targetUserId, new SimpleCallback() {
            @Override
            public void onSuccess() {
                isFollowing.setValue(false);
                // Optionally refresh user to update follower count
                loadProfile(targetUserId, currentUserId);
            }
            @Override
            public void onError(String message) {
                error.setValue(message);
            }
        });
    }
}
