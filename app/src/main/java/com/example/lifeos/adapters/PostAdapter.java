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
        ImageView imgUser, imgMedia, icLike;
        TextView tvUsername, tvTime, tvTitle, tvDescription, tvLikes, tvComments;
        View btnLike, btnComment, cardMedia;

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
            btnLike = itemView.findViewById(R.id.btnLike);
            btnComment = itemView.findViewById(R.id.btnComment);
            cardMedia = itemView.findViewById(R.id.cardMedia);
        }

        void bind(Post post, PostListener listener, int position) {
            tvUsername.setText(post.getUsername());
            tvTime.setText(TimeUtils.timeAgo(post.getCreatedAt()));
            tvTitle.setText(post.getTitle());
            tvDescription.setText(post.getDescription());
            tvLikes.setText(String.valueOf(post.getLikesCount()));
            tvComments.setText(String.valueOf(post.getCommentsCount()));

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
            }
        }
    }
}
