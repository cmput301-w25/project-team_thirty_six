package com.example.androidproject;

import android.location.Location;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalDateTime;
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
        // ✅ Reconstruct location from Firestore
        if (document.contains("location")) {
            Object locationObj = document.get("location");

            if (locationObj instanceof Map) {
                Map<String, Object> locationMap = (Map<String, Object>) locationObj;
                if (locationMap.containsKey("latitude")  && locationMap.containsKey("longitude")) {
                    double lat = locationMap.get("latitude") != null ? (double) locationMap.get("latitude") : 0.0;
                    double lon = locationMap.get("longitude") != null ? (double) locationMap.get("longitude") : 0.0;

                    // ✅ Debug log to check extracted values
                    Log.d("FirestoreDebug", "Extracted location - Latitude: " + lat + ", Longitude: " + lon);

                    // Reconstruct Location object
                    Location loc = new Location("");
                    loc.setLatitude(lat);
                    loc.setLongitude(lon);
                    moodState.setLocation(loc); // ✅ Set reconstructed Location object
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