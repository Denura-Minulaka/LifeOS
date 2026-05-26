package com.example.lifeos.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lifeos.R;
import com.example.lifeos.models.Comment;
import com.example.lifeos.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private final List<Comment> comments = new ArrayList<>();

    public void setComments(List<Comment> list) {
        comments.clear();
        if (list != null) comments.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CommentViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment c = comments.get(position);
        holder.tvUser.setText(c.getUsername());
        holder.tvText.setText(c.getText());
        holder.tvTime.setText(TimeUtils.timeAgo(c.getCreatedAt()));
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvUser, tvText, tvTime;

        CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUser = itemView.findViewById(R.id.tvCommentUser);
            tvText = itemView.findViewById(R.id.tvCommentText);
            tvTime = itemView.findViewById(R.id.tvCommentTime);
        }
    }
}
