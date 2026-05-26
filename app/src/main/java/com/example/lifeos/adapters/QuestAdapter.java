package com.example.lifeos.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lifeos.R;
import com.example.lifeos.models.DailyQuest;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class QuestAdapter extends RecyclerView.Adapter<QuestAdapter.QuestViewHolder> {

    public interface QuestListener {
        void onComplete(DailyQuest quest, int position);
    }

    private final List<DailyQuest> quests = new ArrayList<>();
    private QuestListener listener;

    public void setQuests(List<DailyQuest> list) {
        quests.clear();
        if (list != null) quests.addAll(list);
        notifyDataSetChanged();
    }

    public void setListener(QuestListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public QuestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new QuestViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quest, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull QuestViewHolder holder, int position) {
        holder.bind(quests.get(position), listener, position);
    }

    @Override
    public int getItemCount() {
        return quests.size();
    }

    static class QuestViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvXp;
        MaterialButton btnComplete;

        QuestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvQuestTitle);
            tvDescription = itemView.findViewById(R.id.tvQuestDescription);
            tvXp = itemView.findViewById(R.id.tvQuestXp);
            btnComplete = itemView.findViewById(R.id.btnComplete);
        }

        void bind(DailyQuest quest, QuestListener listener, int position) {
            tvTitle.setText(quest.getTitle());
            tvDescription.setText(quest.getDescription());
            tvXp.setText(itemView.getContext().getString(R.string.quest_xp, (int) quest.getXpReward()));
            if (quest.isCompletedToday()) {
                btnComplete.setText(R.string.completed);
                btnComplete.setEnabled(false);
            } else {
                btnComplete.setText(R.string.complete);
                btnComplete.setEnabled(true);
                btnComplete.setOnClickListener(v -> {
                    if (listener != null) listener.onComplete(quest, position);
                });
            }
        }
    }
}
