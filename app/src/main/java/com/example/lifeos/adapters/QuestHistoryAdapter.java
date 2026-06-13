package com.example.lifeos.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lifeos.R;
import com.example.lifeos.utils.TimeUtils;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QuestHistoryAdapter extends RecyclerView.Adapter<QuestHistoryAdapter.ViewHolder> {

    private final List<Map<String, Object>> history = new ArrayList<>();

    public void setHistory(List<Map<String, Object>> list) {
        history.clear();
        if (list != null) history.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quest_history, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> data = history.get(position);
        holder.tvTitle.setText((String) data.get("questTitle"));
        
        Object earned = data.get("xpEarned");
        holder.tvXp.setText("+" + (earned != null ? earned : 0) + " XP");
        
        Object time = data.get("completedAt");
        if (time instanceof Timestamp) {
            holder.tvDate.setText(TimeUtils.formatDateTime(((Timestamp) time).toDate()));
        }
    }

    @Override
    public int getItemCount() {
        return history.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvXp, tvDate;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvHistoryTitle);
            tvXp = itemView.findViewById(R.id.tvHistoryXp);
            tvDate = itemView.findViewById(R.id.tvHistoryDate);
        }
    }
}
