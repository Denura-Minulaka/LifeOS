package com.example.lifeos.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lifeos.R;
import com.example.lifeos.adapters.QuestAdapter;
import com.example.lifeos.adapters.TaskAdapter;
import com.example.lifeos.models.DailyQuest;
import com.example.lifeos.models.Task;
import com.example.lifeos.repositories.AuthRepository;
import com.example.lifeos.utils.TimeUtils;
import com.example.lifeos.viewmodels.QuestViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;

import java.util.Calendar;

public class QuestsFragment extends Fragment implements QuestAdapter.QuestListener, TaskAdapter.TaskListener {

    private QuestViewModel viewModel;
    private QuestAdapter questAdapter;
    private TaskAdapter taskAdapter;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quests, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(QuestViewModel.class);
        AuthRepository auth = new AuthRepository();
        if (auth.getCurrentUser() == null) return;
        userId = auth.getCurrentUser().getUid();

        TextView tvProgress = view.findViewById(R.id.tvProgress);
        RecyclerView recyclerQuests = view.findViewById(R.id.recyclerQuests);
        RecyclerView recyclerTasks = view.findViewById(R.id.recyclerTasks);
        FloatingActionButton fabAdd = view.findViewById(R.id.fabAddTask);

        questAdapter = new QuestAdapter();
        questAdapter.setListener(this);
        recyclerQuests.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerQuests.setAdapter(questAdapter);

        taskAdapter = new TaskAdapter();
        taskAdapter.setListener(this);
        recyclerTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerTasks.setAdapter(taskAdapter);

        viewModel.getQuests().observe(getViewLifecycleOwner(), quests -> {
            questAdapter.setQuests(quests);
            tvProgress.setText("You have " + quests.size() + " quests remaining today");
        });

        viewModel.getTasks().observe(getViewLifecycleOwner(), tasks -> {
            taskAdapter.setTasks(tasks);
        });

        viewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        });
        viewModel.getError().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        });

        fabAdd.setOnClickListener(v -> showAddTaskDialog());

        viewModel.loadQuests(userId);
    }

    private void showAddTaskDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_add_task);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextInputEditText etTitle = dialog.findViewById(R.id.etTaskTitle);
        TextInputEditText etDesc = dialog.findViewById(R.id.etTaskDesc);
        MaterialButton btnSelectTime = dialog.findViewById(R.id.btnSelectTime);
        TextView tvSelectedTime = dialog.findViewById(R.id.tvSelectedTime);
        MaterialButton btnCreate = dialog.findViewById(R.id.btnCreate);

        final Calendar calendar = Calendar.getInstance();
        final Timestamp[] selectedTimeframe = {null};

        btnSelectTime.setOnClickListener(v -> {
            new DatePickerDialog(requireContext(), (view, year, month, day) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, day);

                new TimePickerDialog(requireContext(), (view1, hour, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minute);

                    selectedTimeframe[0] = new Timestamp(calendar.getTime());
                    tvSelectedTime.setText("Deadline: " + TimeUtils.formatDateTime(calendar.getTime()));
                    tvSelectedTime.setVisibility(View.VISIBLE);
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();

            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnCreate.setOnClickListener(v -> {
            String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
            if (title.isEmpty()) {
                etTitle.setError("Title required");
                return;
            }

            Task task = new Task();
            task.setUserId(userId);
            task.setTitle(title);
            task.setDescription(etDesc.getText() != null ? etDesc.getText().toString().trim() : "");
            task.setTimeframe(selectedTimeframe[0]);
            
            viewModel.createTask(task, userId);
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (userId != null) viewModel.loadQuests(userId);
    }

    @Override
    public void onComplete(DailyQuest quest, int position) {
        viewModel.completeQuest(userId, quest);
    }

    @Override
    public void onDone(Task task, int position) {
        viewModel.completeTask(userId, task.getId());
    }
}
