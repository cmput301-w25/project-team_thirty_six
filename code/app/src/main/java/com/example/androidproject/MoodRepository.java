package com.example.androidproject;

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
            updatedData.put("image", imageUrl);
        }

        // Update Firestore
        db.collection("moods").document(moodId)
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
     * Updates all fields in the dayTime subcollection
     */
    private void updateDayTimeSubcollection(String id, Calendar cal, OnMoodUpdateListener listener) {
        Map<String, Object> dayTimeData = new HashMap<>();

        // Create chronology object
        Map<String, Object> chronology = new HashMap<>();
        chronology.put("calendarType", "iso8601");
        dayTimeData.put("chronology", chronology);

        // Get and set the year
        dayTimeData.put("year", cal.get(Calendar.YEAR));

        // get and set month, and month value
        String[] monthNames = {"JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE",
                "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"};
        dayTimeData.put("month", monthNames[cal.get(Calendar.MONTH)]);
        dayTimeData.put("monthValue", cal.get(Calendar.MONTH) + 1);

        // Get and set month, day of week, and day of year
        dayTimeData.put("dayOfMonth", cal.get(Calendar.DAY_OF_MONTH));
        String[] daysOfWeek = {"SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"};
        dayTimeData.put("dayOfWeek", daysOfWeek[cal.get(Calendar.DAY_OF_WEEK) - 1]);
        dayTimeData.put("dayOfYear", cal.get(Calendar.DAY_OF_YEAR));

        // Get and set the hour, minute, and second
        dayTimeData.put("hour", cal.get(Calendar.HOUR_OF_DAY));
        dayTimeData.put("minute", cal.get(Calendar.MINUTE));
        dayTimeData.put("second", cal.get(Calendar.SECOND));

        // convert milliseconds to nanoseconds
        long nanos = (cal.get(Calendar.MILLISECOND) * 1000000L);
        dayTimeData.put("nano", nanos);

        // Update the dayTime subcollection
        db.collection("moods").document(id)
                .collection("dayTime").document("time_data")
                .set(dayTimeData)
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

    // Interface for callbacks
    public interface OnMoodUpdateListener {
        void onSuccess();
        void onFailure(Exception e);
    }
}