package com.example.androidproject;

import android.location.Location;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Filter {

    /**
     * Filters the mood history to show only events from the most recent week.
     *
     * @param moodHistory The list of mood states to filter.
     * @return A filtered list containing only mood events from the past week.
     */
    public static ArrayList<MoodState> filterByRecentWeek(ArrayList<MoodState> moodHistory) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneWeekAgo = now.minus(1, ChronoUnit.WEEKS);

        ArrayList<MoodState> filteredMoods = new ArrayList<>();
        for (MoodState mood : moodHistory) {
            if (mood.getDayTime().isAfter(oneWeekAgo)) {
                filteredMoods.add(mood);
            }
        }
        return filteredMoods;
    }

    /**
     * Filters the mood history to show only events with a specific emotional state.
     *
     * @param moodHistory    The list of mood states to filter.
     * @param emotionalState The emotional state to filter by (e.g., "Happiness").
     * @return A filtered list containing only mood events with the specified emotional state.
     */
    public static ArrayList<MoodState> filterByEmotionalState(ArrayList<MoodState> moodHistory, String emotionalState) {
        ArrayList<MoodState> filteredMoods = new ArrayList<>();
        for (MoodState mood : moodHistory) {
            if (mood.getMood().equals(emotionalState)) {
                filteredMoods.add(mood);
            }
        }
        return filteredMoods;
    }

    /**
     * Filters the mood history to show only events where the reason text contains a given keyword.
     *
     * @param moodHistory The list of mood states to filter.
     * @param keyword     The keyword to search for in the reason text.
     * @return A filtered list containing only mood events with the keyword in the reason text.
     */
    public static ArrayList<MoodState> filterByKeyword(ArrayList<MoodState> moodHistory, String keyword) {
        ArrayList<MoodState> filteredMoods = new ArrayList<>();
        for (MoodState mood : moodHistory) {
            if (mood.getReason() != null && mood.getReason().toLowerCase().contains(keyword.toLowerCase())) {
                filteredMoods.add(mood);
            }
        }
        return filteredMoods;
    }

    public static ArrayList<MoodState> filterBy5kDistance(ArrayList<MoodState> moodHistory, Location currentLocation, List<String> followingList, double radius) {
        if (currentLocation == null || followingList == null || followingList.isEmpty()) {
            return new ArrayList<>(); //location permission denied or user isnt following anyone then the results is basically null/empty
        }
        ArrayList<MoodState> filteredMoods = new ArrayList<>(); //a store for moods that may fit the criteria
        // start by checking the radius relative to users current position as well as the users we follow
        ArrayList<MoodState> candidateMoods = new ArrayList<>();
        for (MoodState mood : moodHistory) {
            if (mood.getLocation() != null && followingList.contains(mood.getUser())) {
                // Calculate distance
                // Taken from https://stackoverflow.com/questions/22751306/android-location-distancebetween-and-using-previous-location/22755930#22755930
                // Taken from https://stackoverflow.com/questions/6510050/android-location-distancebetween-and-location-distanceto-difference
                // By: Haasya
                float[] distance = new float[1];
                Location.distanceBetween(
                        currentLocation.getLatitude(), currentLocation.getLongitude(),
                        mood.getLocation().getLatitude(), mood.getLocation().getLongitude(),
                        distance
                );

                // fix added: needed to convert it to kilometers
                if (distance[0] / 1000 <= radius) {
                    candidateMoods.add(mood);
                }
            }
        }
        ArrayList<String> processedUsers = new ArrayList<>(); // we use this to store if the user has been processed (like a boolean)
        Collections.sort(candidateMoods, (m1, m2) ->
                m2.getDayTime().compareTo(m1.getDayTime())); // this sorts the mood in desc order so that each users latest mood is first

        // only need to add the most recent mood for each user we are following
        for (MoodState mood : candidateMoods) {
            String username = mood.getUser();
            if (!processedUsers.contains(username)) {
                filteredMoods.add(mood);
                processedUsers.add(username);
            }
        }
        return filteredMoods;
    }
}
