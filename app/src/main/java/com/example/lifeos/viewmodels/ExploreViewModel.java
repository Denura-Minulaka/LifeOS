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

    public LiveData<List<Post>> getPosts() { return posts; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }

    public void loadExplorePosts(String currentUserId) {
        loading.setValue(true);
        if (currentQuery.isEmpty() && selectedCategories.isEmpty()) {
            postRepository.getFeed(currentUserId, new FirebaseCallback<List<Post>>() {
                @Override
                public void onSuccess(List<Post> result) {
                    posts.setValue(result);
                    loading.setValue(false);
                }
                @Override
                public void onError(String message) {
                    error.setValue(message);
                    loading.setValue(false);
                }
            });
        } else {
            postRepository.searchPosts(currentQuery, new FirebaseCallback<List<Post>>() {
                @Override
                public void onSuccess(List<Post> result) {
                    List<Post> filtered = new ArrayList<>();
                    for (Post p : result) {
                        boolean matchesQuery = currentQuery.isEmpty() || 
                                (p.getTitle() != null && p.getTitle().toLowerCase().contains(currentQuery.toLowerCase())) ||
                                (p.getDescription() != null && p.getDescription().toLowerCase().contains(currentQuery.toLowerCase()));
                        
                        boolean matchesCategories = selectedCategories.isEmpty();
                        if (!selectedCategories.isEmpty() && p.getCategories() != null) {
                            for (String cat : selectedCategories) {
                                if (p.getCategories().contains(cat)) {
                                    matchesCategories = true;
                                    break;
                                }
                            }
                        }
                        
                        if (matchesQuery && matchesCategories) {
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
