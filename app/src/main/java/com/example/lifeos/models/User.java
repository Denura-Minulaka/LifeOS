package com.example.lifeos.models;

import com.google.firebase.Timestamp;

/** User profile document in Firestore. */
public class User {
    private String id;
    private String name;
    private String username;
    private String email;
    private String profilePhoto;
    private String coverPhoto;
    private String bio;
    private long xp;
    private int level;
    private long followersCount;
    private long followingCount;
    private Timestamp createdAt;

    public User() {}

    public User(String id, String name, String username, String email) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.email = email;
        this.xp = 0;
        this.level = 1;
        this.followersCount = 0;
        this.followingCount = 0;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getProfilePhoto() { return profilePhoto; }
    public void setProfilePhoto(String profilePhoto) { this.profilePhoto = profilePhoto; }
    public String getCoverPhoto() { return coverPhoto; }
    public void setCoverPhoto(String coverPhoto) { this.coverPhoto = coverPhoto; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public long getXp() { return xp; }
    public void setXp(long xp) { this.xp = xp; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public long getFollowersCount() { return followersCount; }
    public void setFollowersCount(long followersCount) { this.followersCount = followersCount; }
    public long getFollowingCount() { return followingCount; }
    public void setFollowingCount(long followingCount) { this.followingCount = followingCount; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
