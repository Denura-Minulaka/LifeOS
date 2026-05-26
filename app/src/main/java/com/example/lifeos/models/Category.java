package com.example.lifeos.models;

public class Category {
    private String id;
    private String name;
    private long xp;
    private boolean isActive;
    private boolean selected;

    public Category() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public long getXp() { return xp; }
    public void setXp(long xp) { this.xp = xp; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }
}
