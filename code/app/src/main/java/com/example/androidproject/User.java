package com.example.androidproject;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates the user class to keep track of users interactions with others and moods
 */
public class User {
    private Database database;
    String username;
    String password;
    // No setter for these 3 as their logic is handled in the add and delete functions
    ArrayList<User> following;
    ArrayList<MoodEvent> moodHistory;
    MoodEvent mostRecentMood;

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
        //this.moodHistory = new ArrayList<>();
    }



    /**
     * Adds a provided mood to a users mood history and updates the most recent mood
     * @param mood
     *      Mood you are adding to mood history
     */
    public void addMood(MoodEvent mood){
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
    public void deleteMood(MoodEvent mood){
        // If the mood does not exist throw an error
        if (!moodHistory.contains(mood)){
            throw new IllegalArgumentException();
        }
        // Removes the mood from history
        moodHistory.remove(mood);
        // If it is the most recent mood sets second most recent as new most recent
        if (mostRecentMood == mood){
            mostRecentMood = moodHistory.get(moodHistory.size() - 1);
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

    public ArrayList<MoodEvent> getMoodHistory() {
        return moodHistory;
    }

    public String getUsername() {
        return username;
    }

    public ArrayList<User> getFollowing() {
        return following;
    }

    public MoodEvent getMostRecentMood() {
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
}
