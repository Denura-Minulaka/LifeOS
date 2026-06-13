package com.example.lifeos.models;

public class UserStats {
    private long tasksCompleted;
    private long streakDays;
    private long totalPosts;

    public UserStats() {}

    public long getTasksCompleted() { return tasksCompleted; }
    public void setTasksCompleted(long tasksCompleted) { this.tasksCompleted = tasksCompleted; }
    public long getStreakDays() { return streakDays; }
    public void setStreakDays(long streakDays) { this.streakDays = streakDays; }
    public long getTotalPosts() { return totalPosts; }
    public void setTotalPosts(long totalPosts) { this.totalPosts = totalPosts; }
}
