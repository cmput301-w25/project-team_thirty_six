package com.example.androidproject;

import android.net.Uri;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoodHistoryManager {
    private FirebaseFirestore db;

    public MoodHistoryManager() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Stores a user's moodHistory in Firestore.
     *
     * @param userId      The ID of the user.
     * @param moodHistory The list of MoodState objects to store.
     */
    public void storeMoodHistory(String userId, List<MoodState> moodHistory) {
        // Convert the list of MoodState objects to a list of maps
        List<Map<String, Object>> moodHistoryMap = new ArrayList<>();
        for (MoodState moodState : moodHistory) {
            moodHistoryMap.add(moodState.toMap());
        }

        // Create a document with the moodHistory array
        Map<String, Object> userMoodHistory = new HashMap<>();
        userMoodHistory.put("moodHistory", moodHistoryMap);

        // Store the document in Firestore
        db.collection("Users")
                .document(userId)
                .set(userMoodHistory)
                .addOnSuccessListener(aVoid -> {
                    Log.d("MoodHistoryManager", "Mood history stored successfully for user: " + userId);
                })
                .addOnFailureListener(e -> {
                    Log.w("MoodHistoryManager", "Error storing mood history", e);
                });
    }

    /**
     * Fetches the moodHistory for a user from Firestore.
     *
     * @param userId   The ID of the user.
     * @param callback The callback to handle the result.
     */
    public void fetchMoodHistory(String userId, MoodHistoryCallback callback) {
        db.collection("Users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> moodHistoryMap = (List<Map<String, Object>>) documentSnapshot.get("moodHistory");
                        ArrayList<MoodState> moodHistory = new ArrayList<>(); // Use ArrayList directly

                        if (moodHistoryMap != null) {
                            for (Map<String, Object> moodMap : moodHistoryMap) {
                                MoodState moodState = mapToMoodState(moodMap);
                                moodHistory.add(moodState);
                            }
                        }

                        callback.onCallback(moodHistory); // Pass ArrayList to callback
                    } else {
                        Log.d("MoodHistoryManager", "No such document");
                        callback.onCallback(new ArrayList<>()); // Return an empty ArrayList if no document exists
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w("MoodHistoryManager", "Error fetching mood history", e);
                    callback.onCallback(null); // Return null to indicate an error
                });
    }

    /**
     * Converts a Firestore map to a MoodState object.
     *
     * @param moodStateMap The map representing a MoodState.
     * @return The MoodState object.
     */
    private MoodState mapToMoodState(Map<String, Object> moodStateMap) {
        MoodState moodState = new MoodState((String) moodStateMap.get("mood"));
        moodState.setUser((String) moodStateMap.get("username"));
        moodState.setId((String) moodStateMap.get("id"));
        moodState.setReason((String) moodStateMap.get("reason"));
        moodState.setDayTime(LocalDateTime.parse((String) moodStateMap.get("dayTime"), DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        // Handle nullable fields
        if (moodStateMap.get("image") != null) {
            moodState.setImage(Uri.parse((String) moodStateMap.get("image")));
        }

        return moodState;
    }

    /**
     * Callback interface for fetching mood history.
     */
    public interface MoodHistoryCallback {
        void onCallback(ArrayList<MoodState> moodHistory);
    }
}