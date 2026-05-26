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
        User user = doc.toObject(User.class);
        if (user != null) user.setId(doc.getId());
        return user;
    }

    public static Post mapPost(DocumentSnapshot doc) {
        Post post = doc.toObject(Post.class);
        if (post != null) {
            post.setId(doc.getId());
            if (post.getCategories() == null) post.setCategories(new ArrayList<>());
        }
        return post;
    }

    public static Category mapCategory(DocumentSnapshot doc) {
        Category c = doc.toObject(Category.class);
        if (c != null) c.setId(doc.getId());
        return c;
    }

    public static DailyQuest mapQuest(DocumentSnapshot doc) {
        DailyQuest q = doc.toObject(DailyQuest.class);
        if (q != null) q.setId(doc.getId());
        return q;
    }

    public static Comment mapComment(DocumentSnapshot doc) {
        Comment c = doc.toObject(Comment.class);
        if (c != null) c.setId(doc.getId());
        return c;
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
