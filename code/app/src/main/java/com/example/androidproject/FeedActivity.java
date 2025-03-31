package com.example.androidproject;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.Intent;

/**
 * FeedActivity is an activity that displays the mood history of a user.
 * It allows users to view, filter, and manage their mood entries.
 */
public class FeedActivity extends AppCompatActivity {

    private ArrayList<MoodState> feed;
    private ArrayList<String> following;
    private ArrayList<MoodState> completeFeed;
    private MoodArrayAdapter moodAdapter;

    private ListView moodListView;

    FeedManager feedManager;

    private String currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        ImageButton backButton = findViewById(R.id.feed_button_back);

        // Retrieve the currentUser from the Intent
        currentUser = (String) getIntent().getSerializableExtra("currentUser");


        if (currentUser != null) {
            Log.d("FeedActivity", "Current user: " + currentUser);
        } else {
            Log.e("FeedActivity", "No user data found");
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.nav_bar_container, NavBarFragment.newInstance(currentUser))
                    .commit();
        }

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Initialize feedManager
        feedManager = new FeedManager();

        // Initialize mood history lists
        feed = new ArrayList<>();
        completeFeed = new ArrayList<>();

        // Initialize the adapter with an empty list
        moodAdapter = new MoodArrayAdapter(this, feed, currentUser);
        moodListView = findViewById(R.id.mood_list);
        moodListView.setAdapter(moodAdapter);

        // Fetch the following list asynchronously
        fetchFollowingAndFeed();

        // Set up the filter button
        ImageButton filterButton = findViewById(R.id.filter_button);
        filterButton.setOnClickListener(view -> showFilterDialog());
    }

    /**
     * Fetches the following list and then fetches the feed for the followed users.
     */
    private void fetchFollowingAndFeed() {
        feedManager.getFollowing(currentUser, new FeedManager.FollowingCallback() {
            @Override
            public void onCallback(ArrayList<String> followingList) {
                if (followingList != null) {
                    following = followingList;
                    Log.d("FeedActivity", "Following list: " + following);

                    // Now fetch the feed for the followed users
                    fetchFeed();
                } else {
                    Log.e("FeedActivity", "Failed to fetch following list");
                }
            }
        });
    }

    /**
     * Fetches feed from Firestore and updates the UI.
     */
    private void fetchFeed() {
        feedManager.fetchFeed(following, new FeedManager.FeedCallback() {
            @Override
            public void onCallback(ArrayList<MoodState> feed) {
                if (feed != null) {
                    // Update the mood history lists
                    FeedActivity.this.feed.clear();
                    FeedActivity.this.feed.addAll(feed);

                    completeFeed.clear();
                    completeFeed.addAll(feed);

                    // Sort the mood history by date and time
                    sortFeed();

                    // Notify the adapter that the data has changed
                    moodAdapter.notifyDataSetChanged();
                } else {
                    Log.e("FeedActivity", "Failed to fetch feed");
                }
            }
        });
    }

    /**
     * Sorts the mood history by date and time in reverse chronological order.
     */
    private void sortFeed() {
        feed.sort(new Comparator<MoodState>() {
            @Override
            public int compare(MoodState m1, MoodState m2) {
                return m2.getDayTime().compareTo(m1.getDayTime()); // Reverse order
            }
        });
        moodAdapter.notifyDataSetChanged(); // Refresh the adapter

        completeFeed.sort(new Comparator<MoodState>() {
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
        ArrayList<MoodState> filteredMoods = Filter.filterByRecentWeek(feed);
        moodAdapter.clear();
        moodAdapter.addAll(filteredMoods);
        moodAdapter.notifyDataSetChanged();
    }

    /**
     * Filters the mood history by a specific emotional state.
     *
     * @param emotionalState The emotional state to filter by.
     */
    public void filterByEmotionalState(String emotionalState) {
        ArrayList<MoodState> filteredMoods = Filter.filterByEmotionalState(feed, emotionalState);
        moodAdapter.clear();
        moodAdapter.addAll(filteredMoods);
        moodAdapter.notifyDataSetChanged();
    }

    /**
     * Filters the mood history by a keyword in the reason text.
     *
     * @param keyword The keyword to filter by.
     */
    public void filterByKeyword(String keyword) {
        ArrayList<MoodState> filteredMoods = Filter.filterByKeyword(feed, keyword);
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
        CheckBox checkNearbyFollowing = dialogView.findViewById(R.id.check_nearby_following);
        if (checkNearbyFollowing != null) {
            checkNearbyFollowing.setVisibility(View.GONE); //hide the checkbox so that its only accessible from the map filters
            ViewGroup parent = (ViewGroup) checkNearbyFollowing.getParent();
            if (parent != null) {
                parent.setVisibility(View.GONE);
            }
        }
        Spinner spinnerMoods = dialogView.findViewById(R.id.spinner_moods);
        EditText editKeyword = dialogView.findViewById(R.id.edit_keyword);

        editKeyword.setHintTextColor(Color.LTGRAY);  // Light gray for hint text for contrast
        editKeyword.setTextColor(Color.WHITE);
        spinnerMoods.setBackgroundColor(Color.GRAY);

        // Set custom title with white text color
        TextView titleTextView = new TextView(this);
        titleTextView.setText("Filter by");
        titleTextView.setTextColor(Color.WHITE);
        titleTextView.setTextSize(18);
        titleTextView.setPadding(350, 50, 16, 16);  // Add padding for better look

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
        feed.clear();
        feed.addAll(completeFeed); // Restore all moods from the original list
        moodAdapter.notifyDataSetChanged();
    }



    /**
     * Called when the activity is about to become visible or return to the foreground.
     * Performs essential setup tasks and refreshes the feed data:
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Retrieve the currentUser from the Intent
        currentUser = (String) getIntent().getSerializableExtra("currentUser");

        if (currentUser != null) {
            Log.d("FeedActivity", "Current user: " + currentUser);
        } else {
            Log.e("FeedActivity", "No user data found");
        }

        // Initialize feedManager
        feedManager = new FeedManager();

        // Initialize mood history lists
        feed = new ArrayList<>();
        completeFeed = new ArrayList<>();

        // Initialize the adapter with an empty list
        moodAdapter = new MoodArrayAdapter(this, feed, currentUser);
        moodListView = findViewById(R.id.mood_list);
        moodListView.setAdapter(moodAdapter);

        // Fetch mood history for the current user
        fetchFollowingAndFeed(); // Fetch following list and then fetch feed
    }
}