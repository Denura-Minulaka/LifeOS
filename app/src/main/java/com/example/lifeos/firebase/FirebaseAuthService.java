package com.example.lifeos.firebase;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

/** Wraps Firebase Authentication operations. */
public class FirebaseAuthService {

    private final FirebaseAuth auth;

    public FirebaseAuthService() {
        auth = FirebaseAuth.getInstance();
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public boolean isLoggedIn() {
        return getCurrentUser() != null;
    }

    public Task<AuthResult> signInWithEmail(String email, String password) {
        return auth.signInWithEmailAndPassword(email, password);
    }

    public Task<AuthResult> registerWithEmail(String email, String password) {
        return auth.createUserWithEmailAndPassword(email, password);
    }

    public Task<AuthResult> signInWithGoogle(String idToken) {
        return auth.signInWithCredential(
                GoogleAuthProvider.getCredential(idToken, null));
    }

    public Task<Void> sendPasswordReset(String email) {
        return auth.sendPasswordResetEmail(email);
    }

    public void signOut() {
        auth.signOut();
    }
}
