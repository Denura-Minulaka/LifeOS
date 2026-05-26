package com.example.lifeos.firebase;

import android.net.Uri;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/** Uploads media to Firebase Storage. */
public class FirebaseStorageService {

    private final StorageReference root;

    public FirebaseStorageService() {
        root = FirebaseStorage.getInstance().getReference();
    }

    public StorageReference postMediaRef(String userId, String fileName) {
        return root.child("posts").child(userId).child(fileName);
    }

    public StorageReference profilePhotoRef(String userId, String fileName) {
        return root.child("profiles").child(userId).child(fileName);
    }

    public StorageReference coverPhotoRef(String userId, String fileName) {
        return root.child("covers").child(userId).child(fileName);
    }

    public com.google.android.gms.tasks.Task<android.net.Uri> upload(StorageReference ref, Uri fileUri) {
        return ref.putFile(fileUri).continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException() != null ? task.getException() : new Exception("Upload failed");
            }
            return ref.getDownloadUrl();
        });
    }
}
