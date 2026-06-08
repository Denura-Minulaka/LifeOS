package com.example.lifeos.repositories;

import android.net.Uri;

import com.example.lifeos.firebase.FirebaseStorageService;
import com.example.lifeos.firebase.FirestoreService;
import com.example.lifeos.interfaces.FirebaseCallback;
import com.example.lifeos.interfaces.SimpleCallback;
import com.example.lifeos.models.User;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserRepository {

    private final FirestoreService firestoreService;
    private final FirebaseStorageService storageService;

    public UserRepository() {
        firestoreService = new FirestoreService();
        storageService = new FirebaseStorageService();
    }

    public void getUser(String userId, FirebaseCallback<User> callback) {
        firestoreService.getUser(userId, callback);
    }

    public void updateProfile(String userId, String name, String username, String bio,
                              SimpleCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("username", username);
        updates.put("bio", bio);
        firestoreService.updateUser(userId, updates, new SimpleCallback() {
            @Override
            public void onSuccess() {
                Map<String, Object> postUpdates = new HashMap<>();
                postUpdates.put("username", username);
                firestoreService.updatePostUserInfo(userId, postUpdates, callback);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public void uploadProfilePhoto(String userId, Uri uri, FirebaseCallback<String> callback) {
        String fileName = UUID.randomUUID().toString() + ".jpg";
        storageService.upload(storageService.profilePhotoRef(userId, fileName), uri)
                .addOnSuccessListener(url -> {
                    Map<String, Object> updates = new HashMap<>();
                    String photoUrl = url.toString();
                    updates.put("profilePhoto", photoUrl);
                    firestoreService.updateUser(userId, updates, new SimpleCallback() {
                        @Override
                        public void onSuccess() {
                            Map<String, Object> postUpdates = new HashMap<>();
                            postUpdates.put("userPhoto", photoUrl);
                            firestoreService.updatePostUserInfo(userId, postUpdates, new SimpleCallback() {
                                @Override
                                public void onSuccess() {
                                    callback.onSuccess(photoUrl);
                                }

                                @Override
                                public void onError(String message) {
                                    // Even if posts fail to update, the profile photo was updated
                                    callback.onSuccess(photoUrl);
                                }
                            });
                        }
                        @Override
                        public void onError(String message) {
                            callback.onError(message);
                        }
                    });
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void uploadCoverPhoto(String userId, Uri uri, FirebaseCallback<String> callback) {
        String fileName = UUID.randomUUID().toString() + ".jpg";
        storageService.upload(storageService.coverPhotoRef(userId, fileName), uri)
                .addOnSuccessListener(url -> {
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("coverPhoto", url.toString());
                    firestoreService.updateUser(userId, updates, new SimpleCallback() {
                        @Override
                        public void onSuccess() {
                            callback.onSuccess(url.toString());
                        }
                        @Override
                        public void onError(String message) {
                            callback.onError(message);
                        }
                    });
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}
