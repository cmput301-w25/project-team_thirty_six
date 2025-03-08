package com.example.androidproject;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * MoodHistoryActivity is an activity that displays the mood history of a user.
 * It allows users to view, filter, and manage their mood entries.
 */

public class MoodHistoryActivity extends AppCompatActivity {

    private ArrayList<MoodState> moodHistory;
    private ArrayList<MoodState> completeMoodHistory;
    private MoodArrayAdapter moodAdapter;

    private ListView moodListView;

    private MoodHistoryManager moodHistoryManager;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_history);

        // Initialize MoodHistoryManager
        moodHistoryManager = new MoodHistoryManager();

        // Initialize mood history lists
        moodHistory = new ArrayList<>();
        completeMoodHistory = new ArrayList<>();

        // Initialize the adapter with an empty list
        moodAdapter = new MoodArrayAdapter(this, moodHistory);
        moodListView = findViewById(R.id.mood_list);
        moodListView.setAdapter(moodAdapter);

        //change this later to use user id or
        // add user authentication to make sure usernames are unique
        currentUsername = "testUser"; // dummy user

        // Fetch mood history for the current user
        fetchMoodHistory(currentUsername);

        // Set up the filter button
        ImageButton filterButton = findViewById(R.id.filter_button);
        filterButton.setOnClickListener(view -> showFilterDialog());
    }

    /**
     * Fetches mood history from Firestore and updates the UI.
     *
     * @param username
     *      The username of the user whose mood history is to be fetched.
     */
    private void fetchMoodHistory(String username) {
        moodHistoryManager.fetchMoodHistory(username, new MoodHistoryManager.MoodHistoryCallback() {
            @Override
            public void onCallback(ArrayList<MoodState> moodHistory) {
                if (moodHistory != null) {
                    // Update the mood history lists
                    MoodHistoryActivity.this.moodHistory.clear();
                    MoodHistoryActivity.this.moodHistory.addAll(moodHistory);

                    completeMoodHistory.clear();
                    completeMoodHistory.addAll(moodHistory);

                    // Sort the mood history by date and time
                    sortMoodHistory();

                    // Notify the adapter that the data has changed
                    moodAdapter.notifyDataSetChanged();
                } else {
                    Log.e("MoodState", "Failed to fetch mood history");

                    // Use dummy data as a fallback
                    ArrayList<MoodState> dummyData = generateDummyData();
                    MoodHistoryActivity.this.moodHistory.clear();
                    MoodHistoryActivity.this.moodHistory.addAll(dummyData);

                    completeMoodHistory.clear();
                    completeMoodHistory.addAll(dummyData);

                    // Sort the mood history by date and time
                    sortMoodHistory();

                    // Notify the adapter that the data has changed
                    moodAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    /**
     * Sorts the mood history by date and time in reverse chronological order.
     */
    private void sortMoodHistory() {
        Collections.sort(moodHistory, new Comparator<MoodState>() {
            @Override
            public int compare(MoodState m1, MoodState m2) {
                return m2.getDayTime().compareTo(m1.getDayTime()); // Reverse order
            }
        });
        moodAdapter.notifyDataSetChanged(); // Refresh the adapter
    }

    /**
     * Filters the mood history by the most recent week.
     */
    public void filterByRecentWeek() {
        ArrayList<MoodState> filteredMoods = Filter.filterByRecentWeek(moodHistory);
        moodAdapter.clear();
        moodAdapter.addAll(filteredMoods);
        moodAdapter.notifyDataSetChanged();
    }

    /**
     * Filters the mood history by a specific emotional state.
     *
     * @param emotionalState
     *        The emotional state to filter by.
     */
    public void filterByEmotionalState(String emotionalState) {
        ArrayList<MoodState> filteredMoods = Filter.filterByEmotionalState(moodHistory, emotionalState);
        moodAdapter.clear();
        moodAdapter.addAll(filteredMoods);
        moodAdapter.notifyDataSetChanged();
    }

    /**
     * Filters the mood history by a keyword in the reason text.
     *
     * @param keyword
     *      The keyword to filter by.
     */
    public void filterByKeyword(String keyword) {
        ArrayList<MoodState> filteredMoods = Filter.filterByKeyword(moodHistory, keyword);
        moodAdapter.clear();
        moodAdapter.addAll(filteredMoods);
        moodAdapter.notifyDataSetChanged();
    }

    /**
     * Shows the filter dialog with options.
     */
    public void showFilterDialog() {
        // Inflate the custom layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_filter, null);

        CheckBox checkRecentWeek = dialogView.findViewById(R.id.check_recent_week);
        CheckBox checkFilterMood = dialogView.findViewById(R.id.check_filter_mood);
        CheckBox checkFilterKeyword = dialogView.findViewById(R.id.check_filter_keyword);
        Spinner spinnerMoods = dialogView.findViewById(R.id.spinner_moods);
        EditText editKeyword = dialogView.findViewById(R.id.edit_keyword);

        editKeyword.setHintTextColor(Color.LTGRAY);  // Light gray for hint text for contrast
        spinnerMoods.setBackgroundColor(Color.GRAY);

        // Set custom title with white text color
        TextView titleTextView = new TextView(this);
        titleTextView.setText("Filter by");
        titleTextView.setTextColor(Color.WHITE);
        titleTextView.setTextSize(18);  // Optional: Adjust text size
        titleTextView.setPadding(350, 50, 16, 16);  // Optional: Add padding for better look

        // Get the mood options from the string array
        String[] moods = getResources().getStringArray(R.array.moods_array);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, moods);
        spinnerMoods.setAdapter(adapter);

        // Show/hide spinner and edit text based on checkboxes
        checkFilterMood.setOnCheckedChangeListener((buttonView, isChecked) ->
                spinnerMoods.setVisibility(isChecked ? View.VISIBLE : View.GONE)
        );

        checkFilterKeyword.setOnCheckedChangeListener((buttonView, isChecked) ->
                editKeyword.setVisibility(isChecked ? View.VISIBLE : View.GONE)
        );

        // Build the dialog with the custom style
        new MaterialAlertDialogBuilder(this, R.style.CustomDialogStyle)  // Apply custom style here
                .setCustomTitle(titleTextView)  // Set the custom title here
                .setView(dialogView)
                .setPositiveButton("Apply", (dialog, which) -> {
                    if (checkRecentWeek.isChecked()) {
                        filterByRecentWeek();
                    }
                    if (checkFilterMood.isChecked()) {
                        String selectedMood = spinnerMoods.getSelectedItem().toString();
                        filterByEmotionalState(selectedMood);
                    }
                    if (checkFilterKeyword.isChecked()) {
                        String keyword = editKeyword.getText().toString().trim();
                        if (!keyword.isEmpty()) {
                            filterByKeyword(keyword);
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .setNeutralButton("Reset", (dialog, which) -> {
                    displayAllMoods();
                })
                .show();
    }

    /**
     * Resets the mood history to display all moods.
     */
    private void displayAllMoods() {
        moodHistory.clear();
        moodHistory.addAll(completeMoodHistory); // Restore all moods from the original list
        moodAdapter.notifyDataSetChanged();
    }

    /**
     * Generates dummy data for testing purposes.
     */
    private ArrayList<MoodState> generateDummyData() {
        ArrayList<MoodState> dummyMoods = new ArrayList<>();

        MoodState mood1 = new MoodState("Happiness");
        mood1.setUser("user123");
        mood1.setId("mood_001");
        mood1.setReason("Had a great day with friends");
        mood1.setDayTime(LocalDateTime.now().minusDays(1));

        MoodState mood2 = new MoodState("Sadness");
        mood2.setUser("user456");
        mood2.setId("mood_002");
        mood2.setReason("Feeling lonely");
        mood2.setDayTime(LocalDateTime.now().minusDays(3));

        MoodState mood3 = new MoodState("Fear");
        mood3.setUser("user789");
        mood3.setId("mood_003");
        mood3.setReason("Worried about the future");
        mood3.setDayTime(LocalDateTime.now().minusDays(5));

        MoodState mood4 = new MoodState("Anger");
        mood4.setUser("user111");
        mood4.setId("mood_004");
        mood4.setReason("Frustrated with work");
        mood4.setDayTime(LocalDateTime.now().minusDays(7));

        MoodState mood5 = new MoodState("Surprise");
        mood5.setUser("user222");
        mood5.setId("mood_005");
        mood5.setReason("Unexpected news");
        mood5.setDayTime(LocalDateTime.now().minusDays(10));

        dummyMoods.add(mood1);
        dummyMoods.add(mood2);
        dummyMoods.add(mood3);
        dummyMoods.add(mood4);
        dummyMoods.add(mood5);

        return dummyMoods;
    }
}