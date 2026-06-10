package com.example.lifeos.firebase;

import com.example.lifeos.interfaces.FirebaseCallback;
import com.example.lifeos.interfaces.SimpleCallback;
import com.example.lifeos.models.Category;
import com.example.lifeos.models.Comment;
import com.example.lifeos.models.DailyQuest;
import com.example.lifeos.models.Post;
import com.example.lifeos.models.User;
import com.example.lifeos.utils.XpCalculator;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Low-level Firestore CRUD operations. */
public class FirestoreService {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void createUser(User user, SimpleCallback callback) {
        Map<String, Object> data = FirestoreMapper.userToMap(user);
        db.collection(FirestoreConstants.USERS).document(user.getId())
                .set(data)
                .addOnSuccessListener(v -> {
                    Map<String, Object> stats = new HashMap<>();
                    stats.put("tasksCompleted", 0);
                    stats.put("streakDays", 0);
                    stats.put("totalPosts", 0);
                    db.collection(FirestoreConstants.USERS).document(user.getId())
                            .collection(FirestoreConstants.STATS).document("main")
                            .set(stats)
                            .addOnSuccessListener(s -> callback.onSuccess())
                            .addOnFailureListener(e -> callback.onError(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getUser(String userId, FirebaseCallback<User> callback) {
        db.collection(FirestoreConstants.USERS).document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) callback.onSuccess(FirestoreMapper.mapUser(doc));
                    else callback.onError("User not found");
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void updateUser(String userId, Map<String, Object> updates, SimpleCallback callback) {
        db.collection(FirestoreConstants.USERS).document(userId)
                .update(updates)
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void isUsernameTaken(String username, FirebaseCallback<Boolean> callback) {
        db.collection(FirestoreConstants.USERS)
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> callback.onSuccess(!snap.isEmpty()))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getFeedPosts(String currentUserId, FirebaseCallback<List<Post>> callback) {
        db.collection(FirestoreConstants.POSTS)
                .whereEqualTo("visibility", FirestoreConstants.VISIBILITY_PUBLIC)
                .limit(100)
                .get()
                .addOnSuccessListener(snap -> {
                    List<Post> posts = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snap) {
                        Post p = FirestoreMapper.mapPost(doc);
                        // Filter locally for "shouldGoFeed" to avoid complex index
                        if (p != null && FirestoreConstants.FEED_YES.equals(p.getShouldGoFeed())) {
                            posts.add(p);
                        }
                    }
                    // Sort locally to avoid index
                    posts.sort((p1, p2) -> {
                        if (p1.getCreatedAt() == null || p2.getCreatedAt() == null) return 0;
                        return p2.getCreatedAt().compareTo(p1.getCreatedAt());
                    });
                    mapPostsWithLikes(posts, currentUserId, callback);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getUserPosts(String userId, String currentUserId, FirebaseCallback<List<Post>> callback) {
        db.collection(FirestoreConstants.POSTS)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(snap -> {
                    List<Post> posts = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snap) {
                        Post p = FirestoreMapper.mapPost(doc);
                        if (p != null) posts.add(p);
                    }
                    // Sort locally to avoid index
                    posts.sort((p1, p2) -> {
                        if (p1.getCreatedAt() == null || p2.getCreatedAt() == null) return 0;
                        return p2.getCreatedAt().compareTo(p1.getCreatedAt());
                    });
                    mapPostsWithLikes(posts, currentUserId, callback);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void searchPosts(String query, String currentUserId, FirebaseCallback<List<Post>> callback) {
        db.collection(FirestoreConstants.POSTS)
                .whereEqualTo("visibility", FirestoreConstants.VISIBILITY_PUBLIC)
                .limit(100)
                .get()
                .addOnSuccessListener(snap -> {
                    List<Post> posts = new ArrayList<>();
                    String q = query.toLowerCase();
                    for (QueryDocumentSnapshot doc : snap) {
                        Post p = FirestoreMapper.mapPost(doc);
                        if (p != null && (contains(p.getTitle(), q) || contains(p.getDescription(), q)
                                || categoryMatch(p.getCategories(), q))) {
                            posts.add(p);
                        }
                    }
                    // Sort locally to avoid index
                    posts.sort((p1, p2) -> {
                        if (p1.getCreatedAt() == null || p2.getCreatedAt() == null) return 0;
                        return p2.getCreatedAt().compareTo(p1.getCreatedAt());
                    });
                    mapPostsWithLikes(posts, currentUserId, callback);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void searchPosts(String query, FirebaseCallback<List<Post>> callback) {
        searchPosts(query, null, callback);
    }

    public void searchUsers(String query, FirebaseCallback<List<User>> callback) {
        db.collection(FirestoreConstants.USERS)
                .limit(50)
                .get()
                .addOnSuccessListener(snap -> {
                    List<User> users = new ArrayList<>();
                    String q = query.toLowerCase();
                    for (QueryDocumentSnapshot doc : snap) {
                        User u = FirestoreMapper.mapUser(doc);
                        if (u != null && (contains(u.getUsername(), q) || contains(u.getName(), q))) {
                            users.add(u);
                        }
                    }
                    callback.onSuccess(users);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void createPost(Post post, SimpleCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", post.getUserId());
        data.put("username", post.getUsername());
        data.put("userPhoto", post.getUserPhoto() != null ? post.getUserPhoto() : "");
        data.put("title", post.getTitle());
        data.put("description", post.getDescription());
        data.put("mediaUrl", post.getMediaUrl() != null ? post.getMediaUrl() : "");
        data.put("mediaType", post.getMediaType());
        data.put("categories", post.getCategories());
        data.put("visibility", post.getVisibility());
        data.put("shouldGoFeed", post.getShouldGoFeed());
        data.put("xpEarned", post.getXpEarned());
        data.put("likesCount", 0L);
        data.put("commentsCount", 0L);
        data.put("pinned", post.isPinned());
        data.put("createdAt", Timestamp.now());

        db.collection(FirestoreConstants.POSTS).add(data)
                .addOnSuccessListener(ref -> {
                    db.collection(FirestoreConstants.USERS).document(post.getUserId())
                            .collection(FirestoreConstants.STATS).document("main")
                            .update("totalPosts", FieldValue.increment(1));
                    addXpToUser(post.getUserId(), post.getXpEarned(), callback);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void toggleLike(String postId, String userId, boolean currentlyLiked, SimpleCallback callback) {
        var likeRef = db.collection(FirestoreConstants.POSTS).document(postId)
                .collection(FirestoreConstants.LIKES).document(userId);
        var postRef = db.collection(FirestoreConstants.POSTS).document(postId);
        if (currentlyLiked) {
            likeRef.delete().addOnSuccessListener(v ->
                    postRef.update("likesCount", FieldValue.increment(-1))
                            .addOnSuccessListener(s -> callback.onSuccess())
                            .addOnFailureListener(e -> callback.onError(e.getMessage())));
        } else {
            // Save empty document (only document ID matters)
            likeRef.set(new HashMap<>()).addOnSuccessListener(v ->
                    postRef.update("likesCount", FieldValue.increment(1))
                            .addOnSuccessListener(s -> callback.onSuccess())
                            .addOnFailureListener(e -> callback.onError(e.getMessage())));
        }
    }

    public void pinPost(String postId, boolean pin, SimpleCallback callback) {
        db.collection(FirestoreConstants.POSTS).document(postId)
                .update("pinned", pin)
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void updatePostUserInfo(String userId, Map<String, Object> updates, SimpleCallback callback) {
        db.collection(FirestoreConstants.POSTS)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(snap -> {
                    if (snap.isEmpty()) {
                        callback.onSuccess();
                        return;
                    }
                    com.google.firebase.firestore.WriteBatch batch = db.batch();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : snap) {
                        batch.update(doc.getReference(), updates);
                    }
                    batch.commit()
                            .addOnSuccessListener(v -> callback.onSuccess())
                            .addOnFailureListener(e -> callback.onError(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void checkLiked(String postId, String userId, FirebaseCallback<Boolean> callback) {
        db.collection(FirestoreConstants.POSTS).document(postId)
                .collection(FirestoreConstants.LIKES).document(userId)
                .get()
                .addOnSuccessListener(doc -> callback.onSuccess(doc.exists()))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void addComment(String postId, Comment comment, SimpleCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", comment.getUserId());
        data.put("text", comment.getText());
        data.put("createdAt", Timestamp.now());

        db.collection(FirestoreConstants.POSTS).document(postId)
                .collection(FirestoreConstants.COMMENTS).add(data)
                .addOnSuccessListener(ref ->
                        db.collection(FirestoreConstants.POSTS).document(postId)
                                .update("commentsCount", FieldValue.increment(1))
                                .addOnSuccessListener(v -> callback.onSuccess())
                                .addOnFailureListener(e -> callback.onError(e.getMessage())))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getComments(String postId, FirebaseCallback<List<Comment>> callback) {
        db.collection(FirestoreConstants.POSTS).document(postId)
                .collection(FirestoreConstants.COMMENTS)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    List<Comment> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snap) {
                        Comment c = FirestoreMapper.mapComment(doc);
                        if (c != null) list.add(c);
                    }
                    if (list.isEmpty()) {
                        callback.onSuccess(list);
                        return;
                    }
                    // Fetch usernames for each comment
                    final int[] pending = {list.size()};
                    for (Comment comment : list) {
                        getUser(comment.getUserId(), new FirebaseCallback<User>() {
                            @Override
                            public void onSuccess(User user) {
                                comment.setUsername(user.getUsername());
                                comment.setUserPhoto(user.getProfilePhoto());
                                if (--pending[0] == 0) callback.onSuccess(list);
                            }
                            @Override
                            public void onError(String message) {
                                comment.setUsername("Unknown");
                                if (--pending[0] == 0) callback.onSuccess(list);
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getCategories(FirebaseCallback<List<Category>> callback) {
        db.collection(FirestoreConstants.CATEGORY)
                .get()
                .addOnSuccessListener(snap -> {
                    List<Category> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snap) {
                        Category c = FirestoreMapper.mapCategory(doc);
                        if (c != null) {
                            // If the document has isActive field, respect it, otherwise assume true for existing categories
                            Boolean active = doc.getBoolean("isActive");
                            if (active == null || active) {
                                list.add(c);
                            }
                        }
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getActiveQuests(FirebaseCallback<List<DailyQuest>> callback) {
        db.collection(FirestoreConstants.DAILY_QUESTS)
                .whereEqualTo("isActive", true)
                .get()
                .addOnSuccessListener(snap -> {
                    List<DailyQuest> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snap) {
                        DailyQuest q = FirestoreMapper.mapQuest(doc);
                        if (q != null) list.add(q);
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void addRecentSearch(String userId, String query, SimpleCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("query", query);
        data.put("timestamp", Timestamp.now());

        db.collection(FirestoreConstants.USERS).document(userId)
                .collection(FirestoreConstants.RECENT_SEARCHES).document(query)
                .set(data)
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getRecentSearches(String userId, FirebaseCallback<List<String>> callback) {
        db.collection(FirestoreConstants.USERS).document(userId)
                .collection(FirestoreConstants.RECENT_SEARCHES)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(snap -> {
                    List<String> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snap) {
                        list.add(doc.getString("query"));
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void deleteRecentSearch(String userId, String query, SimpleCallback callback) {
        db.collection(FirestoreConstants.USERS).document(userId)
                .collection(FirestoreConstants.RECENT_SEARCHES).document(query)
                .delete()
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void clearRecentSearches(String userId, SimpleCallback callback) {
        db.collection(FirestoreConstants.USERS).document(userId)
                .collection(FirestoreConstants.RECENT_SEARCHES)
                .get()
                .addOnSuccessListener(snap -> {
                    if (snap.isEmpty()) {
                        callback.onSuccess();
                        return;
                    }
                    com.google.firebase.firestore.WriteBatch batch = db.batch();
                    for (DocumentSnapshot doc : snap) {
                        batch.delete(doc.getReference());
                    }
                    batch.commit()
                            .addOnSuccessListener(v -> callback.onSuccess())
                            .addOnFailureListener(e -> callback.onError(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void isQuestCompletedToday(String userId, String questId, FirebaseCallback<Boolean> callback) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Timestamp startOfDay = new Timestamp(cal.getTime());

        db.collection(FirestoreConstants.USERS).document(userId)
                .collection(FirestoreConstants.COMPLETED_QUESTS).document(questId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        callback.onSuccess(false);
                        return;
                    }
                    Timestamp completedAt = doc.getTimestamp("completedAt");
                    callback.onSuccess(completedAt != null && completedAt.compareTo(startOfDay) >= 0);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void completeQuest(String userId, DailyQuest quest, SimpleCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("completedAt", Timestamp.now());
        data.put("xpEarned", quest.getXpReward());

        db.collection(FirestoreConstants.USERS).document(userId)
                .collection(FirestoreConstants.COMPLETED_QUESTS).document(quest.getId())
                .set(data)
                .addOnSuccessListener(v -> {
                    db.collection(FirestoreConstants.USERS).document(userId)
                            .collection(FirestoreConstants.STATS).document("main")
                            .update("tasksCompleted", FieldValue.increment(1));
                    addXpToUser(userId, quest.getXpReward(), callback);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    private void addXpToUser(String userId, long xpToAdd, SimpleCallback callback) {
        db.collection(FirestoreConstants.USERS).document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    long currentXp = doc.getLong("xp") != null ? doc.getLong("xp") : 0;
                    long newXp = currentXp + xpToAdd;
                    int newLevel = XpCalculator.calculateLevel(newXp);
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("xp", newXp);
                    updates.put("level", newLevel);
                    db.collection(FirestoreConstants.USERS).document(userId)
                            .update(updates)
                            .addOnSuccessListener(v -> callback.onSuccess())
                            .addOnFailureListener(e -> callback.onError(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    private void mapPostsWithLikes(List<Post> posts,
                                   String currentUserId, FirebaseCallback<List<Post>> callback) {
        if (posts.isEmpty() || currentUserId == null) {
            callback.onSuccess(posts);
            return;
        }
        final int[] pending = {posts.size()};
        for (Post post : posts) {
            checkLiked(post.getId(), currentUserId, new FirebaseCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean liked) {
                    post.setLikedByCurrentUser(liked);
                    if (--pending[0] == 0) callback.onSuccess(posts);
                }
                @Override
                public void onError(String message) {
                    if (--pending[0] == 0) callback.onSuccess(posts);
                }
            });
        }
    }

    private static boolean contains(String value, String q) {
        return value != null && value.toLowerCase().contains(q);
    }

    private static boolean categoryMatch(List<String> categories, String q) {
        if (categories == null) return false;
        for (String c : categories) {
            if (c != null && c.toLowerCase().contains(q)) return true;
        }
        return false;
    }
}
