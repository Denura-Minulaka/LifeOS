package com.example.lifeos.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lifeos.R;
import com.example.lifeos.models.Task;
import com.example.lifeos.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface TaskListener {
        void onDone(Task task, int position);
    }

    private final List<Task> tasks = new ArrayList<>();
    private TaskListener listener;

    public void setListener(TaskListener listener) {
        this.listener = listener;
    }

    public void setTasks(List<Task> list) {
        tasks.clear();
        if (list != null) tasks.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TaskViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        holder.bind(tasks.get(position), listener, position);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvTimeframe;
        View btnDone;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvDescription = itemView.findViewById(R.id.tvTaskDescription);
            tvTimeframe = itemView.findViewById(R.id.tvTimeframe);
            btnDone = itemView.findViewById(R.id.btnDone);
        }

        void bind(Task task, TaskListener listener, int position) {
            tvTitle.setText(task.getTitle());
            tvDescription.setText(task.getDescription());
            if (task.getTimeframe() != null) {
                tvTimeframe.setText("Until: " + TimeUtils.formatDateTime(task.getTimeframe().toDate()));
                tvTimeframe.setVisibility(View.VISIBLE);
            } else {
                tvTimeframe.setVisibility(View.GONE);
            }

            btnDone.setOnClickListener(v -> {
                if (listener != null) listener.onDone(task, position);
            });
        }
    }
}
