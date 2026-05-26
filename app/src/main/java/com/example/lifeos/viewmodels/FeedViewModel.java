package com.example.lifeos.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.lifeos.interfaces.FirebaseCallback;
import com.example.lifeos.interfaces.SimpleCallback;
import com.example.lifeos.models.Post;
import com.example.lifeos.repositories.PostRepository;

import java.util.List;

public class FeedViewModel extends ViewModel {

    private final PostRepository postRepository = new PostRepository();
    private final MutableLiveData<List<Post>> posts = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public LiveData<List<Post>> getPosts() { return posts; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }

    public void loadFeed(String userId) {
        loading.setValue(true);
        postRepository.getFeed(userId, new FirebaseCallback<List<Post>>() {
            @Override
            public void onSuccess(List<Post> result) {
                loading.setValue(false);
                posts.setValue(result);
            }
            @Override
            public void onError(String message) {
                loading.setValue(false);
                error.setValue(message);
            }
        });
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
