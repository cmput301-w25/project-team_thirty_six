package com.example.androidproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.androidproject.MoodArrayAdapter;
import com.example.androidproject.MoodHistoryActivity;
import com.example.androidproject.MoodHistoryManager;
import com.example.androidproject.MoodState;
import com.example.androidproject.NavBarFragment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
/**
 * Creates the home page activity functions
 */
public class HomePageActivity extends AppCompatActivity {
    private String currentUser;
    private ListView recentMoodsList;
    private MoodArrayAdapter moodAdapter;
    private ArrayList<MoodState> moodDataList;
    private MoodHistoryManager moodHistoryManager;
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

        // Initialize MoodHistoryManager
        moodHistoryManager = new MoodHistoryManager();

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
        // Use the existing MoodArrayAdapter
        moodAdapter = new MoodArrayAdapter(this, moodDataList);
        recentMoodsList.setAdapter(moodAdapter);

        // Load recent moods from other users
        fetchOtherUsersMoods();
    }

    /**
     * Fetches and displays the three most recent mood events from other users
     */
    private void fetchOtherUsersMoods() {
        // First, get a list of users (excluding the current user)
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users")
                .limit(10)  // Limit to 10 users to avoid excessive queries
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<String> users = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String username = document.getId();
                        // Skip the current user
                        if (!username.equals(currentUser)) {
                            users.add(username);
                        }
                    }

                    if (users.isEmpty()) {
                        Toast.makeText(HomePageActivity.this, "No other users found.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Now fetch moods for these users
                    fetchMoodsForUsers(users);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting users", e);
                    Toast.makeText(HomePageActivity.this, "Error finding other users", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Fetches moods for multiple users and combines them
     * @param users List of usernames to fetch moods for
     */
    private void fetchMoodsForUsers(ArrayList<String> users) {
        final int[] completedQueries = {0};
        final int totalUsers = users.size();
        final ArrayList<MoodState> allMoods = new ArrayList<>();

        for (String username : users) {
            // Use the same method as MoodHistoryActivity but for other users
            moodHistoryManager.fetchMoodHistory(username, new MoodHistoryManager.MoodHistoryCallback() {
                @Override
                public void onCallback(ArrayList<MoodState> userMoods) {
                    completedQueries[0]++;

                    if (userMoods != null && !userMoods.isEmpty()) {
                        // Only add public moods if there's a visibility filter
                        for (MoodState mood : userMoods) {
                            if (mood.getVisibility() == null || mood.getVisibility()) {
                                allMoods.add(mood);
                            }
                        }
                    }

                    // If all queries completed, process and display the moods
                    if (completedQueries[0] == totalUsers) {
                        processMoods(allMoods);
                    }
                }
            });
        }
    }

    /**
     * Processes and displays the mood data
     * @param moods List of moods to process
     */
    private void processMoods(ArrayList<MoodState> moods) {
        if (moods.isEmpty()) {
            Toast.makeText(HomePageActivity.this, "No mood events found from other users.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Clear current data
        moodDataList.clear();

        // Sort by date (newest first) - using Collections.sort for compatibility
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

        Log.d(TAG, "Displaying " + moodDataList.size() + " recent moods from other users");
    }

    /**
     * Takes you to a page to view mood history
     * @param view The view that was clicked
     */
    public void viewMoodHistory(View view) {
        Intent i = new Intent(this, MoodHistoryActivity.class);
        i.putExtra("currentUser", currentUser);
        startActivity(i);
    }
}