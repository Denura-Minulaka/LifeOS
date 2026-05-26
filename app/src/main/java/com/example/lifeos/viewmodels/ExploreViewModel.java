package com.example.lifeos.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.lifeos.interfaces.FirebaseCallback;
import com.example.lifeos.models.Post;
import com.example.lifeos.models.User;
import com.example.lifeos.repositories.PostRepository;
import com.example.lifeos.firebase.FirestoreService;

import java.util.ArrayList;
import java.util.List;

public class ExploreViewModel extends ViewModel {

    private final PostRepository postRepository = new PostRepository();
    private final FirestoreService firestoreService = new FirestoreService();
    private final MutableLiveData<List<Post>> posts = new MutableLiveData<>();
    private final MutableLiveData<List<User>> users = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public LiveData<List<Post>> getPosts() { return posts; }
    public LiveData<List<User>> getUsers() { return users; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }

    public void search(String query) {
        if (query == null || query.trim().isEmpty()) {
            posts.setValue(new ArrayList<>());
            users.setValue(new ArrayList<>());
            return;
        }
        loading.setValue(true);
        final int[] pending = {2};
        postRepository.searchPosts(query, new FirebaseCallback<List<Post>>() {
            @Override
            public void onSuccess(List<Post> result) {
                posts.setValue(result);
                if (--pending[0] == 0) loading.setValue(false);
            }
            @Override
            public void onError(String message) {
                if (--pending[0] == 0) loading.setValue(false);
                error.setValue(message);
            }
        });
        firestoreService.searchUsers(query, new FirebaseCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> result) {
                users.setValue(result);
                if (--pending[0] == 0) loading.setValue(false);
            }
            @Override
            public void onError(String message) {
                if (--pending[0] == 0) loading.setValue(false);
            }
        });
    }

    public void filterByCategory(String category) {
        loading.setValue(true);
        postRepository.searchPosts(category, new FirebaseCallback<List<Post>>() {
            @Override
            public void onSuccess(List<Post> result) {
                List<Post> filtered = new ArrayList<>();
                for (Post p : result) {
                    if (p.getCategories() != null && p.getCategories().contains(category)) {
                        filtered.add(p);
                    }
                }
                loading.setValue(false);
                posts.setValue(filtered);
            }
            @Override
            public void onError(String message) {
                loading.setValue(false);
                error.setValue(message);
            }
        });
    }
}
