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
import com.example.lifeos.utils.TimeUtils;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    public interface PostListener {
        void onLikeClick(Post post, int position);
        void onCommentClick(Post post);
    }

    private final List<Post> posts = new ArrayList<>();
    private PostListener listener;

    public void setPosts(List<Post> newPosts) {
        posts.clear();
        if (newPosts != null) posts.addAll(newPosts);
        notifyDataSetChanged();
    }

    public void setListener(PostListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post, listener, position);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView imgUser, imgMedia;
        TextView tvUsername, tvTime, tvTitle, tvDescription, tvLikes, tvComments, tvXp;
        ChipGroup chipGroup;
        View btnLike, btnComment;

        PostViewHolder(@NonNull View itemView) {
            super(itemView);
            imgUser = itemView.findViewById(R.id.imgUser);
            imgMedia = itemView.findViewById(R.id.imgMedia);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvLikes = itemView.findViewById(R.id.tvLikes);
            tvComments = itemView.findViewById(R.id.tvComments);
            tvXp = itemView.findViewById(R.id.tvXp);
            chipGroup = itemView.findViewById(R.id.chipGroup);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnComment = itemView.findViewById(R.id.btnComment);
        }

        void bind(Post post, PostListener listener, int position) {
            tvUsername.setText(post.getUsername());
            tvTime.setText(TimeUtils.timeAgo(post.getCreatedAt()));
            tvTitle.setText(post.getTitle());
            tvDescription.setText(post.getDescription());
            tvLikes.setText(String.valueOf(post.getLikesCount()));
            tvComments.setText(String.valueOf(post.getCommentsCount()));
            tvXp.setText(itemView.getContext().getString(R.string.xp_earned, (int) post.getXpEarned()));

            Glide.with(itemView).load(post.getUserPhoto())
                    .circleCrop().placeholder(R.drawable.ic_logo).into(imgUser);

            if (post.getMediaUrl() != null && !post.getMediaUrl().isEmpty()) {
                imgMedia.setVisibility(View.VISIBLE);
                Glide.with(itemView).load(post.getMediaUrl()).into(imgMedia);
            } else {
                imgMedia.setVisibility(View.GONE);
            }

            chipGroup.removeAllViews();
            if (post.getCategories() != null) {
                for (String cat : post.getCategories()) {
                    Chip chip = new Chip(itemView.getContext());
                    chip.setText(cat);
                    chip.setChipBackgroundColorResource(R.color.chip_background);
                    chip.setTextColor(itemView.getContext().getColor(R.color.text_primary));
                    chipGroup.addView(chip);
                }
            }

            btnLike.setAlpha(post.isLikedByCurrentUser() ? 1f : 0.5f);
            if (listener != null) {
                btnLike.setOnClickListener(v -> listener.onLikeClick(post, position));
                btnComment.setOnClickListener(v -> listener.onCommentClick(post));
            }
        }
    }
}
