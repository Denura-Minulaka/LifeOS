package com.example.lifeos.models;

import com.google.firebase.Timestamp;

public class Task {
    private String id;
    private String userId;
    private String title;
    private String description;
    private Timestamp timeframe;
    private boolean completed;
    private Timestamp createdAt;

    public Task() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Timestamp getTimeframe() { return timeframe; }
    public void setTimeframe(Timestamp timeframe) { this.timeframe = timeframe; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
