package com.example.lifeos.utils;

import com.google.firebase.Timestamp;

import java.util.concurrent.TimeUnit;

public final class TimeUtils {

    private TimeUtils() {}

    public static String formatDateTime(java.util.Date date) {
        if (date == null) return "";
        return new java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault()).format(date);
    }

    public static String timeAgo(Timestamp timestamp) {
        if (timestamp == null) return "";
        long diff = System.currentTimeMillis() - timestamp.toDate().getTime();
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + "m ago";
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        if (hours < 24) return hours + "h ago";
        long days = TimeUnit.MILLISECONDS.toDays(diff);
        if (days < 7) return days + "d ago";
        return (days / 7) + "w ago";
    }
}
