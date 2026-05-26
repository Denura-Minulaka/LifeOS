package com.example.lifeos.models;

import com.google.firebase.Timestamp;

public class Comment {
    private String id;
    private String userId;
    private String username;
    private String userPhoto;
    private String text;
    private Timestamp createdAt;

    public Comment() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getUserPhoto() { return userPhoto; }
    public void setUserPhoto(String userPhoto) { this.userPhoto = userPhoto; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
