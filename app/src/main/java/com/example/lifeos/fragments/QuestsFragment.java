package com.example.lifeos.fragments;

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
import com.example.lifeos.repositories.AuthRepository;
import com.example.lifeos.viewmodels.QuestViewModel;

public class QuestsFragment extends Fragment implements QuestAdapter.QuestListener {

    private QuestViewModel viewModel;
    private QuestAdapter adapter;
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
        RecyclerView recycler = view.findViewById(R.id.recyclerQuests);
        adapter = new QuestAdapter();
        adapter.setListener(this);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        recycler.setAdapter(adapter);

        viewModel.getQuests().observe(getViewLifecycleOwner(), quests -> {
            adapter.setQuests(quests);
            tvProgress.setText(getString(R.string.daily_progress,
                    viewModel.getCompletedCount(), viewModel.getTotalCount()));
        });
        viewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        });
        viewModel.getError().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        });

        viewModel.loadQuests(userId);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (userId != null) viewModel.loadQuests(userId);
    }

    @Override
    public void onComplete(com.example.lifeos.models.DailyQuest quest, int position) {
        viewModel.completeQuest(userId, quest);
    }
}
