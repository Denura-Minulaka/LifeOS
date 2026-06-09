package com.example.lifeos.firebase;

import com.example.lifeos.models.Category;
import com.example.lifeos.models.Comment;
import com.example.lifeos.models.DailyQuest;
import com.example.lifeos.models.Post;
import com.example.lifeos.models.User;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class FirestoreMapper {

    private FirestoreMapper() {}

    public static User mapUser(DocumentSnapshot doc) {
        try {
            User user = doc.toObject(User.class);
            if (user != null) user.setId(doc.getId());
            return user;
        } catch (Exception e) {
            // Manual mapping if deserialization fails
            User user = new User();
            user.setId(doc.getId());
            user.setName(doc.getString("name"));
            user.setUsername(doc.getString("username"));
            user.setEmail(doc.getString("email"));
            user.setProfilePhoto(doc.getString("profilePhoto"));
            user.setCoverPhoto(doc.getString("coverPhoto"));
            user.setBio(doc.getString("bio"));
            user.setXp(doc.getLong("xp") != null ? doc.getLong("xp") : 0);
            user.setLevel(doc.getLong("level") != null ? doc.getLong("level").intValue() : 1);
            user.setFollowersCount(doc.getLong("followersCount") != null ? doc.getLong("followersCount") : 0);
            user.setFollowingCount(doc.getLong("followingCount") != null ? doc.getLong("followingCount") : 0);
            
            Object createdAtObj = doc.get("createdAt");
            if (createdAtObj instanceof Timestamp) {
                user.setCreatedAt((Timestamp) createdAtObj);
            } else {
                user.setCreatedAt(Timestamp.now());
            }
            
            return user;
        }
    }

    public static Post mapPost(DocumentSnapshot doc) {
        try {
            Post post = doc.toObject(Post.class);
            if (post != null) {
                post.setId(doc.getId());
                if (post.getCategories() == null) post.setCategories(new ArrayList<>());
            }
            return post;
        } catch (Exception e) {
            // Manual mapping if deserialization fails
            Post post = new Post();
            post.setId(doc.getId());
            post.setUserId(doc.getString("userId"));
            post.setUsername(doc.getString("username"));
            post.setUserPhoto(doc.getString("userPhoto"));
            post.setTitle(doc.getString("title"));
            post.setDescription(doc.getString("description"));
            post.setMediaUrl(doc.getString("mediaUrl"));
            post.setMediaType(doc.getString("mediaType"));
            
            Object cats = doc.get("categories");
            if (cats instanceof List) {
                post.setCategories((List<String>) cats);
            } else {
                post.setCategories(new ArrayList<>());
            }
            
            post.setVisibility(doc.getString("visibility"));
            post.setShouldGoFeed(doc.getString("shouldGoFeed"));
            post.setXpEarned(doc.getLong("xpEarned") != null ? doc.getLong("xpEarned") : 0);
            post.setLikesCount(doc.getLong("likesCount") != null ? doc.getLong("likesCount") : 0);
            post.setCommentsCount(doc.getLong("commentsCount") != null ? doc.getLong("commentsCount") : 0);
            post.setPinned(doc.getBoolean("pinned") != null ? doc.getBoolean("pinned") : false);
            
            Object createdAtObj = doc.get("createdAt");
            if (createdAtObj instanceof Timestamp) {
                post.setCreatedAt((Timestamp) createdAtObj);
            } else {
                post.setCreatedAt(Timestamp.now());
            }

            return post;
        }
    }

    public static Category mapCategory(DocumentSnapshot doc) {
        Category c = doc.toObject(Category.class);
        if (c != null) {
            c.setId(doc.getId());
            if (c.getName() == null || c.getName().isEmpty()) {
                c.setName(doc.getId());
            }
        }
        return c;
    }

    public static DailyQuest mapQuest(DocumentSnapshot doc) {
        DailyQuest q = doc.toObject(DailyQuest.class);
        if (q != null) q.setId(doc.getId());
        return q;
    }

    public static Comment mapComment(DocumentSnapshot doc) {
        try {
            Comment c = doc.toObject(Comment.class);
            if (c != null) c.setId(doc.getId());
            return c;
        } catch (Exception e) {
            Comment c = new Comment();
            c.setId(doc.getId());
            c.setUserId(doc.getString("userId"));
            c.setText(doc.getString("text"));
            
            Object createdAtObj = doc.get("createdAt");
            if (createdAtObj instanceof Timestamp) {
                c.setCreatedAt((Timestamp) createdAtObj);
            } else {
                c.setCreatedAt(Timestamp.now());
            }
            return c;
        }
    }

    public static Map<String, Object> userToMap(User user) {
        java.util.HashMap<String, Object> map = new java.util.HashMap<>();
        map.put("name", user.getName() != null ? user.getName() : "");
        map.put("username", user.getUsername() != null ? user.getUsername() : "");
        map.put("email", user.getEmail() != null ? user.getEmail() : "");
        map.put("profilePhoto", user.getProfilePhoto() != null ? user.getProfilePhoto() : "");
        map.put("coverPhoto", user.getCoverPhoto() != null ? user.getCoverPhoto() : "");
        map.put("bio", user.getBio() != null ? user.getBio() : "");
        map.put("xp", user.getXp());
        map.put("level", user.getLevel());
        map.put("followersCount", user.getFollowersCount());
        map.put("followingCount", user.getFollowingCount());
        map.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt() : Timestamp.now());
        return map;
    }
}
