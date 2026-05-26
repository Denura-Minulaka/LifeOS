package com.example.lifeos.interfaces;

public interface FirebaseCallback<T> {
    void onSuccess(T result);
    void onError(String message);
}
