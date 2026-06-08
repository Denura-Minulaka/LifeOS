package com.example.lifeos.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lifeos.R;
import com.example.lifeos.models.Post;
import com.example.lifeos.ui.BorderAnimationView;
import com.example.lifeos.utils.TimeUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProfilePostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface ProfilePostListener {
        void onLikeClick(Post post, int position);
        void onCommentClick(Post post);
        void onPinHold(Post post, View anchor);
    }

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_POST = 1;
    private static final int TYPE_LOAD_MORE = 2;

    private final List<Object> items = new ArrayList<>();
    private final Map<String, List<Post>> groupedPosts = new LinkedHashMap<>();
    private final Map<String, Integer> visibleCounts = new HashMap<>();
    private ProfilePostListener listener;

    public void setListener(ProfilePostListener listener) {
        this.listener = listener;
    }

    public void clearExpandedState() {
        visibleCounts.clear();
    }

    public void setPosts(List<Post> list) {
        groupedPosts.clear();
        
        if (list != null) {
            for (Post post : list) {
                if (post.getCreatedAt() == null) continue;
                String dateStr = formatDate(post.getCreatedAt().toDate());
                if (!groupedPosts.containsKey(dateStr)) {
                    groupedPosts.put(dateStr, new ArrayList<>());
                    if (!visibleCounts.containsKey(dateStr)) {
                        visibleCounts.put(dateStr, 2); // Initial visible count per day
                    }
                }
                List<Post> dayPosts = groupedPosts.get(dateStr);
                if (dayPosts != null) dayPosts.add(post);
            }
        }
        rebuildItems();
    }

    private void loadMoreForDate(String date) {
        Integer current = visibleCounts.get(date);
        if (current != null) {
            visibleCounts.put(date, current + 5);
            rebuildItems();
        }
    }

    private void rebuildItems() {
        items.clear();
        for (Map.Entry<String, List<Post>> entry : groupedPosts.entrySet()) {
            String dateStr = entry.getKey();
            List<Post> postsForDate = entry.getValue();
            Integer visible = visibleCounts.get(dateStr);
            int visibleCount = (visible != null) ? visible : 2;
            
            items.add(dateStr); // Add Header
            
            int countToShow = Math.min(visibleCount, postsForDate.size());
            for (int i = 0; i < countToShow; i++) {
                items.add(postsForDate.get(i));
            }
            
            if (postsForDate.size() > visibleCount) {
                items.add(new LoadMoreItem(dateStr));
            }
        }
        notifyDataSetChanged();
    }

    private String formatDate(java.util.Date date) {
        Calendar today = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        target.setTime(date);

        if (today.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)) {
            return "Today";
        }

        today.add(Calendar.DAY_OF_YEAR, -1);
        if (today.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)) {
            return "Yesterday";
        }

        return new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(date);
    }

    @Override
    public int getItemViewType(int position) {
        Object item = items.get(position);
        if (item instanceof String) return TYPE_HEADER;
        if (item instanceof LoadMoreItem) return TYPE_LOAD_MORE;
        return TYPE_POST;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            return new HeaderViewHolder(inflater.inflate(R.layout.item_post_date_header, parent, false));
        } else if (viewType == TYPE_LOAD_MORE) {
            return new LoadMoreViewHolder(inflater.inflate(R.layout.item_load_more, parent, false));
        } else {
            return new PostViewHolder(inflater.inflate(R.layout.item_post, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int type = getItemViewType(position);
        Object item = items.get(position);

        if (type == TYPE_HEADER) {
            ((HeaderViewHolder) holder).tvDate.setText((String) item);
        } else if (type == TYPE_LOAD_MORE) {
            LoadMoreItem lmi = (LoadMoreItem) item;
            holder.itemView.setOnClickListener(v -> loadMoreForDate(lmi.date));
        } else {
            Post post = (Post) item;
            ((PostViewHolder) holder).bind(post, listener, position);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private static class LoadMoreItem {
        final String date;
        LoadMoreItem(String date) { this.date = date; }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;
        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDateHeader);
        }
    }

    static class LoadMoreViewHolder extends RecyclerView.ViewHolder {
        LoadMoreViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView imgUser, imgMedia, icLike;
        TextView tvUsername, tvTime, tvTitle, tvDescription, tvLikes, tvComments, tvPinnedBadge;
        View btnLike, btnComment, cardMedia;
        BorderAnimationView borderAnimation;
        private android.os.Handler holdHandler = new android.os.Handler();
        private Runnable holdRunnable;

        PostViewHolder(@NonNull View itemView) {
            super(itemView);
            imgUser = itemView.findViewById(R.id.imgUser);
            imgMedia = itemView.findViewById(R.id.imgMedia);
            icLike = itemView.findViewById(R.id.icLike);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvLikes = itemView.findViewById(R.id.tvLikes);
            tvComments = itemView.findViewById(R.id.tvComments);
            tvPinnedBadge = itemView.findViewById(R.id.tvPinnedBadge);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnComment = itemView.findViewById(R.id.btnComment);
            cardMedia = itemView.findViewById(R.id.cardMedia);
            borderAnimation = itemView.findViewById(R.id.borderAnimation);
        }

        @android.annotation.SuppressLint("ClickableViewAccessibility")
        void bind(Post post, ProfilePostListener listener, int position) {
            tvUsername.setText(post.getUsername());
            tvTime.setText(TimeUtils.timeAgo(post.getCreatedAt()));
            tvTitle.setText(post.getTitle());
            tvDescription.setText(post.getDescription());
            tvLikes.setText(String.valueOf(post.getLikesCount()));
            tvComments.setText(String.valueOf(post.getCommentsCount()));
            
            if (tvPinnedBadge != null) {
                tvPinnedBadge.setVisibility(post.isPinned() ? View.VISIBLE : View.GONE);
            }

            Glide.with(itemView).load(post.getUserPhoto())
                    .circleCrop().placeholder(R.drawable.ic_logo).into(imgUser);

            if (post.getMediaUrl() != null && !post.getMediaUrl().isEmpty()) {
                cardMedia.setVisibility(View.VISIBLE);
                Glide.with(itemView).load(post.getMediaUrl()).into(imgMedia);
            } else {
                cardMedia.setVisibility(View.GONE);
            }

            if (post.isLikedByCurrentUser()) {
                icLike.setImageResource(android.R.drawable.btn_star_big_on);
                icLike.setColorFilter(itemView.getContext().getColor(R.color.primary_accent));
            } else {
                icLike.setImageResource(android.R.drawable.btn_star_big_off);
                icLike.setColorFilter(itemView.getContext().getColor(R.color.text_secondary));
            }

            if (listener != null) {
                btnLike.setOnClickListener(v -> listener.onLikeClick(post, position));
                btnComment.setOnClickListener(v -> listener.onCommentClick(post));

                itemView.setOnTouchListener((v, event) -> {
                    switch (event.getAction()) {
                        case android.view.MotionEvent.ACTION_DOWN:
                            holdRunnable = () -> {
                                if (listener != null) listener.onPinHold(post, itemView);
                                borderAnimation.stopAnimation();
                            };
                            holdHandler.postDelayed(holdRunnable, 1000);
                            borderAnimation.startAnimation(1000);
                            break;
                        case android.view.MotionEvent.ACTION_UP:
                        case android.view.MotionEvent.ACTION_CANCEL:
                            holdHandler.removeCallbacks(holdRunnable);
                            borderAnimation.stopAnimation();
                            break;
                    }
                    return true;
                });
            }
        }
    }
}
