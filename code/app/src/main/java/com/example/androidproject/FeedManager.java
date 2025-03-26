package com.example.androidproject;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * FeedManager is a utility class responsible for managing following data in Firestore.
 * It provides methods to fetch, store, and manipulate mood entries for a specific user.
 */
public class FeedManager {
    private FirebaseFirestore db;

    public FeedManager() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Fetches the following list for a user from the "moods" collection.
     *
     * @param following The following list of the user.
     * @param callback  The callback to handle the result.
     */
    public void fetchFeed(ArrayList<String> following, FeedCallback callback) {
        ArrayList<MoodState> feed = new ArrayList<>();
        int totalUsers = following.size();
        final int[] completedUsers = {0}; // Counter for completed Firestore queries

        if (totalUsers == 0) {
            callback.onCallback(feed); // Return empty feed if no users are followed
            return;
        }

        for (String user : following) {
            db.collection("Moods")
                    .whereEqualTo("user", user) // Filter moods by the user ID
                    .whereEqualTo("visibility",Boolean.TRUE)// Filter out private moods
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            MoodState moodState = documentToMoodState(document);
                            feed.add(moodState);
                        }

                        completedUsers[0]++;
                        if (completedUsers[0] == totalUsers) {
                            callback.onCallback(feed); // Call the callback when all queries are done
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.w("FeedManager", "Error fetching feed for user: " + user, e);
                        completedUsers[0]++;
                        if (completedUsers[0] == totalUsers) {
                            callback.onCallback(feed); // Call the callback even if some queries fail
                        }
                    });
        }
    }

    /**
     * Converts a Firestore document to a MoodState object.
     *
     * @param document The Firestore document representing a mood.
     * @return The MoodState object.
     */
    private MoodState documentToMoodState(QueryDocumentSnapshot document) {
        MoodState moodState = new MoodState(document.getString("mood"));
        moodState.setUser(document.getString("user"));
        moodState.setId(document.getId()); // Use the document ID as the mood ID
        moodState.setReason(document.getString("reason"));

        // Convert dayTime map to LocalDateTime
        Map<String, Object> dayTimeMap = (Map<String, Object>) document.get("dayTime");
        if (dayTimeMap != null) {
            LocalDateTime dayTime = mapToLocalDateTime(dayTimeMap);
            moodState.setDayTime(dayTime);
        }

        // Handle nullable fields
        if (document.getString("image") != null) {
            moodState.setImage(Uri.parse(document.getString("image")));
        }

        return moodState;
    }

    /**
     * Callback interface for fetching mood history.
     */
    public interface FeedCallback {
        void onCallback(ArrayList<MoodState> feed);
    }

    /**
     * Converts a dayTime map to a LocalDateTime object.
     *
     * @param dayTimeMap The map representing the dayTime.
     * @return The LocalDateTime object.
     */
    private LocalDateTime mapToLocalDateTime(Map<String, Object> dayTimeMap) {
        // Convert Long values to int
        int year = ((Long) dayTimeMap.get("year")).intValue();
        int monthValue = ((Long) dayTimeMap.get("monthValue")).intValue();
        int dayOfMonth = ((Long) dayTimeMap.get("dayOfMonth")).intValue();
        int hour = ((Long) dayTimeMap.get("hour")).intValue();
        int minute = ((Long) dayTimeMap.get("minute")).intValue();
        int second = ((Long) dayTimeMap.get("second")).intValue();
        int nano = ((Long) dayTimeMap.get("nano")).intValue();

        return LocalDateTime.of(year, monthValue, dayOfMonth, hour, minute, second, nano);
    }

    /**
     * Retrieves the following array for a specific user as an ArrayList<String>.
     *
     * @param username The username of the user whose following list needs to be retrieved.
     * @param callback The callback to handle the result.
     */
    public void getFollowing(String username, FollowingCallback callback) {
        DocumentReference userRef = db.collection("Users").document(username);

        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        List<String> following = (List<String>) document.get("following");
                        if (following != null) {
                            callback.onCallback(new ArrayList<>(following));
                        } else {
                            callback.onCallback(new ArrayList<>()); // Return empty list if "following" field is null
                        }
                    } else {
                        callback.onCallback(new ArrayList<>()); // Return empty list if document doesn't exist
                    }
                } else {
                    Log.w("FeedManager", "Error fetching following list", task.getException());
                    callback.onCallback(new ArrayList<>()); // Return empty list on error
                }
            }
        });
    }

    /**
     * Callback interface for fetching the following list.
     */
    public interface FollowingCallback {
        void onCallback(ArrayList<String> following);
    }
}