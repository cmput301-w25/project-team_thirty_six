package com.example.androidproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MoodHistoryManager moodHistoryManager;
    ArrayList<MoodState> moodHistory1;
    static User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.splash_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


//        // Initialize MoodHistoryManager
//        moodHistoryManager = new MoodHistoryManager();
//
//
//        // Fetch mood history for a user
//        moodHistoryManager.fetchMoodHistory("user123", new MoodHistoryManager.MoodHistoryCallback() {
//            @Override
//            public void onCallback(ArrayList<MoodState> moodHistory) {
//                if (moodHistory != null) {
//                   moodHistory1 = moodHistory;
//                    // Log the retrieved moodHistory
////                    for (MoodState moodState : moodHistory) {
////                        Log.d("MoodState", "Mood: " + moodState.getMood() + ", Time: " + moodState.getDayTime());
//                   }
//                } else {
//                    Log.e("MoodState", "Failed to fetch mood history");
//                }
//            }
//        });


        //testMoodHistory();

//        currentUser = new User("testUser", "passs3");
//        MoodHistoryManager moodHistoryManager = new MoodHistoryManager();
//
//        // Populate the user's fields with data from Firestore
//        currentUser.populateUserFields(moodHistoryManager);
////        ArrayList<MoodState> moodHistory = currentUser.getMoodHistory();
////
////        // Print the mood history for verification
////        for (MoodState mood : moodHistory) {
////            Log.d("MoodHistoryTest", "Mood: " + mood.getMood() + ", Reason: " + mood.getReason());
////        }



    }


    /**
     * Handles the login button click event.
     * Starts the LoginActivity.
     *
     * @param v
     *      The view that was clicked.
     */
    public void loginPage(View v){
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);

    }
    /**
     * Handles the sign-up button click event.
     * Starts the SignUpActivity.
     *
     * @param v
     *      The view that was clicked.
     */
    public void signUpPage(View v){
        Intent i = new Intent(this, SignUpActivity.class);
        startActivity(i);

    }

    public void testMoodHistory() {
        String username = "testUser";
        String password = "passs3";

        // Create a user
        User user = new User(username, password);

        // Create some MoodState objects
        MoodState mood1 = new MoodState("Happiness");
        mood1.setUser(username);
        mood1.setReason("Had a great day with friends");
        mood1.setDayTime(LocalDateTime.now().minusDays(1));

        MoodState mood2 = new MoodState("Sadness");
        mood2.setUser(username);
        mood2.setReason("Feeling lonely");
        mood2.setDayTime(LocalDateTime.now().minusDays(3));

        MoodState mood3 = new MoodState("Fear");
        mood3.setUser(username);
        mood3.setId("mood_003");
        mood3.setReason("Worried about the future");
        mood3.setDayTime(LocalDateTime.now().minusDays(5));

        MoodState mood4 = new MoodState("Anger");
        mood4.setUser(username);
        mood4.setId("mood_004");
        mood4.setReason("Frustrated with work");
        mood4.setDayTime(LocalDateTime.now().minusDays(7));

        MoodState mood5 = new MoodState("Surprise");
        mood5.setUser(username);
        mood5.setId("mood_005");
        mood5.setReason("Unexpected news");
        mood5.setDayTime(LocalDateTime.now().minusDays(10));

        // Add moods to the user's mood history
        user.addMood(mood1);
        user.addMood(mood2);
        user.addMood(mood3);
        user.addMood(mood4);
        user.addMood(mood5);


        // Store the mood history in Firestore
        MoodHistoryManager moodHistoryManager = new MoodHistoryManager();
        moodHistoryManager.storeMoodHistory(user.getUsername(), user.getMoodHistory());

        // Fetch the mood history from Firestore
        moodHistoryManager.fetchMoodHistory(user.getUsername(), new MoodHistoryManager.MoodHistoryCallback() {
            @Override
            public void onCallback(ArrayList<MoodState> moodHistory) {

                // Print the mood history for verification
                for (MoodState mood : moodHistory) {
                    Log.d("MoodHistoryTest", "Mood: " + mood.getMood() + ", Reason: " + mood.getReason());
                }
            }
        });
    }

}