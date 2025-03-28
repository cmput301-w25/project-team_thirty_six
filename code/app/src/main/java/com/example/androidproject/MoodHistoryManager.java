package com.example.androidproject;

import android.net.Uri;
import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalDateTime;
import android.location.Location;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * MoodHistoryManager is a utility class responsible for managing mood history data in Firestore.
 * It provides methods to fetch, store, and manipulate mood entries for a specific user.
 */

public class MoodHistoryManager {
    private FirebaseFirestore db;

    public MoodHistoryManager() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Fetches the mood history for a user from the "moods" collection.
     *
     * @param userId   The ID of the user.
     * @param callback The callback to handle the result.
     */
    public void fetchMoodHistory(String userId, MoodHistoryCallback callback) {
        db.collection("Moods")
                .whereEqualTo("user", userId) // Filter moods by the user ID
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<MoodState> moodHistory = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        MoodState moodState = documentToMoodState(document);
                        moodHistory.add(moodState);
                    }
                    callback.onCallback(moodHistory); // Pass the fetched moods to the callback
                })
                .addOnFailureListener(e -> {
                    Log.w("MoodHistoryManager", "Error fetching mood history", e);
                    callback.onCallback(null); // Return null to indicate an error
                });
    }

    /**
     * Fetches the mood history for a user while ignoring private moods from the "moods" collection.
     *
     * @param userId   The ID of the user.
     * @param callback The callback to handle the result.
     */
    public void fetchMoodHistoryIgnorePrivate(String userId, MoodHistoryCallback callback) {
        db.collection("Moods")
                .whereEqualTo("user", userId) // Filter moods by the user ID
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<MoodState> moodHistory = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        MoodState moodState = documentToMoodState(document);
                        // Ignores all private moods in mood history
                        if (moodState.visibility) {
                            moodHistory.add(moodState);
                        }
                    }
                    callback.onCallback(moodHistory); // Pass the fetched moods to the callback
                })
                .addOnFailureListener(e -> {
                    Log.w("MoodHistoryManager", "Error fetching mood history", e);
                    callback.onCallback(null); // Return null to indicate an error
                });
    }

    /**
     * Fetches the mood feed for a user from the "moods" collection.
     *
     * @param userId   The ID of the user.
     * @param callback The callback to handle the result.
     */
    public void fetchMoodFeed(String userId, MoodHistoryCallback callback) {
        // Gets the list of people the user is following
        db.collection("Users")
                .whereEqualTo("username",userId)// Finds the user
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Ensures the document list isn't empty
                    if (queryDocumentSnapshots.size() == 0) {
                        callback.onCallback(null);
                    } else {
                        // Gets the doc for the user
                        DocumentSnapshot userDoc = queryDocumentSnapshots.getDocuments().get(0);
                        ArrayList<String> followingList = (ArrayList<String>) userDoc.get("following");
                        // Gets the array list for moodHistory
                        ArrayList<MoodState> feedHistory = new ArrayList<>();
                        for (int i = 0; i < followingList.size(); i++) {
                            // Copies i for inside callback
                            int finalI = i;
                            fetchMoodHistoryIgnorePrivate(followingList.get(i), new MoodHistoryCallback() {
                                /**
                                 * Handles receiving data back from moodHistory
                                 * @param moodHistory
                                 */
                                @Override
                                public void onCallback(ArrayList<MoodState> moodHistory) {
                                    Log.e("MOOD FEED SIZE",String.valueOf(moodHistory.size()));
                                    feedHistory.addAll(moodHistory);
                                    if (finalI == followingList.size() -1) {
                                        callback.onCallback(feedHistory);
                                    }
                                }
                            });
                        }
                    }
                });
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
        moodState.setVisibility(document.get("visibility", Boolean.class));

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
        // Reconstruct location from Firestore
        if (document.contains("location")) {
            Object locationObj = document.get("location");

            if (locationObj instanceof Map) {
                Map<String, Object> locationMap = (Map<String, Object>) locationObj;
                if (locationMap.containsKey("latitude")  && locationMap.containsKey("longitude")) {
                    double lat = locationMap.get("latitude") != null ? (double) locationMap.get("latitude") : 0.0;
                    double lon = locationMap.get("longitude") != null ? (double) locationMap.get("longitude") : 0.0;

                    Log.d("FirestoreDebug", "Extracted location - Latitude: " + lat + ", Longitude: " + lon);

                    // Reconstruct Location object
                    Location loc = new Location("");
                    loc.setLatitude(lat);
                    loc.setLongitude(lon);
                    moodState.setLocation(loc);
                } else {
                    Log.w("FirestoreDebug", "Location map exists but missing latitude/longitude keys.");
                }
            } else {
                Log.w("FirestoreDebug", "Location field found but not in expected Map format.");
            }
        } else {
            Log.w("FirestoreDebug", "No location field found in Firestore document.");
        }

        return moodState;
    }

    /**
     * Callback interface for fetching mood history.
     */
    public interface MoodHistoryCallback {
        void onCallback(ArrayList<MoodState> moodHistory);
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

}