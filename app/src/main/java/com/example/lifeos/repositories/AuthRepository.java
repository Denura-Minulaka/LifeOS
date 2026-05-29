package com.example.lifeos.repositories;

import com.example.lifeos.firebase.FirebaseAuthService;
import com.example.lifeos.firebase.FirestoreService;
import com.example.lifeos.interfaces.FirebaseCallback;
import com.example.lifeos.interfaces.SimpleCallback;
import com.example.lifeos.models.User;
import com.example.lifeos.utils.XpCalculator;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;

public class AuthRepository {

    private final FirebaseAuthService authService;
    private final FirestoreService firestoreService;

    public AuthRepository() {
        authService = new FirebaseAuthService();
        firestoreService = new FirestoreService();
    }

    public boolean isLoggedIn() {
        return authService.isLoggedIn();
    }

    public FirebaseUser getCurrentUser() {
        return authService.getCurrentUser();
    }

    public void login(String email, String password, FirebaseCallback<FirebaseUser> callback) {
        authService.signInWithEmail(email, password)
                .addOnSuccessListener(r -> callback.onSuccess(r.getUser()))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void register(String name, String username, String email, String password,
                         FirebaseCallback<FirebaseUser> callback) {
        authService.registerWithEmail(email, password)
                .addOnSuccessListener(r -> {
                    FirebaseUser fbUser = r.getUser();
                    if (fbUser == null) {
                        callback.onError("Registration failed");
                        return;
                    }
                    // Now authenticated, check if username is already taken by another user
                    firestoreService.isUsernameTaken(username, new FirebaseCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean taken) {
                            if (taken) {
                                callback.onError("Username already taken");
                                // Note: In a production app, you might want to delete the auth user here
                                // or allow them to change the username without re-authenticating.
                                return;
                            }
                            User user = new User(fbUser.getUid(), name, username, email);
                            user.setCreatedAt(Timestamp.now());
                            user.setLevel(XpCalculator.calculateLevel(0));
                            firestoreService.createUser(user, new SimpleCallback() {
                                @Override
                                public void onSuccess() {
                                    callback.onSuccess(fbUser);
                                }
                                @Override
                                public void onError(String message) {
                                    callback.onError(message);
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

    public void signInWithGoogle(String idToken, FirebaseCallback<FirebaseUser> callback) {
        authService.signInWithGoogle(idToken)
                .addOnSuccessListener(r -> {
                    FirebaseUser fbUser = r.getUser();
                    if (fbUser == null) {
                        callback.onError("Google sign-in failed");
                        return;
                    }
                    firestoreService.getUser(fbUser.getUid(), new FirebaseCallback<User>() {
                        @Override
                        public void onSuccess(User user) {
                            callback.onSuccess(fbUser);
                        }
                        @Override
                        public void onError(String message) {
                            User user = new User(
                                    fbUser.getUid(),
                                    fbUser.getDisplayName() != null ? fbUser.getDisplayName() : "User",
                                    "user_" + fbUser.getUid().substring(0, 6),
                                    fbUser.getEmail() != null ? fbUser.getEmail() : "");
                            user.setProfilePhoto(fbUser.getPhotoUrl() != null ? fbUser.getPhotoUrl().toString() : "");
                            user.setCreatedAt(Timestamp.now());
                            user.setLevel(1);
                            firestoreService.createUser(user, new SimpleCallback() {
                                @Override
                                public void onSuccess() {
                                    callback.onSuccess(fbUser);
                                }
                                @Override
                                public void onError(String err) {
                                    callback.onError(err);
                                }
                            });
                        }
                    });
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void resetPassword(String email, SimpleCallback callback) {
        authService.sendPasswordReset(email)
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void logout(android.content.Context context) {
        authService.signOut();
        com.google.android.gms.auth.api.signin.GoogleSignInOptions gso = new com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
                .build();
        com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, gso).signOut();
    }

    public void logout() {
        authService.signOut();
    }
}
