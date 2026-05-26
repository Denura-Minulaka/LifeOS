package com.example.lifeos.models;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

/** Post document in Firestore. */
public class Post {
    private String id;
    private String userId;
    private String username;
    private String userPhoto;
    private String title;
    private String description;
    private String mediaUrl;
    private String mediaType;
    private List<String> categories = new ArrayList<>();
    private String visibility;
    private String shouldGoFeed;
    private long xpEarned;
    private long likesCount;
    private long commentsCount;
    private boolean pinned;
    private Timestamp createdAt;
    private boolean likedByCurrentUser;

    public Post() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getUserPhoto() { return userPhoto; }
    public void setUserPhoto(String userPhoto) { this.userPhoto = userPhoto; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }
    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }
    public List<String> getCategories() { return categories; }
    public void setCategories(List<String> categories) { this.categories = categories; }
    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }
    public String getShouldGoFeed() { return shouldGoFeed; }
    public void setShouldGoFeed(String shouldGoFeed) { this.shouldGoFeed = shouldGoFeed; }
    public long getXpEarned() { return xpEarned; }
    public void setXpEarned(long xpEarned) { this.xpEarned = xpEarned; }
    public long getLikesCount() { return likesCount; }
    public void setLikesCount(long likesCount) { this.likesCount = likesCount; }
    public long getCommentsCount() { return commentsCount; }
    public void setCommentsCount(long commentsCount) { this.commentsCount = commentsCount; }
    public boolean isPinned() { return pinned; }
    public void setPinned(boolean pinned) { this.pinned = pinned; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public boolean isLikedByCurrentUser() { return likedByCurrentUser; }
    public void setLikedByCurrentUser(boolean likedByCurrentUser) {
        this.likedByCurrentUser = likedByCurrentUser;
    }
}
