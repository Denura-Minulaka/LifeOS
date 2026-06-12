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

import java.util.ArrayList;
import java.util.List;

public class PinnedPostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface PinnedPostListener {
        void onLikeClick(Post post, int position);
        void onCommentClick(Post post);
        void onPinHold(Post post, View anchor);
    }

    private static final int TYPE_POST = 0;
    private static final int TYPE_LOAD_MORE = 1;

    private final List<Post> allPosts = new ArrayList<>();
    private final List<Object> visibleItems = new ArrayList<>();
    private int visibleCount = 2;
    private PinnedPostListener listener;
    private boolean canPin = true;

    public void setListener(PinnedPostListener listener) {
        this.listener = listener;
    }

    public void setCanPin(boolean canPin) {
        this.canPin = canPin;
    }

    public void setPosts(List<Post> list) {
        allPosts.clear();
        if (list != null) allPosts.addAll(list);
        visibleCount = 2;
        rebuild();
    }

    private void rebuild() {
        visibleItems.clear();
        int toShow = Math.min(visibleCount, allPosts.size());
        for (int i = 0; i < toShow; i++) {
            visibleItems.add(allPosts.get(i));
        }
        if (allPosts.size() > visibleCount) {
            visibleItems.add("LOAD_MORE");
        }
        notifyDataSetChanged();
    }

    public void loadMore() {
        visibleCount += 5;
        rebuild();
    }

    @Override
    public int getItemViewType(int position) {
        return visibleItems.get(position) instanceof String ? TYPE_LOAD_MORE : TYPE_POST;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_LOAD_MORE) {
            return new LoadMoreViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_load_more, parent, false));
        }
        return new PostViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof LoadMoreViewHolder) {
            holder.itemView.setOnClickListener(v -> loadMore());
        } else {
            Post post = (Post) visibleItems.get(position);
            ((PostViewHolder) holder).bind(post, listener, position, canPin);
        }
    }

    @Override
    public int getItemCount() {
        return visibleItems.size();
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
        void bind(Post post, PinnedPostListener listener, int position, boolean canPin) {
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

                if (canPin) {
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
                } else {
                    itemView.setOnTouchListener(null);
                    itemView.setLongClickable(false);
                    borderAnimation.stopAnimation();
                    borderAnimation.setVisibility(View.GONE);
                }
            }
        }
    }

    static class LoadMoreViewHolder extends RecyclerView.ViewHolder {
        LoadMoreViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
