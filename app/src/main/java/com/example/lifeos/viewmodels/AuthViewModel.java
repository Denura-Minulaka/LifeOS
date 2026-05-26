package com.example.lifeos.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.lifeos.interfaces.FirebaseCallback;
import com.example.lifeos.interfaces.SimpleCallback;
import com.example.lifeos.repositories.AuthRepository;
import com.google.firebase.auth.FirebaseUser;

public class AuthViewModel extends ViewModel {

    private final AuthRepository authRepository = new AuthRepository();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<FirebaseUser> user = new MutableLiveData<>();

    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }
    public LiveData<FirebaseUser> getUser() { return user; }

    public void login(String email, String password) {
        loading.setValue(true);
        authRepository.login(email, password, authCallback());
    }

    public void register(String name, String username, String email, String password) {
        loading.setValue(true);
        authRepository.register(name, username, email, password, authCallback());
    }

    public void googleSignIn(String idToken) {
        loading.setValue(true);
        authRepository.signInWithGoogle(idToken, authCallback());
    }

    public void resetPassword(String email) {
        loading.setValue(true);
        authRepository.resetPassword(email, new SimpleCallback() {
            @Override
            public void onSuccess() {
                loading.setValue(false);
                error.setValue("Password reset email sent");
            }
            @Override
            public void onError(String message) {
                loading.setValue(false);
                error.setValue(message);
            }
        });
    }

    private FirebaseCallback<FirebaseUser> authCallback() {
        return new FirebaseCallback<FirebaseUser>() {
            @Override
            public void onSuccess(FirebaseUser result) {
                loading.setValue(false);
                user.setValue(result);
            }
            @Override
            public void onError(String message) {
                loading.setValue(false);
                error.setValue(message);
            }
        };
    }
}
