package com.example.lifeos.utils;

import com.example.lifeos.firebase.FirestoreConstants;
import com.example.lifeos.models.Category;

import java.util.List;

/** Calculates post XP per PRD formula. */
public final class XpCalculator {

    private XpCalculator() {}

    public static int mediaXp(String mediaType) {
        if (FirestoreConstants.MEDIA_VIDEO.equals(mediaType)) return 20;
        if (FirestoreConstants.MEDIA_IMAGE.equals(mediaType)) return 10;
        return 5;
    }

    public static int visibilityXp(String visibility) {
        return FirestoreConstants.VISIBILITY_PUBLIC.equals(visibility) ? 50 : 10;
    }

    public static int feedXp(String shouldGoFeed) {
        return FirestoreConstants.FEED_YES.equals(shouldGoFeed) ? 50 : 10;
    }

    public static int categoryXp(List<Category> selectedCategories) {
        int total = 0;
        if (selectedCategories == null) return 0;
        for (Category c : selectedCategories) {
            if (c.isSelected()) total += (int) c.getXp();
        }
        return total;
    }

    public static int calculatePostXp(String mediaType, String visibility, String shouldGoFeed,
                                    List<Category> selectedCategories) {
        return mediaXp(mediaType) + visibilityXp(visibility) + feedXp(shouldGoFeed)
                + categoryXp(selectedCategories);
    }

    public static int calculateLevel(long xp) {
        return Math.max(1, (int) (Math.floor(Math.sqrt(xp / 100.0)) + 1));
    }
}
