package com.example.lifeos.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.lifeos.interfaces.FirebaseCallback;
import com.example.lifeos.interfaces.SimpleCallback;
import com.example.lifeos.models.Post;
import com.example.lifeos.repositories.PostRepository;

import java.util.ArrayList;
import java.util.List;

public class ExploreViewModel extends ViewModel {

    private final PostRepository postRepository = new PostRepository();
    private final MutableLiveData<List<Post>> posts = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    
    private String currentQuery = "";
    private final List<String> selectedCategories = new ArrayList<>();
    private String tabFilter = "all";

    public LiveData<List<Post>> getPosts() { return posts; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }

    public void setTabFilter(String filter, String userId) {
        this.tabFilter = filter;
        loadExplorePosts(userId);
    }

    public void loadExplorePosts(String currentUserId) {
        loading.setValue(true);
        postRepository.searchPosts(currentQuery, new FirebaseCallback<List<Post>>() {
            @Override
            public void onSuccess(List<Post> result) {
                List<Post> filtered = new ArrayList<>();
                for (Post p : result) {
                    // Search query filter
                    boolean matchesQuery = currentQuery.isEmpty() || 
                            (p.getTitle() != null && p.getTitle().toLowerCase().contains(currentQuery.toLowerCase())) ||
                            (p.getDescription() != null && p.getDescription().toLowerCase().contains(currentQuery.toLowerCase()));
                    
                    // Category filter
                    boolean matchesCategories = selectedCategories.isEmpty();
                    if (!selectedCategories.isEmpty() && p.getCategories() != null) {
                        for (String cat : selectedCategories) {
                            if (p.getCategories().contains(cat)) {
                                matchesCategories = true;
                                break;
                            }
                        }
                    }

                    // Tab filter (Media type)
                    boolean matchesTab = true;
                    if ("photos".equals(tabFilter)) {
                        matchesTab = "image".equals(p.getMediaType());
                    } else if ("videos".equals(tabFilter)) {
                        matchesTab = "video".equals(p.getMediaType());
                    } else if ("none".equals(tabFilter)) {
                        matchesTab = p.getMediaType() == null || "none".equals(p.getMediaType()) || p.getMediaType().isEmpty();
                    }
                    
                    if (matchesQuery && matchesCategories && matchesTab) {
                        filtered.add(p);
                    }
                }
                posts.setValue(filtered);
                loading.setValue(false);
            }
            @Override
            public void onError(String message) {
                error.setValue(message);
                loading.setValue(false);
            }
        });
    }

    public void setSearchQuery(String query, String userId) {
        this.currentQuery = query;
        loadExplorePosts(userId);
    }

    public void setSelectedCategories(List<String> categories, String userId) {
        this.selectedCategories.clear();
        this.selectedCategories.addAll(categories);
        loadExplorePosts(userId);
    }

    public void toggleLike(Post post, String userId) {
        boolean liked = post.isLikedByCurrentUser();
        postRepository.toggleLike(post.getId(), userId, liked, new SimpleCallback() {
            @Override
            public void onSuccess() {
                post.setLikedByCurrentUser(!liked);
                post.setLikesCount(liked ? post.getLikesCount() - 1 : post.getLikesCount() + 1);
                posts.setValue(posts.getValue());
            }
            @Override
            public void onError(String message) {
                error.setValue(message);
            }
        });
    }
}
