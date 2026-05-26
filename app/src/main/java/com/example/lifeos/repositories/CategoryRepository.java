package com.example.lifeos.repositories;

import com.example.lifeos.firebase.FirestoreService;
import com.example.lifeos.interfaces.FirebaseCallback;
import com.example.lifeos.models.Category;

import java.util.List;

public class CategoryRepository {

    private final FirestoreService firestoreService = new FirestoreService();

    public void getActiveCategories(FirebaseCallback<List<Category>> callback) {
        firestoreService.getCategories(callback);
    }
}
