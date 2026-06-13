package com.example.lifeos.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.lifeos.interfaces.FirebaseCallback;
import com.example.lifeos.interfaces.SimpleCallback;
import com.example.lifeos.models.DailyQuest;
import com.example.lifeos.models.Task;
import com.example.lifeos.repositories.QuestRepository;

import java.util.List;

public class QuestViewModel extends ViewModel {

    private final QuestRepository questRepository = new QuestRepository();
    private final MutableLiveData<List<DailyQuest>> quests = new MutableLiveData<>();
    private final MutableLiveData<List<Task>> tasks = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();

    public LiveData<List<DailyQuest>> getQuests() { return quests; }
    public LiveData<List<Task>> getTasks() { return tasks; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }
    public LiveData<String> getMessage() { return message; }

    public void loadQuests(String userId) {
        loading.setValue(true);
        questRepository.loadQuestsWithStatus(userId, new FirebaseCallback<List<DailyQuest>>() {
            @Override
            public void onSuccess(List<DailyQuest> result) {
                quests.setValue(result);
                loadTasks(userId);
            }
            @Override
            public void onError(String message) {
                loading.setValue(false);
                error.setValue(message);
            }
        });
    }

    private void loadTasks(String userId) {
        questRepository.loadUserTasks(userId, new FirebaseCallback<List<Task>>() {
            @Override
            public void onSuccess(List<Task> result) {
                loading.setValue(false);
                tasks.setValue(result);
            }
            @Override
            public void onError(String message) {
                loading.setValue(false);
                error.setValue(message);
            }
        });
    }

    public void completeQuest(String userId, DailyQuest quest) {
        loading.setValue(true);
        questRepository.completeQuest(userId, quest, new SimpleCallback() {
            @Override
            public void onSuccess() {
                loading.setValue(false);
                message.setValue("Quest completed! +" + quest.getXpReward() + " XP");
                loadQuests(userId); // Reload to hide completed
            }
            @Override
            public void onError(String msg) {
                loading.setValue(false);
                error.setValue(msg);
            }
        });
    }

    public void createTask(Task task, String userId) {
        loading.setValue(true);
        questRepository.createTask(task, new SimpleCallback() {
            @Override
            public void onSuccess() {
                message.setValue("Task created!");
                loadTasks(userId);
            }
            @Override
            public void onError(String msg) {
                loading.setValue(false);
                error.setValue(msg);
            }
        });
    }

    public void completeTask(String userId, String taskId) {
        loading.setValue(true);
        questRepository.completeTask(userId, taskId, new SimpleCallback() {
            @Override
            public void onSuccess() {
                message.setValue("Task completed!");
                loadTasks(userId);
            }
            @Override
            public void onError(String msg) {
                loading.setValue(false);
                error.setValue(msg);
            }
        });
    }

    public int getCompletedCount() {
        // This might need adjustment if we only load non-completed ones
        return 0; // Not strictly needed for the new UI if they disappear
    }

    public int getTotalCount() {
        List<DailyQuest> list = quests.getValue();
        return list != null ? list.size() : 0;
    }
}
