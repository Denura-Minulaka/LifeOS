package com.example.lifeos.repositories;

import com.example.lifeos.firebase.FirestoreService;
import com.example.lifeos.interfaces.FirebaseCallback;
import com.example.lifeos.interfaces.SimpleCallback;
import com.example.lifeos.models.DailyQuest;

import java.util.ArrayList;
import java.util.List;

public class QuestRepository {

    private final FirestoreService firestoreService = new FirestoreService();

    public void loadQuestsWithStatus(String userId, FirebaseCallback<List<DailyQuest>> callback) {
        firestoreService.getActiveQuests(new FirebaseCallback<List<DailyQuest>>() {
            @Override
            public void onSuccess(List<DailyQuest> quests) {
                if (quests.isEmpty()) {
                    callback.onSuccess(quests);
                    return;
                }
                List<DailyQuest> result = new ArrayList<>(quests);
                final int[] pending = {quests.size()};
                for (int i = 0; i < quests.size(); i++) {
                    DailyQuest quest = quests.get(i);
                    int index = i;
                    firestoreService.isQuestCompletedToday(userId, quest.getId(),
                            new FirebaseCallback<Boolean>() {
                                @Override
                                public void onSuccess(Boolean completed) {
                                    result.get(index).setCompletedToday(completed);
                                    if (--pending[0] == 0) callback.onSuccess(result);
                                }
                                @Override
                                public void onError(String message) {
                                    if (--pending[0] == 0) callback.onSuccess(result);
                                }
                            });
                }
            }
            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public void completeQuest(String userId, DailyQuest quest, SimpleCallback callback) {
        firestoreService.completeQuest(userId, quest, callback);
    }
}
