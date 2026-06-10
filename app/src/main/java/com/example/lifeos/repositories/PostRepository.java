package com.example.lifeos.repositories;

import android.net.Uri;

import com.example.lifeos.firebase.FirebaseStorageService;
import com.example.lifeos.firebase.FirestoreService;
import com.example.lifeos.interfaces.FirebaseCallback;
import com.example.lifeos.interfaces.SimpleCallback;
import com.example.lifeos.models.Comment;
import com.example.lifeos.models.Post;

import java.util.List;
import java.util.UUID;

public class PostRepository {

    private final FirestoreService firestoreService;
    private final FirebaseStorageService storageService;

    public PostRepository() {
        firestoreService = new FirestoreService();
        storageService = new FirebaseStorageService();
    }

    public void getFeed(String userId, FirebaseCallback<List<Post>> callback) {
        firestoreService.getFeedPosts(userId, callback);
    }

    public void getUserPosts(String profileUserId, String currentUserId, FirebaseCallback<List<Post>> callback) {
        firestoreService.getUserPosts(profileUserId, currentUserId, callback);
    }

    public void searchPosts(String query, String currentUserId, FirebaseCallback<List<Post>> callback) {
        firestoreService.searchPosts(query, currentUserId, callback);
    }

    public void searchPosts(String query, FirebaseCallback<List<Post>> callback) {
        firestoreService.searchPosts(query, null, callback);
    }

    public void createPost(Post post, Uri mediaUri, SimpleCallback callback) {
        if (mediaUri != null) {
            String ext = post.getMediaType().equals("video") ? ".mp4" : ".jpg";
            String fileName = UUID.randomUUID().toString() + ext;
            storageService.upload(storageService.postMediaRef(post.getUserId(), fileName), mediaUri)
                    .addOnSuccessListener(url -> {
                        post.setMediaUrl(url.toString());
                        firestoreService.createPost(post, callback);
                    })
                    .addOnFailureListener(e -> callback.onError(e.getMessage()));
        } else {
            firestoreService.createPost(post, callback);
        }
    }

    public void toggleLike(String postId, String userId, boolean liked, SimpleCallback callback) {
        firestoreService.toggleLike(postId, userId, liked, callback);
    }

    public void pinPost(String postId, boolean pin, SimpleCallback callback) {
        firestoreService.pinPost(postId, pin, callback);
    }

    public void addComment(String postId, Comment comment, SimpleCallback callback) {
        firestoreService.addComment(postId, comment, callback);
    }

    public void getComments(String postId, FirebaseCallback<List<Comment>> callback) {
        firestoreService.getComments(postId, callback);
    }
}
