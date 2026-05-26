package com.example.lifeos.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.lifeos.interfaces.FirebaseCallback;
import com.example.lifeos.interfaces.SimpleCallback;
import com.example.lifeos.models.DailyQuest;
import com.example.lifeos.repositories.QuestRepository;

import java.util.List;

public class QuestViewModel extends ViewModel {

    private final QuestRepository questRepository = new QuestRepository();
    private final MutableLiveData<List<DailyQuest>> quests = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();

    public LiveData<List<DailyQuest>> getQuests() { return quests; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }
    public LiveData<String> getMessage() { return message; }

    public void loadQuests(String userId) {
        loading.setValue(true);
        questRepository.loadQuestsWithStatus(userId, new FirebaseCallback<List<DailyQuest>>() {
            @Override
            public void onSuccess(List<DailyQuest> result) {
                loading.setValue(false);
                quests.setValue(result);
            }
            @Override
            public void onError(String message) {
                loading.setValue(false);
                error.setValue(message);
            }
        });
    }

    public void completeQuest(String userId, DailyQuest quest) {
        if (quest.isCompletedToday()) return;
        loading.setValue(true);
        questRepository.completeQuest(userId, quest, new SimpleCallback() {
            @Override
            public void onSuccess() {
                loading.setValue(false);
                quest.setCompletedToday(true);
                message.setValue("Quest completed! +" + quest.getXpReward() + " XP");
                quests.setValue(quests.getValue());
            }
            @Override
            public void onError(String msg) {
                loading.setValue(false);
                error.setValue(msg);
            }
        });
    }

    public int getCompletedCount() {
        List<DailyQuest> list = quests.getValue();
        if (list == null) return 0;
        int count = 0;
        for (DailyQuest q : list) if (q.isCompletedToday()) count++;
        return count;
    }

    public int getTotalCount() {
        List<DailyQuest> list = quests.getValue();
        return list != null ? list.size() : 0;
    }
}
