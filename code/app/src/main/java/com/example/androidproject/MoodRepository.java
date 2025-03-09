package com.example.androidproject;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Repository class to handle all Firestore update operations related to moods
 */
public class MoodRepository {
    private FirebaseFirestore  db;

    public MoodRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Updates a mood document and its dayTime subcollection in Firestore
     */
    public void updateMood(String moodId, String mood, String situation, String reason,
                           String location, String imageUrl, Calendar dateTime,
                           OnMoodUpdateListener listener) {
        // Validate id is not null
        if (moodId == null) {
            if (listener != null) {
                listener.onFailure(new IllegalArgumentException("Mood ID cannot be null"));
            }
            return;
        }
        // Create the main mood document data
        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("mood", mood);
        updatedData.put("timestamp", dateTime.getTimeInMillis());

        // Only add fields if they are not empty
        if (situation != null && !situation.isEmpty()) {
            updatedData.put("situation", situation);
        }

        if (reason != null && !reason.isEmpty()) {
            updatedData.put("reason", reason);
        }

        if (location != null && !location.isEmpty()) {
            updatedData.put("location", location);
        }

        if (imageUrl != null && !imageUrl.isEmpty()) {
            updatedData.put("id", imageUrl);
        }

        // Update Firestore
        db.collection("Moods").document(moodId)
                .update(updatedData)
                .addOnSuccessListener(aVoid -> {
                    // Now update the dayTime subcollection
                    updateDayTimeSubcollection(moodId, dateTime, listener);
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onFailure(e);
                    }
                });
    }
    /**
     * Deletes a mood from Firestore
     */
    public void deleteMood(String id, OnMoodDeleteListener listener) {
        // Delete the mood document
        db.collection("Moods").document(id)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (listener != null) {
                        listener.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onFailure(e);
                    }
                });
    }

    /**
     * Updates all fields in the dayTime subcollection
     */
    private void updateDayTimeSubcollection(String id, Calendar cal, OnMoodUpdateListener listener) {
        Map<String, Object> dayTimeData = new HashMap<>();

        // Month names array
        String[] monthNames = {"JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE",
                "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"};

        // Day of week names array
        String[] daysOfWeek = {"SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"};

        // Year and month details
        dayTimeData.put("year", cal.get(Calendar.YEAR));
        dayTimeData.put("monthValue", cal.get(Calendar.MONTH) + 1);
        dayTimeData.put("month", monthNames[cal.get(Calendar.MONTH)]);

        // Day details
        dayTimeData.put("dayOfMonth", cal.get(Calendar.DAY_OF_MONTH));
        dayTimeData.put("dayOfWeek", daysOfWeek[cal.get(Calendar.DAY_OF_WEEK) - 1]);
        dayTimeData.put("dayOfYear", cal.get(Calendar.DAY_OF_YEAR));

        // Time details
        dayTimeData.put("hour", cal.get(Calendar.HOUR_OF_DAY));
        dayTimeData.put("minute", cal.get(Calendar.MINUTE));
        dayTimeData.put("second", cal.get(Calendar.SECOND));
        long nanos = (cal.get(Calendar.MILLISECOND) * 1000000L);
        dayTimeData.put("nano", nanos);

        Map<String, Object> chronology = new HashMap<>();
        chronology.put("calendarType", "iso8601");
        dayTimeData.put("chronology", chronology);

        // Update the dayTime subcollection
        db.collection("Moods").document(id)
                .collection("dayTime").document("time_data")
                .set(dayTimeData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("MoodRepository", "DayTime subcollection updated successfully");
                    if (listener != null) {
                        listener.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("MoodRepository", "Failed to update dayTime subcollection", e);
                    if (listener != null) {
                        listener.onFailure(e);
                    }
                });

    }

    // Interface for callbacks
    public interface OnMoodUpdateListener {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface OnMoodDeleteListener {
        void onSuccess();
        void onFailure(Exception e);
    }
}