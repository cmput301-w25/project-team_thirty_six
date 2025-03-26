package com.example.androidproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * The HomePageActivity serves as the main landing page after the user logs in or signs up. This
 * activity displays the three most recent mood events posted by users that the current user follows
 *
 * This class interacts with the following components:
 * - FeedManager: Used to fetch the following list and mood events
 * - MoodArrayAdapter: Displays mood events in a list view
 * - FeedActivity: Displays the entire following feed
 * - MoodState: Model for the mood events
 * - NavBarFragment: Navigation capabilities
 */
public class HomePageActivity extends AppCompatActivity {
    private String currentUser;
    private ListView recentMoodsList;
    private MoodArrayAdapter moodAdapter;
    private ArrayList<MoodState> moodDataList;
    private ArrayList<String> following;
    private FeedManager feedManager;
    private static final String TAG = "HomePageActivity";

    /**
     * Runs the main home page activity loop
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        // Initialize FeedManager
        feedManager = new FeedManager();

        // Get the string from the login page
        currentUser = (String) getIntent().getSerializableExtra("currentUser");
        Log.d(TAG, "Current user: " + currentUser);

        // Initialize the NavBarFragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.nav_bar_container, NavBarFragment.newInstance(currentUser))
                    .commit();
        }

        // Set up the ListView for recent moods
        recentMoodsList = findViewById(R.id.recentMoodsList);
        moodDataList = new ArrayList<>();
        // Use the existing MoodArrayAdapter and pass the current user
        moodAdapter = new MoodArrayAdapter(this, moodDataList, currentUser);
        recentMoodsList.setAdapter(moodAdapter);

        // Load recent moods from following list
        fetchFollowingAndMoods();
    }

    /**
     * Fetches the following list and then fetches moods for the followed users.
     */
    private void fetchFollowingAndMoods() {
        feedManager.getFollowing(currentUser, new FeedManager.FollowingCallback() {
            @Override
            public void onCallback(ArrayList<String> followingList) {
                if (followingList != null && !followingList.isEmpty()) {
                    following = followingList;

                    // Fetch moods for the followed users
                    fetchMoodsFromFollowing();
                } else {
                    Toast.makeText(HomePageActivity.this, "You are not following anyone yet.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Fetches moods from users in the following list
     */
    private void fetchMoodsFromFollowing() {
        feedManager.fetchFeed(following, new FeedManager.FeedCallback() {
            @Override
            public void onCallback(ArrayList<MoodState> feed) {
                if (feed != null && !feed.isEmpty()) {
                    // Process and display the moods
                    processMoods(feed);
                } else {
                    Toast.makeText(HomePageActivity.this, "No recent moods from people you follow.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Processes and displays the mood data
     * @param moods List of moods to process
     */
    private void processMoods(ArrayList<MoodState> moods) {
        // Clear current data
        moodDataList.clear();

        // Sort by date
        Collections.sort(moods, new Comparator<MoodState>() {
            @Override
            public int compare(MoodState m1, MoodState m2) {
                return m2.getDayTime().compareTo(m1.getDayTime()); // Reverse order
            }
        });

        // Take only the 3 most recent moods
        int moodCount = Math.min(moods.size(), 3);
        for (int i = 0; i < moodCount; i++) {
            moodDataList.add(moods.get(i));
        }

        // Update the adapter
        moodAdapter.notifyDataSetChanged();
    }

    /**
     * Takes you to a page to view the feed
     * @param view The view that was clicked
     */
    public void viewFeed(View view) {
        Intent i = new Intent(this, FeedActivity.class);
        i.putExtra("currentUser", currentUser);
        startActivity(i);
    }
}