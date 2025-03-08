package com.example.androidproject;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

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
}
