package com.example.androidproject;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates the user class to keep track of users interactions with others and moods
 */
public class User {
    String username;
    String password;
    // No setter for these 3 as their logic is handled in the add and delete functions
    ArrayList<User> following;
    static ArrayList<MoodState> moodHistory;
    MoodState mostRecentMood;

    /**
     * Initializes a user
     * @param username password
     *                 Username of the new user
     *                 password of the new user
     */
    public User(String username, String password){
        this.username = username;
        this.password = password;
        this.following = new ArrayList<>();
        this.moodHistory = new ArrayList<>();
    }


    /**
     * Populates the user's fields (moodHistory, following, mostRecentMood) with data from Firestore.
     * @param moodHistoryManager The MoodHistoryManager instance to fetch mood history.
     */
    public void populateUserFields(MoodHistoryManager moodHistoryManager) {
        // Fetch mood history for the user
        moodHistoryManager.fetchMoodHistory(this.username, new MoodHistoryManager.MoodHistoryCallback() {
            @Override
            public void onCallback(ArrayList<MoodState> fetchedMoodHistory) {
                // Update the moodHistory field
                moodHistory.clear();
                moodHistory.addAll(fetchedMoodHistory);

                // Update the mostRecentMood field
                if (!moodHistory.isEmpty()) {
                    mostRecentMood = moodHistory.get(0); // Assuming the list is sorted by date (most recent first)
                }

                // Log the fetched mood history for debugging
                for (MoodState mood : moodHistory) {
                    System.out.println("Mood: " + mood.getMood() + ", Reason: " + mood.getReason() + ", Date: " + mood.getDayTime());
                }
            }
        });

        // Fetch the following list (if applicable)
        // You can add logic here to fetch the list of users this user is following
        // For now, we'll leave it as an empty list
        this.following = new ArrayList<>();
    }





    /**
     * Adds a provided mood to a users mood history and updates the most recent mood
     * @param mood
     *      Mood you are adding to mood history
     */
    public void addMood(MoodState mood){
        // If an exact copy of the mood already exists throw an error
        if (moodHistory.contains(mood)){
            throw new IllegalArgumentException();
        }
        // Adds the mood to mood history and sets it as most recent mood
        moodHistory.add(mood);
        mostRecentMood = mood;
    }

    /**
     * Delete a provided mood to a users mood history and updates the most recent mood if needed
     * @param mood
     *      Mood you are deleting from history
     */
    public void deleteMood(MoodState mood){
        // If the mood does not exist throw an error
        if (!moodHistory.contains(mood)){
            throw new IllegalArgumentException();
        }
        // Removes the mood from history
        moodHistory.remove(mood);
        // If it is the most recent mood sets second most recent as new most recent
        if (mostRecentMood == mood){
            // Finds the new lowest mood
            MoodState newLow;
            newLow = moodHistory.get(0);
            for (int i = 1; i < moodHistory.size(); i++){
                // Checks the element to see if it is after the other
                if (newLow.getDayTime().isBefore(moodHistory.get(i).getDayTime())) {
                    newLow = moodHistory.get(i);
                }
            }
            // Sets mood history to the new low
            this.mostRecentMood = newLow;
        }
    }



    /**
     * Adds a user to the following list
     * @param user
     *      User that you are trying to follow
     */
    public void addFollowing(User user){
        // Throws an error if you are already following the user
        if (following.contains(user)){
            throw new IllegalArgumentException();
        }
        // Adds the user to following list
        following.add(user);
    }

    /**
     * Removes a user from the following list
     * @param user
     *      User that you are no longer following
     */
    public void removeFollowing(User user){
        // Throws an error if you are not following the user
        if (!following.contains(user)){
            throw new IllegalArgumentException();
        }
        // Removes the user to following list
        following.remove(user);
    }

    /**
     * Tests the password to ensure that the user is authenticated
     * @param testPassword
     *      Password given by the user
     */
    public Boolean testPassword(String testPassword){
        return password.equals(testPassword);
    }

    public static ArrayList<MoodState> getMoodHistory() {
        return moodHistory;
    }

    public String getUsername() {
        return username;
    }

    public ArrayList<User> getFollowing() {
        return following;
    }

    public MoodState getMostRecentMood() {
        return mostRecentMood;
    }

    public String getPassword() {
        return password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void fetchMoodHistory(MoodHistoryManager moodHistoryManager, MoodHistoryManager.MoodHistoryCallback callback) {
        moodHistoryManager.fetchMoodHistory(this.username, callback);
    }
}
