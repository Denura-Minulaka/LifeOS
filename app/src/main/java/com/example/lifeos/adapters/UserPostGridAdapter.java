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

import java.util.ArrayList;
import java.util.List;

public class UserPostGridAdapter extends RecyclerView.Adapter<UserPostGridAdapter.GridViewHolder> {

    private final List<Post> posts = new ArrayList<>();

    public void setPosts(List<Post> list) {
        posts.clear();
        if (list != null) posts.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GridViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_post, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull GridViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.tvTitle.setText(post.getTitle());
        if (post.getMediaUrl() != null && !post.getMediaUrl().isEmpty()) {
            Glide.with(holder.itemView).load(post.getMediaUrl()).into(holder.imgThumb);
        } else {
            holder.imgThumb.setImageResource(R.drawable.ic_logo);
        }
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class GridViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumb;
        TextView tvTitle;

        GridViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumb = itemView.findViewById(R.id.imgThumb);
            tvTitle = itemView.findViewById(R.id.tvPostTitle);
        }
    }
}
