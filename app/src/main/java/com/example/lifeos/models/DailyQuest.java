package com.example.lifeos.models;

public class DailyQuest {
    private String id;
    private String title;
    private String description;
    private long xpReward;
    private String category;
    private boolean isActive;
    private boolean completedToday;

    public DailyQuest() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public long getXpReward() { return xpReward; }
    public void setXpReward(long xpReward) { this.xpReward = xpReward; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public boolean isCompletedToday() { return completedToday; }
    public void setCompletedToday(boolean completedToday) { this.completedToday = completedToday; }
}
